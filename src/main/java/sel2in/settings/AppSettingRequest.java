package sel2in.settings;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AppSettingRequest(
        Long id,
        @NotBlank String appName,
        @NotBlank String category,
        @NotBlank String propertyName,
        @NotNull SettingType type,
        Integer maxLength,
        String value,
        String timeZone) {
}