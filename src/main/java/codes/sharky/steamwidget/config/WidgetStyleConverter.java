package codes.sharky.steamwidget.config;

import codes.sharky.steamwidget.model.WidgetStyle;
import codes.sharky.steamwidget.service.ThemeService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Binds the {@code style} request parameter to a {@link WidgetStyle}, rejecting ids that have no
 * matching entry in {@code themes.json} so an unknown style still fails fast with a 400 instead
 * of surfacing as a rendering error.
 */
@Component
public class WidgetStyleConverter implements Converter<String, WidgetStyle> {

    private final ThemeService themeService;

    public WidgetStyleConverter(ThemeService themeService) {
        this.themeService = themeService;
    }

    @Override
    public WidgetStyle convert(String source) {
        WidgetStyle style = WidgetStyle.of(source);
        if (!themeService.isKnownStyle(style)) {
            throw new IllegalArgumentException(
                    "Unknown widget style '" + source + "'. Available styles: " + themeService.getAvailableStyleIds());
        }
        return style;
    }
}
