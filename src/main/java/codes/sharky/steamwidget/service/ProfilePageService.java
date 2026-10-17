package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.config.CacheConfig;
import codes.sharky.steamwidget.entity.TrackingProfileDate;
import codes.sharky.steamwidget.entity.TrackingProfileInsightsActivity;
import codes.sharky.steamwidget.entity.TrackingProfileInsightsPlaytime;
import codes.sharky.steamwidget.model.GameSummary;
import codes.sharky.steamwidget.model.HeatmapDay;
import codes.sharky.steamwidget.model.ProfilePageView;
import codes.sharky.steamwidget.model.TrackedStats;
import codes.sharky.steamwidget.model.WidgetGalleryEntry;
import codes.sharky.steamwidget.utils.PersonaStateFormatter;
import com.lukaspradel.steamapi.data.json.playersummaries.Player;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Backs the public, server-rendered /u/{id} profile page: resolves vanity URLs, fetches and
 * caches the Steam Web API data the page needs, and assembles the final view model.
 */
@Service
public class ProfilePageService {

    private final SteamWebAPIService steamWebAPIService;
    private final ProfileService profileService;
    private final TrackingProfileService trackingProfileService;
    private final ThemeService themeService;

    public ProfilePageService(SteamWebAPIService steamWebAPIService,
                               ProfileService profileService,
                               TrackingProfileService trackingProfileService,
                               ThemeService themeService) {
        this.steamWebAPIService = steamWebAPIService;
        this.profileService = profileService;
        this.trackingProfileService = trackingProfileService;
        this.themeService = themeService;
    }

    private static final int TOP_GAMES_LIMIT = 6;
    private static final int HEATMAP_DAYS = 365;
    private static final int WIDGET_GALLERY_LIMIT = 4;
    private static final String BASE_URL = "https://steam-widget.com";

    /** Data sourced live from the Steam Web API — available for any public profile. */
    record LiveSection(
            String displayName,
            String avatarUrl,
            String countryFlag,
            String statusText,
            long gamesOwned,
            List<GameSummary> topGamesAllTime,
            List<GameSummary> topGamesRecent
    ) {
    }

    LiveSection buildLiveSection(Player player,
                                 List<com.lukaspradel.steamapi.data.json.ownedgames.Game> ownedGames,
                                 List<com.lukaspradel.steamapi.data.json.recentlyplayedgames.Game> recentGames,
                                 boolean showNowPlaying) {
        String displayName = player.getPersonaname() == null || player.getPersonaname().isBlank()
                ? "Steam profile" : player.getPersonaname();

        List<GameSummary> topAllTime = ownedGames.stream()
                .limit(TOP_GAMES_LIMIT)
                .map(game -> new GameSummary(
                        game.getName(),
                        gameIconUrl(game.getAppid(), game.getImgIconUrl(), game.getImgLogoUrl()),
                        formatMinutes(game.getPlaytimeForever())))
                .toList();

        List<GameSummary> topRecent = recentGames.stream()
                .limit(TOP_GAMES_LIMIT)
                .map(game -> new GameSummary(
                        game.getName(),
                        gameIconUrl(game.getAppid(), game.getImgIconUrl(), game.getImgLogoUrl()),
                        formatMinutes(game.getPlaytime2weeks())))
                .toList();

        return new LiveSection(displayName, player.getAvatarfull(), countryFlagEmoji(player.getLoccountrycode()),
                buildStatusText(player, showNowPlaying), ownedGames.size(), topAllTime, topRecent);
    }

    private static String buildStatusText(Player player, boolean showNowPlaying) {
        Object game = player.getAdditionalProperties() == null ? null : player.getAdditionalProperties().get("gameextrainfo");
        if (showNowPlaying && game != null && !game.toString().isBlank()) {
            return "Now playing " + game;
        }
        return PersonaStateFormatter.formatPersonaState(player.getPersonastate());
    }

    private static String gameIconUrl(Long appId, String iconUrl, String logoUrl) {
        String hash = (iconUrl == null || iconUrl.isBlank()) ? logoUrl : iconUrl;
        return "https://media.steampowered.com/steamcommunity/public/images/apps/" + appId + "/" + hash + ".jpg";
    }

    private static String formatMinutes(Long minutes) {
        long safe = minutes == null ? 0L : minutes;
        return (safe / 60) + "h " + (safe % 60) + "m";
    }

    /** Converts a 2-letter ISO country code to its flag emoji (regional indicator pair), or null. */
    private static String countryFlagEmoji(String countryCode) {
        if (countryCode == null || countryCode.length() != 2) {
            return null;
        }
        String upper = countryCode.toUpperCase(Locale.ROOT);
        int base = 0x1F1E6 - 'A';
        return new StringBuilder()
                .appendCodePoint(base + upper.charAt(0))
                .appendCodePoint(base + upper.charAt(1))
                .toString();
    }

    /** Resolves a vanity URL or steamid64 to a steamid64, or null if it can't be resolved. */
    @Cacheable(cacheNames = CacheConfig.PROFILE_PAGE_VANITY_RESOLVE, key = "#idOrVanity")
    public String resolveVanity(String idOrVanity) {
        try {
            return steamWebAPIService.resolveSteamId(idOrVanity);
        } catch (Exception e) {
            return null;
        }
    }

    @Cacheable(cacheNames = CacheConfig.PROFILE_PAGE_PLAYER, key = "#steamId")
    public Player getCachedPlayer(String steamId) {
        return steamWebAPIService.getUserBySteamId(steamId);
    }

    @Cacheable(cacheNames = CacheConfig.PROFILE_PAGE_TOP_OWNED_GAMES, key = "#steamId")
    public List<com.lukaspradel.steamapi.data.json.ownedgames.Game> getCachedTopOwnedGames(String steamId) {
        try {
            return steamWebAPIService.getTopOwnedGames(steamId);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Cacheable(cacheNames = CacheConfig.PROFILE_PAGE_TOP_RECENT_GAMES, key = "#steamId")
    public List<com.lukaspradel.steamapi.data.json.recentlyplayedgames.Game> getCachedTopRecentlyPlayedGames(String steamId) {
        try {
            return steamWebAPIService.getTopRecentlyPlayedGames(steamId);
        } catch (Exception e) {
            return List.of();
        }
    }

    /** True when the player's Steam profile is public (communityvisibilitystate == 3). */
    public static boolean isPublicProfile(Player player) {
        return player != null
                && player.getSteamid() != null
                && player.getCommunityvisibilitystate() != null
                && player.getCommunityvisibilitystate() == 3L;
    }

    /** Headline stats and heatmap for tracked profiles, or null if tracking isn't enabled. */
    @Cacheable(cacheNames = CacheConfig.PROFILE_PAGE_TRACKED_STATS, key = "#steamId")
    TrackedStats buildTrackedStats(String steamId) {
        if (!profileService.profileTrackingActive(steamId)) {
            return null;
        }

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(HEATMAP_DAYS - 1L);

        TrackingProfileInsightsPlaytime playtime = trackingProfileService.getInsightsPlaytime(steamId).getBody();
        TrackingProfileInsightsActivity activity = trackingProfileService.getInsightsActivity(steamId).getBody();
        List<TrackingProfileDate> rows = trackingProfileService.getTrackingProfileDate(steamId, start, end);

        long totalHours = playtime != null && playtime.getAlltimeHours() != null ? playtime.getAlltimeHours() : 0L;
        long gamesThisYear = playtime != null && playtime.getUniqueGamesThisYear() != null ? playtime.getUniqueGamesThisYear() : 0L;
        long currentStreakDays = activity != null && activity.getCurrentStreakDays() != null ? activity.getCurrentStreakDays() : 0L;

        Map<LocalDate, Long> minutesByDate = new TreeMap<>();
        for (TrackingProfileDate row : rows) {
            long minutes = (row.getPlaytimeHours() == null ? 0L : row.getPlaytimeHours() * 60)
                    + (row.getPlaytimeMinutes() == null ? 0L : row.getPlaytimeMinutes());
            minutesByDate.merge(row.getId().getDate(), minutes, Long::sum);
        }
        // Pad every date in the requested window to 0 minutes so the heatmap is always a full,
        // contiguous HEATMAP_DAYS-long strip, regardless of how short or stale the underlying
        // tracking history is. getTrackingProfileDate only gap-fills between the profile's own
        // earliest/latest tracked rows, not the full start..end window, so this padding happens
        // here rather than in TrackingProfileService (which other pages depend on as-is).
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            minutesByDate.putIfAbsent(date, 0L);
        }
        List<HeatmapDay> heatmap = minutesByDate.entrySet().stream()
                .map(entry -> new HeatmapDay(entry.getKey(), entry.getValue()))
                .toList();

        return new TrackedStats(totalHours, gamesThisYear, currentStreakDays, heatmap);
    }

    ProfilePageView assembleView(String steamId64, LiveSection live, TrackedStats trackedStats) {
        String title = trackedStats != null
                ? live.displayName() + "'s Steam Stats – " + formatWithCommas(trackedStats.totalHours()) + " hours, " + live.gamesOwned() + " games"
                : live.displayName() + "'s Steam Profile";

        String metaDescription = trackedStats != null
                ? live.displayName() + " has played " + formatWithCommas(trackedStats.totalHours()) + " hours across " + live.gamesOwned()
                    + " games on Steam, with a " + trackedStats.currentStreakDays() + "-day current streak."
                : live.displayName() + "'s public Steam profile on steam-widget.";

        List<WidgetGalleryEntry> gallery = themeService.getThemeSummaries().stream()
                .limit(WIDGET_GALLERY_LIMIT)
                .map(summary -> new WidgetGalleryEntry(
                        summary.id(),
                        summary.label(),
                        "/widget/img?id=" + steamId64 + "&style=" + summary.id() + "&purpose=ProfilePage"))
                .toList();

        return new ProfilePageView(
                steamId64,
                live.displayName(),
                live.avatarUrl(),
                live.countryFlag(),
                live.statusText(),
                live.gamesOwned(),
                live.topGamesAllTime(),
                live.topGamesRecent(),
                trackedStats,
                gallery,
                title,
                metaDescription,
                BASE_URL + "/u/" + steamId64
        );
    }

    private static String formatWithCommas(long value) {
        return String.format("%,d", value);
    }

    /** Builds the full page view: live Steam data plus tracked-only stats, if any. */
    public ProfilePageView buildView(String steamId64, Player player,
                                      List<com.lukaspradel.steamapi.data.json.ownedgames.Game> ownedGames,
                                      List<com.lukaspradel.steamapi.data.json.recentlyplayedgames.Game> recentGames,
                                      boolean showNowPlaying) {
        LiveSection live = buildLiveSection(player, ownedGames, recentGames, showNowPlaying);
        TrackedStats trackedStats = buildTrackedStats(steamId64);
        return assembleView(steamId64, live, trackedStats);
    }
}
