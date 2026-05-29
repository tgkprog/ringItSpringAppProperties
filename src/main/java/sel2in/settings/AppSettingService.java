package sel2in.settings;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import sel2in.config.RingitProperties;

import org.springframework.http.HttpStatus;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AppSettingService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final AppSettingRepository repository;
    private final RingitProperties properties;

    public AppSettingService(AppSettingRepository repository, RingitProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<AppSettingResponse> findAll() {
        return repository.findAllByOrderByAppNameAscCategoryAscPropertyNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> findAppNames() {
        return repository.findDistinctAppNames();
    }

    @Transactional(readOnly = true)
    public List<String> findCategories(String appName) {
        if (appName == null || appName.isBlank()) {
            return List.of();
        }
        return repository.findDistinctCategories(appName.trim());
    }

    @Transactional(readOnly = true)
    public List<String> findPropertyNames(String appName, String category) {
        if (appName == null || appName.isBlank() || category == null || category.isBlank()) {
            return List.of();
        }
        return repository.findDistinctPropertyNames(appName.trim(), category.trim());
    }

    @Transactional(readOnly = true)
    public Optional<AppSettingResponse> findByKey(String appName, String category, String propertyName) {
        if (appName == null || category == null || propertyName == null) {
            return Optional.empty();
        }
        return repository.findByAppNameAndCategoryAndPropertyName(appName.trim(), category.trim(), propertyName.trim())
                .map(this::toResponse);
    }

    public AppSettingResponse save(AppSettingRequest request) {
        String appName = normalizeRequired(request.appName(), "App name");
        String category = normalizeRequired(request.category(), "Category");
        String propertyName = normalizeRequired(request.propertyName(), "Property name");
        SettingType type = request.type();
        Integer maxLength = request.maxLength();

        if (maxLength != null && maxLength < 0) {
            throw badRequest("Max length must be zero or positive");
        }
        if (type == null) {
            throw badRequest("Type is required");
        }

        String value = request.value();
        if (value != null) {
            value = value.trim();
        }

        if (maxLength != null && value != null && value.length() > maxLength) {
            throw badRequest("Value exceeds max length");
        }

        validateByType(type, value, request.timeZone());

        AppSetting entity = request.id() != null
                ? repository.findById(request.id()).orElseGet(AppSetting::new)
                : repository.findByAppNameAndCategoryAndPropertyName(appName, category, propertyName)
                        .orElseGet(AppSetting::new);

        entity.setAppName(appName);
        entity.setCategory(category);
        entity.setPropertyName(propertyName);
        entity.setType(type);
        entity.setMaxLength(maxLength);
        entity.setValue(value);
        entity.setTimeZone(type == SettingType.DATE ? normalizeTimeZone(request.timeZone()) : null);

        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Cacheable("timeZones")
    public List<TimeZoneOption> allowedTimeZones() {
        Set<String> allowed = properties.getSupportedTimeZones().stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        List<TimeZoneOption> options = new ArrayList<>();
        for (String zoneId : allowed) {
            ZoneId zone = ZoneId.of(zoneId);
            options.add(new TimeZoneOption(zone.getId(), displayLabel(zone.getId())));
        }
        return List.copyOf(options);
    }

    @Transactional(readOnly = true)
    public Optional<AppSettingResponse> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).map(this::toResponse);
    }

    private void validateByType(SettingType type, String value, String timeZone) {
        switch (type) {
            case STRING -> {
            }
            case LONG -> {
                ensureValuePresent(value, "Long value");
                try {
                    Long.parseLong(value);
                } catch (NumberFormatException ex) {
                    throw badRequest("Value must be a valid long number");
                }
            }
            case DECIMAL -> {
                ensureValuePresent(value, "Decimal value");
                try {
                    new BigDecimal(value);
                } catch (NumberFormatException ex) {
                    throw badRequest("Value must be a valid decimal number");
                }
            }
            case BOOLEAN -> {
                ensureValuePresent(value, "Boolean value");
                String normalized = value.toLowerCase(Locale.ROOT);
                if (!normalized.equals("true") && !normalized.equals("false")) {
                    throw badRequest("Value must be true or false");
                }
            }
            case DATE -> {
                ensureValuePresent(value, "Date value");
                try {
                    LocalDateTime.parse(value, DATE_TIME_FORMATTER);
                } catch (Exception ex) {
                    throw badRequest("Date must use the browser date-time format");
                }
                normalizeTimeZone(timeZone);
            }
        }
    }

    private String normalizeTimeZone(String timeZone) {
        String candidate = timeZone == null ? "" : timeZone.trim();
        if (candidate.isBlank()) {
            candidate = properties.getDefaultTimeZone();
        }
        if (candidate == null || candidate.isBlank()) {
            throw badRequest("Time zone is required for date values");
        }
        try {
            return ZoneId.of(candidate).getId();
        } catch (Exception ex) {
            throw badRequest("Unsupported time zone: " + candidate);
        }
    }

    private void ensureValuePresent(String value, String label) {
        if (value == null || value.isBlank()) {
            throw badRequest(label + " is required");
        }
    }

    private String normalizeRequired(String value, String label) {
        if (value == null || value.isBlank()) {
            throw badRequest(label + " is required");
        }
        return value.trim();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private AppSettingResponse toResponse(AppSetting setting) {
        return new AppSettingResponse(
                setting.getId(),
                setting.getAppName(),
                setting.getCategory(),
                setting.getPropertyName(),
                setting.getType(),
                setting.getMaxLength(),
                setting.getValue(),
                setting.getTimeZone(),
                setting.getCreatedAt(),
                setting.getUpdatedAt());
    }

    private String displayLabel(String zoneId) {
        return switch (zoneId) {
            case "America/New_York" -> "USA - Eastern (America/New_York)";
            case "America/Chicago" -> "USA - Central (America/Chicago)";
            case "America/Denver" -> "USA - Mountain (America/Denver)";
            case "America/Los_Angeles" -> "USA - Pacific (America/Los_Angeles)";
            case "GMT" -> "GMT";
            case "Europe/Berlin" -> "Germany - Berlin (Europe/Berlin)";
            case "Asia/Kolkata" -> "India (Asia/Kolkata)";
            case "Asia/Singapore" -> "Singapore (Asia/Singapore)";
            case "Asia/Tokyo" -> "Tokyo, Japan (Asia/Tokyo)";
            default -> zoneId;
        };
    }
}