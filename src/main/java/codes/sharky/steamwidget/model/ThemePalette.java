package codes.sharky.steamwidget.model;

import java.awt.Color;

/**
 * The set of colors used to render a widget's share card for a given {@link WidgetStyle}.
 * Instances are built from the entries of {@code themes.json}.
 */
public record ThemePalette(
        Color backgroundStart,
        Color backgroundEnd,
        Color primaryGlow,
        Color secondaryGlow,
        Color cardBackground,
        Color cardBorder,
        Color accent,
        Color text,
        Color muted,
        Color divider,
        Color footer
) {
}
