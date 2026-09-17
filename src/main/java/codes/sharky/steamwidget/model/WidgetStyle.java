package codes.sharky.steamwidget.model;

import java.util.Locale;
import java.util.Objects;

/**
 * Identifies a widget theme by id. Unlike a Java enum, the set of valid ids is not fixed at
 * compile time: it is whatever {@code themes.json} defines, so new themes can be added there
 * without a code change. {@link codes.sharky.steamwidget.service.ThemeService} is the source of
 * truth for which ids actually resolve to a palette.
 */
public final class WidgetStyle {

    /** Fallback style used when none is requested. Must have a matching entry in themes.json. */
    public static final WidgetStyle STEAM = new WidgetStyle("STEAM");

    private final String id;

    private WidgetStyle(String id) {
        this.id = id;
    }

    public static WidgetStyle of(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Widget style id must not be blank");
        }
        return new WidgetStyle(id.trim().toUpperCase(Locale.ROOT));
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WidgetStyle other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
