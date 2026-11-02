package gelasimov.dev.apimonitor.service;

import gelasimov.dev.apimonitor.entity.Api;
import gelasimov.dev.apimonitor.exception.ApiAlreadyRegisteredException;
import gelasimov.dev.apimonitor.exception.ApiNotFoundException;
import gelasimov.dev.apimonitor.repository.ApiRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApiService {
    private final ApiRepository apiRepository;

    public ApiService(ApiRepository apiRepository) {
        this.apiRepository = apiRepository;
    }

    public Api registerApi(String name, String url) {
        Optional<Api> foundApi = apiRepository.findByUrl(url);
        if (foundApi.isPresent()) {
            Api api = foundApi.get();
            if (api.isActive()) {
                throw new ApiAlreadyRegisteredException("Дубликат");
            } else {
                api.setActive(true);
                api.setName(name);
            }
            return apiRepository.save(api);
        }

        Api newApi = new Api();
        newApi.setName(name);
        newApi.setUrl(url);
        return apiRepository.save(newApi);
    }

    public List<Api> getActiveApis() {
        return apiRepository.findByActiveTrue();
    }

    public Api getApiById(UUID id) {
        Api foundApi = apiRepository.findById(id)
                .orElseThrow(() -> new ApiNotFoundException("API не найден"));
        return foundApi;
    }

    public Api deactivateApi(UUID id) {
        Api foundApi = getApiById(id);
        foundApi.setActive(false);
        return apiRepository.save(foundApi);
    }
}
