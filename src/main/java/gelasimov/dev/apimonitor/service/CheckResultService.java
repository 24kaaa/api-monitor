package gelasimov.dev.apimonitor.service;

import gelasimov.dev.apimonitor.entity.Api;
import gelasimov.dev.apimonitor.entity.CheckResult;
import gelasimov.dev.apimonitor.entity.CheckStatus;
import gelasimov.dev.apimonitor.exception.ApiInactiveException;
import gelasimov.dev.apimonitor.repository.CheckResultRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CheckResultService {
    private final ApiService apiService;
    private final CheckResultRepository checkResultRepository;
    private final HttpClient httpClient;


    public CheckResultService(ApiService apiService, CheckResultRepository checkResultRepository) {
        this.apiService = apiService;
        this.checkResultRepository = checkResultRepository;

        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public CheckResult checkApi(UUID apiId) {
        Api api = apiService.getApiById(apiId);
        if (!api.isActive()) {
            throw new ApiInactiveException("API неактивен");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(api.getUrl()))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<Void> response;

        long startTime = System.nanoTime();

        try {
            response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.discarding()
            );
        } catch (HttpTimeoutException e) {
            int responseTimeMs =
                    (int) ((System.nanoTime() - startTime) / 1_000_000);
            return saveResult(api, responseTimeMs, null, CheckStatus.TIMEOUT);

        } catch (IOException e) {
            int responseTimeMs =
                    (int) ((System.nanoTime() - startTime) / 1_000_000);
            return saveResult(api, responseTimeMs, null, CheckStatus.NETWORK_ERROR);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Запрос был прерван", e);
        }

        int responseTimeMs =
                (int) ((System.nanoTime() - startTime) / 1_000_000);

        int httpStatus = response.statusCode();

        CheckStatus status;

        if (httpStatus >= 200 && httpStatus < 300) {
            status = CheckStatus.SUCCESS;
        } else {
            status = CheckStatus.HTTP_ERROR;
        }

        return saveResult(api, responseTimeMs, httpStatus, status);
    }

    private CheckResult saveResult(Api api, int responseTimeMs, Integer httpStatus, CheckStatus status) {
        CheckResult result = new CheckResult();
        result.setApi(api);
        result.setResponseTimeMs(responseTimeMs);
        result.setCheckedAt(LocalDateTime.now());
        result.setHttpStatus(httpStatus);
        result.setCheckStatus(status);
        return checkResultRepository.save(result);
    }

    public Page<CheckResult> getCheckHistory(UUID apiId, int page, int size) {
        if (page < 0 || size < 1 || size > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректные параметры");

        apiService.getApiById(apiId);
        Pageable pageable = PageRequest.of(page, size);
        return checkResultRepository.findByApi_IdOrderByCheckedAtDesc(apiId, pageable);
    }

    @Transactional
    public int cleanupOldCheckResults() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(90);
        return checkResultRepository.deleteOlderThan(localDateTime);
    }


}
