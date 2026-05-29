package sel2in.web;

import java.util.List;

import jakarta.validation.Valid;
import sel2in.settings.AppSettingRequest;
import sel2in.settings.AppSettingResponse;
import sel2in.settings.AppSettingService;
import sel2in.settings.TimeZoneOption;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AppSettingApiController {

    private final AppSettingService service;

    public AppSettingApiController(AppSettingService service) {
        this.service = service;
    }

    @GetMapping("/settings")
    public List<AppSettingResponse> settings() {
        return service.findAll();
    }

    @GetMapping("/settings/{id}")
    public ResponseEntity<AppSettingResponse> settingById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/settings/lookup")
    public ResponseEntity<AppSettingResponse> lookup(@RequestParam String appName,
                                                     @RequestParam String category,
                                                     @RequestParam String propertyName) {
        return service.findByKey(appName, category, propertyName)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/settings")
    public AppSettingResponse save(@Valid @RequestBody AppSettingRequest request) {
        return service.save(request);
    }

    @DeleteMapping("/settings/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/apps")
    public List<String> appNames() {
        return service.findAppNames();
    }

    @GetMapping("/apps/{appName}/categories")
    public List<String> categories(@PathVariable String appName) {
        return service.findCategories(appName);
    }

    @GetMapping("/apps/{appName}/categories/{category}/names")
    public List<String> propertyNames(@PathVariable String appName, @PathVariable String category) {
        return service.findPropertyNames(appName, category);
    }

    @GetMapping("/time-zones")
    public List<TimeZoneOption> timeZones() {
        return service.allowedTimeZones();
    }
}