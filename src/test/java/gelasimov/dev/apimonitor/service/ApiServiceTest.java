package gelasimov.dev.apimonitor.service;

import gelasimov.dev.apimonitor.entity.Api;
import gelasimov.dev.apimonitor.exception.ApiAlreadyRegisteredException;
import gelasimov.dev.apimonitor.exception.ApiNotFoundException;
import gelasimov.dev.apimonitor.repository.ApiRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
public class ApiServiceTest {
    @Autowired
    private ApiService apiService;
    @Autowired
    private ApiRepository apiRepository;

    @Test
    void shouldRegisterNewApi() {
        Api api = apiService.registerApi("Example Api", "https://example-test-12345.com");
        assertNotNull(api);
        assertNotNull(api.getId());
        assertTrue(api.isActive());
    }

    @Test
    void shouldRejectDuplicateActiveApi() {
        String url = "https://example.com/" + UUID.randomUUID();

        apiService.registerApi("Example API", url);

        assertThrows(
                ApiAlreadyRegisteredException.class,
                () -> apiService.registerApi("Example API", url)
        );
    }

    @Test
    void shouldReactivateInactiveApi() {
        String url = "https://example.com/" + UUID.randomUUID();
        Api api = apiService.registerApi("Example API", url);
        api.setActive(false);
        apiRepository.save(api);
        Api reactiveApi = apiService.registerApi("Update Example API", url);
        assertEquals(api.getId(),reactiveApi.getId());
        assertTrue(reactiveApi.isActive());
        assertEquals(reactiveApi.getName(),"Update Example API");
    }

    @Test
    void shouldReturnOnlyActiveApis(){
        String url = "https://example.com/" + UUID.randomUUID();
        Api api = apiService.registerApi("Example API", url);

        String url2 = "https://example.com/" + UUID.randomUUID();
        Api api2 = apiService.registerApi("Example API", url2);

        api.setActive(false);
        apiRepository.save(api);

        List<Api> result = apiService.getActiveApis();

        assertTrue(result.stream().anyMatch(item -> item.getId().equals(api2.getId())));

        assertFalse(result.stream().anyMatch(item -> item.getId().equals(api.getId())));
    }

    @Test
    void shouldReturnApiById() {
        String url = "https://example.com/" + UUID.randomUUID();
        Api api = apiService.registerApi("Example API", url);

        Api foundApi = apiService.getApiById(api.getId());
        assertEquals(foundApi.getId(),api.getId());
    }

    @Test
    void shouldThrowWhenApiIdDoesNotExist() {
        UUID randomId = UUID.randomUUID();

        assertThrows(
                ApiNotFoundException.class, ()-> apiService.getApiById(randomId)
        );
    }

    @Test
    void shouldDeactivateApiWithoutDeletingIt() {
        String url = "https://example.com/" + UUID.randomUUID();
        Api api = apiService.registerApi("Example API", url);
        apiService.deactivateApi(api.getId());
        Api foundApi = apiService.getApiById(api.getId());
        assertFalse(foundApi.isActive());
        assertTrue(apiRepository.findById(api.getId()).isPresent());
    }
}
