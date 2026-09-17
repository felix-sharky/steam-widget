package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.model.ThemePalette;
import codes.sharky.steamwidget.model.ThemeSummary;
import codes.sharky.steamwidget.model.WidgetStyle;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads the widget theme palettes defined in {@code themes.json} (on the classpath) and serves
 * them by {@link WidgetStyle}. This is the single source of truth for which styles exist -
 * adding a theme only requires adding an entry to that file.
 */
@Service
public class ThemeService {

    private static final String THEMES_RESOURCE = "themes.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, ThemePalette> palettesById;

    /** Theme ids and display labels, in the order they appear in themes.json. */
    private List<ThemeSummary> themeSummaries;

    @PostConstruct
    void loadThemes() {
        try (InputStream inputStream = new ClassPathResource(THEMES_RESOURCE).getInputStream()) {
            ThemesFile themesFile = objectMapper.readValue(inputStream, ThemesFile.class);
            Map<String, ThemePalette> palettes = new HashMap<>();
            List<ThemeSummary> summaries = new ArrayList<>();
            for (JsonThemePalette theme : themesFile.themes()) {
                String id = theme.id().trim().toUpperCase(Locale.ROOT);
                palettes.put(id, theme.toPalette());
                summaries.add(new ThemeSummary(id, theme.label()));
            }
            this.palettesById = Map.copyOf(palettes);
            this.themeSummaries = List.copyOf(summaries);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + THEMES_RESOURCE, e);
        }
    }

    /**
     * @throws IllegalArgumentException if {@code style} has no matching entry in themes.json.
     */
    public ThemePalette getPalette(WidgetStyle style) {
        ThemePalette palette = palettesById.get(style.id());
        if (palette == null) {
            throw new IllegalArgumentException(
                    "Unknown widget style '" + style + "'. Available styles: " + getAvailableStyleIds());
        }
        return palette;
    }

    public boolean isKnownStyle(WidgetStyle style) {
        return palettesById.containsKey(style.id());
    }

    public List<String> getAvailableStyleIds() {
        return List.copyOf(palettesById.keySet());
    }

    /** Theme ids and display labels, in the order they appear in themes.json - for the frontend style picker. */
    public List<ThemeSummary> getThemeSummaries() {
        return themeSummaries;
    }

    private record ThemesFile(List<JsonThemePalette> themes) {
    }

    private record JsonThemePalette(
            String id,
            String label,
            String backgroundStart,
            String backgroundEnd,
            String primaryGlow,
            String secondaryGlow,
            String cardBackground,
            String cardBorder,
            String accent,
            String text,
            String muted,
            String divider,
            String footer
    ) {
        ThemePalette toPalette() {
            return new ThemePalette(
                    decodeColor(backgroundStart),
                    decodeColor(backgroundEnd),
                    decodeColor(primaryGlow),
                    decodeColor(secondaryGlow),
                    decodeColor(cardBackground),
                    decodeColor(cardBorder),
                    decodeColor(accent),
                    decodeColor(text),
                    decodeColor(muted),
                    decodeColor(divider),
                    decodeColor(footer)
            );
        }
    }

    /** Parses a hex color string, either 6-digit ({@code #RRGGBB}) or 8-digit with alpha ({@code #RRGGBBAA}). */
    private static Color decodeColor(String hex) {
        String value = hex.startsWith("#") ? hex.substring(1) : hex;
        if (value.length() == 8) {
            int r = Integer.parseInt(value.substring(0, 2), 16);
            int g = Integer.parseInt(value.substring(2, 4), 16);
            int b = Integer.parseInt(value.substring(4, 6), 16);
            int a = Integer.parseInt(value.substring(6, 8), 16);
            return new Color(r, g, b, a);
        }
        return Color.decode(hex);
    }
}
