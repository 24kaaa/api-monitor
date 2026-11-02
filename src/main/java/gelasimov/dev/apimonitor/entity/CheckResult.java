package gelasimov.dev.apimonitor.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class CheckResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private int responseTimeMs;
    private LocalDateTime checkedAt;
    private Integer httpStatus;
    @Enumerated(EnumType.STRING)
    private CheckStatus checkStatus;
    @ManyToOne
    @JoinColumn(name = "api_id", nullable = false)
    private Api api;

    public UUID getId() {
        return id;
    }

    public int getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(int responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public CheckStatus getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(CheckStatus checkStatus) {
        this.checkStatus = checkStatus;
    }

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }
}
