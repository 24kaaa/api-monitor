package gelasimov.dev.apimonitor.controller;

import gelasimov.dev.apimonitor.entity.CheckResult;
import gelasimov.dev.apimonitor.service.CheckResultService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitored-apis/{apiId}/checks")
public class CheckResultController {
    private final CheckResultService checkResultService;

    public CheckResultController(CheckResultService checkResultService) {
        this.checkResultService = checkResultService;
    }

    @PostMapping
    public CheckResult runCheck(@PathVariable UUID apiId) {
        return checkResultService.checkApi(apiId);
    }

    @GetMapping
    public Page<CheckResult> getCheckHistory(@PathVariable UUID apiId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return checkResultService.getCheckHistory(apiId, page,size);
    }
}
