package gelasimov.dev.apimonitor.scheduler;

import gelasimov.dev.apimonitor.entity.Api;
import gelasimov.dev.apimonitor.service.ApiService;
import gelasimov.dev.apimonitor.service.CheckResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiCheckScheduler {
    private final ApiService apiService;
    private final CheckResultService checkResultService;
    private static final Logger log =
            LoggerFactory.getLogger(ApiCheckScheduler.class);

    public ApiCheckScheduler(ApiService apiService, CheckResultService checkResultService) {
        this.apiService = apiService;
        this.checkResultService = checkResultService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void checkActiveApis() {
        List<Api> result = apiService.getActiveApis();

        for (Api api : result) {
            try {
                checkResultService.checkApi(api.getId());
            } catch (RuntimeException e) {
                log.error("Не удалось проверить API с id={}", api.getId(), e);
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Berlin")
    public void cleanupOldResults() {
        int deleted = checkResultService.cleanupOldCheckResults();
        log.info("Удалено {} старых результатов проверок", deleted);
    }
}
