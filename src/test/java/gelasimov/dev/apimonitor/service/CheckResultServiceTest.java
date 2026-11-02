package gelasimov.dev.apimonitor.service;

import com.sun.net.httpserver.HttpServer;
import gelasimov.dev.apimonitor.entity.Api;
import gelasimov.dev.apimonitor.entity.CheckResult;
import gelasimov.dev.apimonitor.entity.CheckStatus;
import gelasimov.dev.apimonitor.exception.ApiInactiveException;
import gelasimov.dev.apimonitor.exception.ApiNotFoundException;
import gelasimov.dev.apimonitor.repository.CheckResultRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CheckResultServiceTest {
    @Autowired
    private ApiService apiService;

    @Autowired
    private CheckResultService checkResultService;

    @Autowired
    private CheckResultRepository checkResultRepository;

    @Test
    void shouldCheckApiAndSaveResult() {
        String url = "https://example.com/" + UUID.randomUUID();
        Api api = apiService.registerApi("Example API", url);

        CheckResult result = checkResultService.checkApi(api.getId());

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getCheckedAt());
        assertNotNull(result.getHttpStatus());
        assertEquals(api.getId(), result.getApi().getId());
    }

    @Test
    void shouldSaveNetworkErrorWhenConnectionFails() {
        String url = "http://127.0.0.1:65534/" + UUID.randomUUID();
        Api api = apiService.registerApi("Example API", url);

        CheckResult result = checkResultService.checkApi(api.getId());

        assertEquals(CheckStatus.NETWORK_ERROR, result.getCheckStatus());
        assertNull(result.getHttpStatus());
        assertNotNull(result.getId());
    }

    @Test
    void shouldSaveTimeoutWhenServerRespondsTooLate() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try {
                Thread.sleep(12_000);
                exchange.sendResponseHeaders(200, -1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                // Клиент мог уже закрыть соединение после таймаута
            } finally {
                exchange.close();
            }
        });

        server.start();

        try {
            int port = server.getAddress().getPort();
            String url = "http://127.0.0.1:" + port;

            Api api = apiService.registerApi("Slow API", url);

            CheckResult result = checkResultService.checkApi(api.getId());

            assertEquals(CheckStatus.TIMEOUT, result.getCheckStatus());
            assertNull(result.getHttpStatus());
            assertNotNull(result.getId());

        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldReturnCheckHistoryForApi() {
        String url = "https://example.com/" + UUID.randomUUID();
        Api api = apiService.registerApi("Example API", url);

        checkResultService.checkApi(api.getId());
        checkResultService.checkApi(api.getId());

        Page<CheckResult> history = checkResultService.getCheckHistory(api.getId(), 0, 20);

        assertEquals(2, history.getContent().size());

        assertTrue(history.getContent().stream().allMatch(result -> result.getApi().getId().equals(api.getId())));
        assertEquals(2, history.getTotalElements());
    }

    @Test
    void shouldThrowWhenApiDoesNotExistForHistory() {
        UUID uuid = UUID.randomUUID();
        assertThrows(ApiNotFoundException.class,
                () -> checkResultService.getCheckHistory(uuid, 0, 20));
    }

    @Test
    void shouldThrowWhenApiIsInactive() {
        String url = "https://example.com/" + UUID.randomUUID();
        Api api = apiService.registerApi("Example API", url);
        apiService.deactivateApi(api.getId());

        assertThrows(ApiInactiveException.class,
                () -> checkResultService.checkApi(api.getId()));
    }

    @Test
    void shouldPaginateCheckHistory() {
        String url = "https://example.com/" + UUID.randomUUID();
        Api api = apiService.registerApi("Example API", url);

        checkResultService.checkApi(api.getId());
        checkResultService.checkApi(api.getId());
        checkResultService.checkApi(api.getId());

        Page<CheckResult> firstPage =
                checkResultService.getCheckHistory(api.getId(), 0, 2);

        Page<CheckResult> secondPage =
                checkResultService.getCheckHistory(api.getId(), 1, 2);

        assertEquals(2, firstPage.getContent().size());
        assertEquals(3, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());

        assertEquals(1, secondPage.getContent().size());
        assertFalse(secondPage.hasNext());
    }

    @Test
    void shouldDeleteCheckResultsOlderThan90Days() {
        String url = "https://example.com/" + UUID.randomUUID();
        Api api = apiService.registerApi("Example API", url);

        CheckResult result = new CheckResult();
        result.setApi(api);
        result.setCheckedAt(LocalDateTime.now().minusDays(91));
        result.setResponseTimeMs(100);
        result.setHttpStatus(200);
        result.setCheckStatus(CheckStatus.SUCCESS);


        CheckResult result2 = new CheckResult();
        result2.setApi(api);
        result2.setCheckedAt(LocalDateTime.now().minusDays(89));
        result2.setResponseTimeMs(100);
        result2.setHttpStatus(200);
        result2.setCheckStatus(CheckStatus.SUCCESS);

        checkResultRepository.saveAndFlush(result);
        checkResultRepository.saveAndFlush(result2);

        int deleted = checkResultService.cleanupOldCheckResults();

        Page<CheckResult> history =
                checkResultService.getCheckHistory(api.getId(), 0, 20);

        assertEquals(1,deleted);
        assertEquals(1,history.getTotalElements());
        assertEquals(result2.getId(), history.getContent().get(0).getId());
    }
}
