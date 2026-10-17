package codes.sharky.steamwidget.model;

import java.util.List;

/** The full view model rendered by templates/profile.html. */
public record ProfilePageView(
        String steamId64,
        String displayName,
        String avatarUrl,
        String countryFlag,
        String statusText,
        long gamesOwned,
        List<GameSummary> topGamesAllTime,
        List<GameSummary> topGamesRecent,
        TrackedStats trackedStats,
        List<WidgetGalleryEntry> widgetGalleryEntries,
        String title,
        String metaDescription,
        String canonicalUrl
) {
    public boolean isTracked() {
        return trackedStats != null;
    }
}
