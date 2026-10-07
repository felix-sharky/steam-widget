package codes.sharky.steamwidget.model;

/**
 * Theme colors as hex strings, exposed to the frontend for client-side rendering (e.g. the
 * Instagram-story-sized "Wrapped" generator) where a {@link ThemePalette}'s AWT {@code Color}
 * values aren't usable directly.
 */
public record ThemeColors(
        String id,
        String label,
        String backgroundStart,
        String backgroundEnd,
        String accent,
        String text,
        String muted
) {
}
