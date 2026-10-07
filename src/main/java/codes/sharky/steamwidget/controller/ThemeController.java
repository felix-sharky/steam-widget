package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.model.ThemeColors;
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

    /**
     * Returns the full theme color palettes (as hex strings) so pages that render their own
     * themed UI client-side, such as the Wrapped story generator, can match the widget themes.
     */
    @GetMapping(value = "/api/widget/themes", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<ThemeColors> getWidgetThemeColors() {
        return themeService.getThemeColors();
    }
}
