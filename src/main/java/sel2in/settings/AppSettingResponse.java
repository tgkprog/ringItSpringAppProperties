package sel2in.settings;

import java.time.Instant;

public record AppSettingResponse(
        Long id,
        String appName,
        String category,
        String propertyName,
        SettingType type,
        Integer maxLength,
        String value,
        String timeZone,
        Instant createdAt,
        Instant updatedAt) {
}