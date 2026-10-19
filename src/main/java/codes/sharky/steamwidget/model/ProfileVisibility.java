package codes.sharky.steamwidget.model;

/**
 * Per-request view of a profile's claim/section-visibility state, kept separate from
 * {@link ProfilePageView} (which is about the profile's data, not who's viewing it or what
 * they've chosen to show).
 */
public record ProfileVisibility(
        boolean claimed,
        boolean owner,
        boolean showNowPlaying,
        boolean showHeadlineStats,
        boolean showTopGames,
        boolean showWidgetGallery
) {
}
