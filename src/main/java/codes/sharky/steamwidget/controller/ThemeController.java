package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.model.ThemeSummary;
import codes.sharky.steamwidget.service.ThemeService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Exposes the widget themes defined in {@code themes.json} so the frontend can build its style
 * picker without hardcoding the list of styles.
 */
@Controller
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping(value = "/api/widget/styles", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<ThemeSummary> getWidgetStyles() {
        return themeService.getThemeSummaries();
    }
}
