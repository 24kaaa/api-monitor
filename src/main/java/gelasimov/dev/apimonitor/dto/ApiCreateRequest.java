package gelasimov.dev.apimonitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ApiCreateRequest {
    @NotBlank(message = "Название API не должно быть пустым")
    private String name;
    @Pattern(regexp = "^https?://.+",
            message = "URL должен начинаться с http:// или https://")
    @NotBlank(message = "URL не должен быть пустым")
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
