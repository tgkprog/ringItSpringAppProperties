package sel2in.web;

import java.util.List;

import sel2in.config.RingitProperties;
import sel2in.settings.SettingType;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private final RingitProperties properties;

    public PageController(RingitProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("defaultTimeZone", properties.getDefaultTimeZone());
        model.addAttribute("allowedTypes", List.of(SettingType.STRING, SettingType.LONG, SettingType.DECIMAL, SettingType.BOOLEAN, SettingType.DATE));
        return "index";
    }
}