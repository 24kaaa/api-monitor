package gelasimov.dev.apimonitor.controller;

import gelasimov.dev.apimonitor.dto.ApiCreateRequest;
import gelasimov.dev.apimonitor.entity.Api;
import gelasimov.dev.apimonitor.service.ApiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitored-apis")
public class ApiController {
    private final ApiService apiService;

    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }


    @GetMapping
    public List<Api> getActiveApis() {
        return apiService.getActiveApis();
    }

    @PostMapping
    public Api createApi(@Valid @RequestBody ApiCreateRequest request) {
        return apiService.registerApi(request.getName(), request.getUrl());
    }

    @GetMapping("/{id}")
    public Api getApiById(@PathVariable UUID id) {
        return apiService.getApiById(id);
    }

    @DeleteMapping("/{id}")
    public Api deactivateApi(@PathVariable UUID id) {
        return apiService.deactivateApi(id);
    }
}
