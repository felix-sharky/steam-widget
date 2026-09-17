package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.utils.IPUtils;
import com.google.common.base.Strings;
import com.lukaspradel.steamapi.core.exception.SteamApiException;
import com.lukaspradel.steamapi.data.json.ownedgames.GetOwnedGames;
import com.lukaspradel.steamapi.data.json.playersummaries.GetPlayerSummaries;
import com.lukaspradel.steamapi.data.json.playersummaries.Player;
import com.lukaspradel.steamapi.data.json.recentlyplayedgames.GetRecentlyPlayedGames;
import com.lukaspradel.steamapi.data.json.resolvevanityurl.ResolveVanityURL;
import com.lukaspradel.steamapi.webapi.request.GetOwnedGamesRequest;
import com.lukaspradel.steamapi.webapi.request.GetPlayerSummariesRequest;
import com.lukaspradel.steamapi.webapi.request.GetRecentlyPlayedGamesRequest;
import com.lukaspradel.steamapi.webapi.request.ResolveVanityUrlRequest;
import codes.sharky.steamwidget.component.SteamWebAPI;
import codes.sharky.steamwidget.entity.Hit;
import codes.sharky.steamwidget.entity.Profile;
import codes.sharky.steamwidget.entity.TrackingProfileInsightsActivity;
import codes.sharky.steamwidget.entity.TrackingProfileInsightsGame;
import codes.sharky.steamwidget.entity.TrackingProfileInsightsPlaytime;
import codes.sharky.steamwidget.model.InsightCategory;
import codes.sharky.steamwidget.model.ShowedGames;
import codes.sharky.steamwidget.model.ThemePalette;
import codes.sharky.steamwidget.model.WidgetStyle;
import codes.sharky.steamwidget.repository.HitRepository;
import codes.sharky.steamwidget.repository.ProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.imgscalr.Scalr;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for managing Steam widget functionalities.
 * This class provides methods for retrieving player information from Steam, generating widget images,
 * and managing profile and hit data in the application's database.
 * <p>
 * It interacts with the Steam Web API through the {@link SteamWebAPI} component and utilizes Spring's
 * dependency injection to access repositories for persisting data.
 */
@Service
public class SteamWidgetService {

    private final SteamWebAPIService steamWebAPIService;

    private final ProfileService profileService;
    private final TrackingProfileService trackingProfileService;
    private final ThemeService themeService;

    public SteamWidgetService(SteamWebAPIService steamWebAPIService, ProfileService profileService, TrackingProfileService trackingProfileService, ThemeService themeService) {
        this.steamWebAPIService = steamWebAPIService;
        this.profileService = profileService;
        this.trackingProfileService = trackingProfileService;
        this.themeService = themeService;
    }

    /**
     * Retrieves a {@link Player} object by their Steam ID. If the Steam ID is not in the correct format,
     * it attempts to resolve it. This method also logs the access attempt by adding a hit to the profile
     * associated with the Steam ID.
     *
     * @param steamId The Steam ID of the user, which can be either a numeric ID or a vanity URL.
     * @param purpose The reason for accessing the user's Steam information.
     * @param ip      The IP address from which the request originated.
     * @return A {@link Player} object containing the user's Steam profile information. Returns an empty
     * {@link Player} object if no information could be retrieved.
     * @throws SteamApiException If there is an issue with accessing the Steam Web API.
     */
    public Player getUserBySteamId(String steamId, String purpose, String ip) throws SteamApiException {
        Player player = steamWebAPIService.getUserBySteamId(steamId);
        if (!Strings.isNullOrEmpty(player.getSteamid())) {
            profileService.addHitToProfile(player.getSteamid(), player.getPersonaname(), purpose, ip, LocalDateTime.now());
        }

        return player;
    }

    /**
     * Generates a widget image for a given Steam ID, purpose, and IP address.
     * This method first retrieves the player's information using their Steam ID,
     * then creates a new BufferedImage and draws the base widget, player's profile image,
     * and user information onto it.
     *
     * @param steamId The Steam ID of the user for whom the widget is being generated.
     * @param showGames The type of games to be shown on the widget (e.g., top recent games, top total games, recent games).
     * @param recentGamesCount The number of recent games to be displayed on the widget.
     * @param showPlayingRightNow A boolean indicating whether to show the game the user is currently playing.
     * @param purpose The reason for accessing the user's Steam information, used for logging.
     * @param request The HttpServletRequest object, used here to get the client's IP address.
     * @return A BufferedImage object representing the generated widget with the player's information.
     * @throws SteamApiException If there is an issue with accessing the Steam Web API.
     */
    public BufferedImage generateWidgetImage(String steamId, @NotNull ShowedGames showGames, int recentGamesCount, boolean showPlayingRightNow, String purpose, @NotNull HttpServletRequest request) throws SteamApiException {
        String ip = IPUtils.getIPAddress(request);
        return generateWidgetImage(steamId, showGames, recentGamesCount, showPlayingRightNow, purpose, ip);
    }

    public BufferedImage generateWidgetImage(String steamId, @NotNull ShowedGames showGames, int recentGamesCount, @NotNull InsightCategory insightCategory, boolean showPlayingRightNow, String purpose, @NotNull HttpServletRequest request) throws SteamApiException {
        String ip = IPUtils.getIPAddress(request);
        return generateWidgetImage(steamId, showGames, recentGamesCount, insightCategory, showPlayingRightNow, purpose, ip);
    }

    public BufferedImage generateWidgetImage(String steamId, @NotNull ShowedGames showGames, int recentGamesCount, @NotNull InsightCategory insightCategory, @NotNull WidgetStyle style, boolean showPlayingRightNow, String purpose, @NotNull HttpServletRequest request) throws SteamApiException {
        String ip = IPUtils.getIPAddress(request);
        return generateWidgetImage(steamId, showGames, recentGamesCount, insightCategory, style, showPlayingRightNow, purpose, ip);
    }

    public BufferedImage generateWidgetImage(String steamId, @NotNull ShowedGames showGames, int recentGamesCount, @NotNull InsightCategory insightCategory, List<String> customCards, @NotNull WidgetStyle style, boolean showPlayingRightNow, String purpose, @NotNull HttpServletRequest request) throws SteamApiException {
        String ip = IPUtils.getIPAddress(request);
        return generateWidgetImage(steamId, showGames, recentGamesCount, insightCategory, customCards, style, showPlayingRightNow, purpose, ip);
    }

    /**
     * Generates a widget image for a given Steam ID, purpose, and IP address.
     * This method first retrieves the player's information using their Steam ID,
     * then creates a new BufferedImage and draws the base widget, player's profile image,
     * and user information onto it.
     *
     * @param steamId The Steam ID of the user for whom the widget is being generated.
     * @param showGames The type of games to be shown on the widget (e.g., top recent games, top total games, recent games).
     * @param recentGamesCount The number of recent games to be displayed on the widget.
     * @param showPlayingRightNow A boolean indicating whether to show the game the user is currently playing.
     * @param purpose The reason for accessing the user's Steam information, used for logging.
     * @param ip The IP address from which the request originated, used for logging.
     * @return A BufferedImage object representing the generated widget with the player's information.
     * @throws SteamApiException If there is an issue with accessing the Steam Web API.
     */
    public BufferedImage generateWidgetImage(String steamId, @NotNull ShowedGames showGames, int recentGamesCount, boolean showPlayingRightNow, String purpose, String ip) throws SteamApiException {
        return generateWidgetImage(steamId, showGames, recentGamesCount, InsightCategory.NONE, showPlayingRightNow, purpose, ip);
    }

    public BufferedImage generateWidgetImage(String steamId, @NotNull ShowedGames showGames, int recentGamesCount, @NotNull InsightCategory insightCategory, boolean showPlayingRightNow, String purpose, String ip) throws SteamApiException {
        return generateWidgetImage(steamId, showGames, recentGamesCount, insightCategory, WidgetStyle.STEAM, showPlayingRightNow, purpose, ip);
    }

    public BufferedImage generateWidgetImage(String steamId, @NotNull ShowedGames showGames, int recentGamesCount, @NotNull InsightCategory insightCategory, @NotNull WidgetStyle style, boolean showPlayingRightNow, String purpose, String ip) throws SteamApiException {
        return generateWidgetImage(steamId, showGames, recentGamesCount, insightCategory, List.of(), style, showPlayingRightNow, purpose, ip);
    }

    public BufferedImage generateWidgetImage(String steamId, @NotNull ShowedGames showGames, int recentGamesCount, @NotNull InsightCategory insightCategory, List<String> customCards, @NotNull WidgetStyle style, boolean showPlayingRightNow, String purpose, String ip) throws SteamApiException {
        Player player = getUserBySteamId(steamId, purpose, ip);
        ThemePalette palette = themeService.getPalette(style);

        boolean showInsights = insightCategory != InsightCategory.NONE;
        List<InsightCard> insightCards = showInsights && player.getSteamid() != null ? getInsightCards(player.getSteamid(), insightCategory, customCards) : List.of();
        if (showInsights) {
            String emptyMessage = insightCategory == InsightCategory.CUSTOM ? "No custom cards configured for this widget." : "No tracked insights found for this profile.";
            return generateInsightWidgetImage(player, insightCards, showPlayingRightNow, palette, emptyMessage);
        }

        List<Object> games = switch (showGames) {
            case TOP_GAMES_RECENT -> {
                List<Object> objects = new ArrayList<>(player.getSteamid() != null ? steamWebAPIService.getTopRecentlyPlayedGames(player.getSteamid()) : new ArrayList<>());
                yield objects.stream().limit(recentGamesCount).toList();
            }
            case TOP_GAMES_TOTAL -> {
                List<Object> objects = new ArrayList<>(player.getSteamid() != null ? steamWebAPIService.getTopOwnedGames(player.getSteamid()) : new ArrayList<>());
                yield objects.stream().limit(recentGamesCount).toList();
            }
            case RECENT_GAMES -> {
                List<Object> objects = new ArrayList<>(player.getSteamid() != null ? steamWebAPIService.getRecentlyPlayedGames(player.getSteamid()) : new ArrayList<>());
                yield objects.stream().limit(recentGamesCount).toList();
            }
            default -> new ArrayList<>();
        };

        if (showGames == ShowedGames.NONE) {
            return generateProfileWidgetImage(player, showPlayingRightNow, palette);
        }

        return generateGameWidgetImage(player, games, showPlayingRightNow, palette);
    }

    private List<InsightCard> getInsightCards(String steamId, InsightCategory insightCategory, List<String> customCards) {
        return switch (insightCategory) {
            case ACTIVITY -> getActivityInsightCards(steamId);
            case PLAYTIME -> getPlaytimeInsightCards(steamId);
            case GAMES -> getGameInsightCards(steamId);
            case CUSTOM -> getCustomInsightCards(steamId, customCards);
            case NONE -> List.of();
        };
    }

    private List<InsightCard> getCustomInsightCards(String steamId, List<String> customCards) {
        if (customCards == null || customCards.isEmpty()) {
            return List.of();
        }
        Map<String, InsightCard> availableCards = getAvailableInsightCards(steamId);
        return customCards.stream()
                .limit(6)
                .map(this::normalizeCustomInsightKey)
                .map(availableCards::get)
                .filter((card) -> card != null)
                .toList();
    }

    private Map<String, InsightCard> getAvailableInsightCards(String steamId) {
        Map<String, InsightCard> cards = new LinkedHashMap<>();
        putInsightCards(cards, List.of(
                "ACTIVITY_CURRENT_STREAK",
                "ACTIVITY_LONGEST_STREAK_YEAR",
                "ACTIVITY_LONGEST_STREAK_ALLTIME",
                "ACTIVITY_MOST_ACTIVE_DAY",
                "ACTIVITY_MOST_ACTIVE_MONTH"
        ), getActivityInsightCards(steamId));
        putInsightCards(cards, List.of(
                "PLAYTIME_ALLTIME",
                "PLAYTIME_YEAR",
                "PLAYTIME_AVG_DAILY",
                "PLAYTIME_BEST_DAY",
                "PLAYTIME_GAMES_YEAR",
                "PLAYTIME_GAMES_ALLTIME"
        ), getPlaytimeInsightCards(steamId));
        putInsightCards(cards, List.of(
                "GAMES_MOST_PLAYED_ALLTIME",
                "GAMES_MOST_PLAYED_YEAR",
                "GAMES_LAST_PLAYED",
                "GAMES_STREAK_ALLTIME",
                "GAMES_STREAK_YEAR"
        ), getGameInsightCards(steamId));
        return cards;
    }

    private void putInsightCards(Map<String, InsightCard> target, List<String> keys, List<InsightCard> cards) {
        for (int i = 0; i < keys.size() && i < cards.size(); i++) {
            target.put(keys.get(i), cards.get(i));
        }
    }

    private String normalizeCustomInsightKey(String rawKey) {
        return rawKey == null ? "" : rawKey.trim().toUpperCase();
    }

    private List<InsightCard> getActivityInsightCards(String steamId) {
        TrackingProfileInsightsActivity insights = trackingProfileService.getInsightsActivity(steamId).getBody();
        if (insights == null) {
            return List.of();
        }
        return List.of(
                new InsightCard("Current streak", formatDays(insights.getCurrentStreakDays()), formatDateRange(insights.getCurrentStreakStart(), insights.getCurrentStreakEnd())),
                new InsightCard("Longest streak (year)", formatDays(insights.getLongestStreakYearDays()), formatDateRange(insights.getLongestStreakYearStart(), insights.getLongestStreakYearEnd())),
                new InsightCard("Longest streak (all-time)", formatDays(insights.getLongestStreakAlltimeDays()), formatDateRange(insights.getLongestStreakAlltimeStart(), insights.getLongestStreakAlltimeEnd())),
                new InsightCard("Most active day", formatText(insights.getMostActiveDow()), formatCount(insights.getMostActiveDowCount(), "session this year", "sessions this year")),
                new InsightCard("Most active month", formatText(insights.getMostActiveMonth()), formatCount(insights.getMostActiveMonthDays(), "day played", "days played"))
        );
    }

    private List<InsightCard> getPlaytimeInsightCards(String steamId) {
        TrackingProfileInsightsPlaytime insights = trackingProfileService.getInsightsPlaytime(steamId).getBody();
        if (insights == null) {
            return List.of();
        }
        return List.of(
                new InsightCard("All-time playtime", formatDuration(insights.getAlltimeHours(), insights.getAlltimeMinutes()), ""),
                new InsightCard("This year", formatDuration(insights.getYearHours(), insights.getYearMinutes()), ""),
                new InsightCard("Avg daily (year)", formatDuration(insights.getAvgDailyHours(), insights.getAvgDailyMinutes()), ""),
                new InsightCard("Best single day", formatDuration(insights.getBestDayHours(), insights.getBestDayMinutes()), formatDate(insights.getBestDayDate())),
                new InsightCard("Games this year", formatNumber(insights.getUniqueGamesThisYear()), ""),
                new InsightCard("Games all-time", formatNumber(insights.getUniqueGamesAlltime()), "")
        );
    }

    private List<InsightCard> getGameInsightCards(String steamId) {
        TrackingProfileInsightsGame insights = trackingProfileService.getInsightsGame(steamId).getBody();
        if (insights == null) {
            return List.of();
        }
        return List.of(
                new InsightCard("Most played (all-time)", formatText(insights.getMostPlayedAlltimeGame()), formatDuration(insights.getMostPlayedAlltimeHours(), insights.getMostPlayedAlltimeMinutes())),
                new InsightCard("Most played (year)", formatText(insights.getMostPlayedYearGame()), formatDuration(insights.getMostPlayedYearHours(), insights.getMostPlayedYearMinutes())),
                new InsightCard("Last played", formatText(insights.getLastPlayedGame()), formatDate(insights.getLastPlayedDate())),
                new InsightCard("Game streak (all-time)", formatText(insights.getLongestStreakAlltimeGame()), formatDaysWithRange(insights.getLongestStreakAlltimeDays(), insights.getLongestStreakAlltimeStart(), insights.getLongestStreakAlltimeEnd())),
                new InsightCard("Game streak (year)", formatText(insights.getLongestStreakYearGame()), formatDaysWithRange(insights.getLongestStreakYearDays(), insights.getLongestStreakYearStart(), insights.getLongestStreakYearEnd()))
        );
    }

    private BufferedImage generateInsightWidgetImage(Player player, List<InsightCard> cards, boolean showPlayingRightNow, ThemePalette palette, String emptyMessage) {
        BufferedImage image = new BufferedImage(1800, 1200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = drawShareHeader(image, player, showPlayingRightNow, palette);

        if (cards.isEmpty()) {
            g.setFont(new Font("ARIAL", Font.PLAIN, 42));
            g.setColor(palette.muted());
            g.drawString(emptyMessage, 80, 420);
            drawShareFooter(g, image, palette);
            g.dispose();
            return image;
        }

        int cardWidth = 790;
        int cardHeight = 240;
        int gapX = 60;
        int gapY = 36;
        int startX = 80;
        int startY = 355;
        for (int i = 0; i < cards.size(); i++) {
            InsightCard card = cards.get(i);
            int col = i % 2;
            int row = i / 2;
            int x = startX + col * (cardWidth + gapX);
            int y = startY + row * (cardHeight + gapY);
            drawShareInsightCard(g, card, x, y, cardWidth, cardHeight, palette);
        }

        drawShareFooter(g, image, palette);

        g.dispose();
        return image;
    }

    private BufferedImage generateGameWidgetImage(Player player, List<Object> games, boolean showPlayingRightNow, ThemePalette palette) {
        List<GameCard> cards = getGameCards(games);
        int rows = cards.isEmpty() ? 1 : (int) Math.ceil(cards.size() / 2.0);
        int height = Math.max(1200, 355 + (rows * 240) + ((rows - 1) * 36) + 90);
        BufferedImage image = new BufferedImage(1800, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = drawShareHeader(image, player, showPlayingRightNow, palette);

        if (cards.isEmpty()) {
            g.setFont(new Font("ARIAL", Font.PLAIN, 42));
            g.setColor(palette.muted());
            g.drawString("No games selected for this widget.", 80, 420);
            drawShareFooter(g, image, palette);
            g.dispose();
            return image;
        }

        int cardWidth = 790;
        int cardHeight = 240;
        int gapX = 60;
        int gapY = 36;
        int startX = 80;
        int startY = 355;
        for (int i = 0; i < cards.size(); i++) {
            GameCard card = cards.get(i);
            int col = i % 2;
            int row = i / 2;
            int x = startX + col * (cardWidth + gapX);
            int y = startY + row * (cardHeight + gapY);
            drawShareGameCard(image, g, card, x, y, cardWidth, cardHeight, palette);
        }

        drawShareFooter(g, image, palette);
        g.dispose();
        return image;
    }

    private BufferedImage generateProfileWidgetImage(Player player, boolean showPlayingRightNow, ThemePalette palette) {
        BufferedImage image = new BufferedImage(1800, 340, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = drawShareHeader(image, player, showPlayingRightNow, palette);
        g.dispose();
        return image;
    }

    private Graphics2D drawShareHeader(BufferedImage image, Player player, boolean showPlayingRightNow, ThemePalette palette) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        GradientPaint background = new GradientPaint(0, 0, palette.backgroundStart(), image.getWidth(), image.getHeight(), palette.backgroundEnd());
        g.setPaint(background);
        g.fillRoundRect(0, 0, image.getWidth(), image.getHeight(), 44, 44);

        g.setColor(palette.primaryGlow());
        g.fillOval(1180, -360, 780, 780);
        g.setColor(palette.secondaryGlow());
        g.fillOval(-260, image.getHeight() - 440, 620, 620);

        if (player.getSteamid() != null) {
            drawRoundImage(image, player.getAvatarfull(), 80, 78, 200, 200);
        }

        String name = player.getPersonaname() == null || player.getPersonaname().isBlank() ? "Steam profile" : player.getPersonaname();
        g.setFont(new Font("ARIAL", Font.BOLD, 68));
        g.setColor(palette.text());
        drawFittedString(g, name, 320, 155, 930);

        String status = getPlayerStatusText(player, showPlayingRightNow);
        g.setFont(new Font("ARIAL", Font.PLAIN, 36));
        g.setColor(palette.muted());
        drawFittedString(g, status, 322, 220, 930);

        g.setColor(palette.divider());
        g.fillRoundRect(80, 315, image.getWidth() - 160, 2, 1, 1);
        return g;
    }

    private List<GameCard> getGameCards(List<Object> games) {
        return games.stream().map((gameObject) -> {
            if (gameObject instanceof com.lukaspradel.steamapi.data.json.recentlyplayedgames.Game game) {
                String iconUrl = getGameIconUrl(game.getAppid(), game.getImgIconUrl(), game.getImgLogoUrl());
                return new GameCard(game.getName(), formatMinutes(game.getPlaytimeForever(), "Total"), formatMinutes(game.getPlaytime2weeks(), "Recent"), iconUrl);
            }
            if (gameObject instanceof com.lukaspradel.steamapi.data.json.ownedgames.Game game) {
                String iconUrl = getGameIconUrl(game.getAppid(), game.getImgIconUrl(), game.getImgLogoUrl());
                String recent = "";
                if (game.getAdditionalProperties() != null && game.getAdditionalProperties().containsKey("playtime_2weeks")) {
                    recent = formatMinutes((Integer) game.getAdditionalProperties().get("playtime_2weeks"), "Recent");
                }
                return new GameCard(game.getName(), formatMinutes(game.getPlaytimeForever(), "Total"), recent, iconUrl);
            }
            return null;
        }).filter((card) -> card != null).toList();
    }

    private void drawShareGameCard(BufferedImage image, Graphics2D g, GameCard card, int x, int y, int width, int height, ThemePalette palette) {
        g.setColor(palette.cardBackground());
        g.fillRoundRect(x, y, width, height, 32, 32);
        g.setColor(palette.cardBorder());
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, width, height, 32, 32);

        drawRoundImage(image, card.iconUrl(), x + 42, y + 58, 124, 124);

        g.setFont(new Font("ARIAL", Font.BOLD, 42));
        g.setColor(palette.text());
        drawFittedString(g, card.name(), x + 200, y + 78, width - 242);

        g.setFont(new Font("ARIAL", Font.PLAIN, 34));
        g.setColor(palette.muted());
        drawFittedString(g, card.totalPlaytime(), x + 200, y + 138, width - 242);

        if (!Strings.isNullOrEmpty(card.recentPlaytime())) {
            g.setColor(palette.accent());
            drawFittedString(g, card.recentPlaytime(), x + 200, y + 192, width - 242);
        }
    }

    private void drawShareFooter(Graphics2D g, BufferedImage image, ThemePalette palette) {
        g.setFont(new Font("ARIAL", Font.PLAIN, 28));
        g.setColor(palette.footer());
        g.drawString("generated by steam-widget.com", image.getWidth() - 450, image.getHeight() - 20);
    }

    private String getGameIconUrl(Object appId, String iconUrl, String logoUrl) {
        String imageHash = iconUrl == null || iconUrl.isBlank() ? logoUrl : iconUrl;
        return "https://media.steampowered.com/steamcommunity/public/images/apps/" + appId + "/" + imageHash + ".jpg";
    }

    private String formatMinutes(long minutes, String label) {
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return label + ": " + hours + "h " + remainingMinutes + "m";
    }

    private void drawShareInsightCard(Graphics2D g, InsightCard card, int x, int y, int width, int height, ThemePalette palette) {
        g.setColor(palette.cardBackground());
        g.fillRoundRect(x, y, width, height, 32, 32);
        g.setColor(palette.cardBorder());
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, width, height, 32, 32);

        g.setFont(new Font("ARIAL", Font.BOLD, 30));
        g.setColor(palette.accent());
        drawFittedString(g, card.label().toUpperCase(), x + 42, y + 54, width - 84);

        g.setFont(new Font("ARIAL", Font.BOLD, 62));
        g.setColor(palette.text());
        drawFittedString(g, card.value(), x + 42, y + 135, width - 84);

        if (!Strings.isNullOrEmpty(card.detail())) {
            g.setFont(new Font("ARIAL", Font.PLAIN, 34));
            g.setColor(palette.muted());
            drawFittedString(g, card.detail(), x + 42, y + 194, width - 84);
        }
    }

    private String getPlayerStatusText(Player player, boolean showPlayingRightNow) {
        String game = player.getAdditionalProperties() == null ? "" : player.getAdditionalProperties().getOrDefault("gameextrainfo", "").toString();
        if (showPlayingRightNow && !game.isBlank()) {
            return "Now playing " + game;
        }
        int state = player.getPersonastate() == null ? -1 : player.getPersonastate().intValue();
        return switch (state) {
            case 0 -> "Offline";
            case 1 -> "Online";
            case 2 -> "Busy";
            case 3 -> "Away";
            case 4 -> "Snooze";
            case 5 -> "Looking to trade";
            case 6 -> "Looking to play";
            default -> "Unknown";
        };
    }

    private void drawFittedString(Graphics2D g, String text, int x, int y, int maxWidth) {
        String fitted = text == null || text.isBlank() ? "-" : text;
        FontMetrics metrics = g.getFontMetrics();
        if (metrics.stringWidth(fitted) <= maxWidth) {
            g.drawString(fitted, x, y);
            return;
        }
        while (fitted.length() > 1 && metrics.stringWidth(fitted + "...") > maxWidth) {
            fitted = fitted.substring(0, fitted.length() - 1);
        }
        g.drawString(fitted + "...", x, y);
    }

    private String formatDuration(Long hours, Long minutes) {
        long safeHours = hours == null ? 0 : hours;
        long safeMinutes = minutes == null ? 0 : minutes;
        if (safeHours == 0 && safeMinutes == 0) {
            return "-";
        }
        return safeHours > 0 ? safeHours + "h " + safeMinutes + "m" : safeMinutes + "m";
    }

    private String formatDays(Long days) {
        if (days == null || days == 0) {
            return "-";
        }
        return days + (days == 1 ? " day" : " days");
    }

    private String formatDaysWithRange(Long days, LocalDate start, LocalDate end) {
        String formattedDays = formatDays(days);
        String range = formatDateRange(start, end);
        if (formattedDays.equals("-")) {
            return range;
        }
        if (range.equals("-")) {
            return formattedDays;
        }
        return formattedDays + " | " + range;
    }

    private String formatCount(Long count, String singular, String plural) {
        if (count == null || count == 0) {
            return "";
        }
        return count + " " + (count == 1 ? singular : plural);
    }

    private String formatNumber(Long number) {
        return number == null ? "-" : String.valueOf(number);
    }

    private String formatText(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : date.toString();
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        String formattedStart = formatDate(start);
        String formattedEnd = formatDate(end);
        if (formattedStart.equals("-") && formattedEnd.equals("-")) {
            return "-";
        }
        return formattedStart + " to " + formattedEnd;
    }

    private record InsightCard(String label, String value, String detail) {}

    private record GameCard(String name, String totalPlaytime, String recentPlaytime, String iconUrl) {}

    /**
     * Draws the game section on the widget image. This method iterates through the list of games and draws
     * each game's icon, name, and playtime information onto the widget.
     *
     * @param image The BufferedImage object representing the widget onto which the game section will be drawn.
     * @param games The list of games to be displayed in the game section. Each game can be an instance of
     *              {@link com.lukaspradel.steamapi.data.json.recentlyplayedgames.Game} or {@link com.lukaspradel.steamapi.data.json.ownedgames.Game}.
     */
    private void drawGameSection(BufferedImage image, @NotNull List<Object> games) {
        if (games.isEmpty()) {
            return;
        }

        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.fillRoundRect(25, 745, image.getWidth() - 50, 10, 5, 5);

        for (Object gameObject : games) {
            if (gameObject instanceof com.lukaspradel.steamapi.data.json.recentlyplayedgames.Game game) {
                String iconUrl = "https://media.steampowered.com/steamcommunity/public/images/apps/" + game.getAppid() + "/" + (Strings.isNullOrEmpty(game.getImgIconUrl()) ? game.getImgLogoUrl() : game.getImgIconUrl()) + ".jpg";
                drawRoundImage(image, iconUrl, 225, 750 + (games.indexOf(game) * 500) + 100, 300, 300);

                long totalHour = game.getPlaytimeForever() / 60;
                long totalMinute = game.getPlaytimeForever() % 60;
                String totalPlaytime = "Total Playtime: " + totalHour + "h " + totalMinute + "m";

                long recentHour = game.getPlaytime2weeks() / 60;
                long recentMinute = game.getPlaytime2weeks() % 60;
                String recentPlaytime = "Recent Playtime: " + recentHour + "h " + recentMinute + "m";

                drawString(image, game.getName(), "ARIAL", Font.BOLD, "#ffffff", 100, 725, 750 + (games.indexOf(game) * 500) + 250);
                drawString(image, recentPlaytime, "ARIAL", Font.PLAIN, "#c7d5e0", 75, 1725, 750 + (games.indexOf(game) * 500) + 350);
                drawString(image, totalPlaytime, "ARIAL", Font.PLAIN, "#c7d5e0", 75, 725, 750 + (games.indexOf(game) * 500) + 350);
            }
            else if (gameObject instanceof com.lukaspradel.steamapi.data.json.ownedgames.Game game) {
                String iconUrl = "https://media.steampowered.com/steamcommunity/public/images/apps/" + game.getAppid() + "/" + (Strings.isNullOrEmpty(game.getImgIconUrl()) ? game.getImgLogoUrl() : game.getImgIconUrl()) + ".jpg";
                drawRoundImage(image, iconUrl, 225, 750 + (games.indexOf(game) * 500) + 100, 300, 300);

                long totalHour = game.getPlaytimeForever() / 60;
                long totalMinute = game.getPlaytimeForever() % 60;
                String totalPlaytime = "Total Playtime: " + totalHour + "h " + totalMinute + "m";

                if (game.getAdditionalProperties().containsKey("playtime_2weeks")) {
                    int recentHour = (Integer) game.getAdditionalProperties().get("playtime_2weeks") / 60;
                    int recentMinute = (Integer) game.getAdditionalProperties().get("playtime_2weeks") % 60;
                    String recentPlaytime = "Recent Playtime: " + recentHour + "h " + recentMinute + "m";

                    drawString(image, recentPlaytime, "ARIAL", Font.PLAIN, "#c7d5e0", 75, 1725, 750 + (games.indexOf(game) * 500) + 350);
                }

                drawString(image, game.getName(), "ARIAL", Font.BOLD, "#ffffff", 100, 725, 750 + (games.indexOf(game) * 500) + 250);
                drawString(image, totalPlaytime, "ARIAL", Font.PLAIN, "#c7d5e0", 75, 725, 750 + (games.indexOf(game) * 500) + 350);
            }
        }

        g.dispose();
    }

    /**
     * Draws the user's information on the widget image. This includes the player's name and, if available,
     * the game they are currently playing. The information is drawn at specific coordinates with predefined
     * styles and colors.
     *
     * @param image  The BufferedImage object representing the widget onto which the user information will be drawn.
     * @param player The Player object containing the user's Steam profile information.
     */
    private void drawUserInformation(BufferedImage image, Player player, boolean showPlayingRightNow) {
        if (showPlayingRightNow && !player.getAdditionalProperties().getOrDefault("gameextrainfo", "").toString().isEmpty()) {
            this.drawString(image, player.getPersonaname(), "ARIAL", Font.BOLD, "#ffffff", 200, 725, 350);
            this.drawString(image, player.getAdditionalProperties().getOrDefault("gameextrainfo", "").toString(), "ARIAL", Font.PLAIN, "#c7d5e0", 150, 725, 550);
        } else {
            this.drawString(image, player.getPersonaname(), "ARIAL", Font.BOLD, "#ffffff", 200, 725, 450);
        }

        this.drawStateDot(image, player);
    }

    /**
     * Draws a colored dot on the widget image to represent the player's current state (e.g., online, busy, away).
     * The color of the dot changes based on the player's state.
     *
     * @param image  The BufferedImage object representing the widget onto which the state dot will be drawn.
     * @param player The Player object containing the user's Steam profile information.
     */
    private void drawStateDot(@NotNull BufferedImage image, @NotNull Player player) {
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(player.getAdditionalProperties().getOrDefault("gameextrainfo", "") != "" ? Color.GREEN : player.getPersonastate() == 3 ? Color.YELLOW : player.getPersonastate() == 2 ? Color.RED : player.getPersonastate() == 1 ? Color.decode("#00b7ff") : Color.decode("#898989"));
        g.fillOval(3350, 600, 100, 100);

        g.dispose();
    }

    /**
     * Draws a string on the widget image. This method is used to draw the player's name and game information.
     * The text is drawn with specified font, style, color, size, and coordinates.
     *
     * @param image    The BufferedImage object representing the widget onto which the text will be drawn.
     * @param display  The text to be drawn.
     * @param font     The font name to be used for drawing the text.
     * @param style    The style of the font (e.g., Font.BOLD).
     * @param hexColor The color of the text, specified in hexadecimal format.
     * @param size     The size of the font.
     * @param x        The x-coordinate where the text will start.
     * @param y        The y-coordinate where the text will start.
     */
    private void drawString(@NotNull BufferedImage image, String display, String font, int style, String hexColor, Integer size, Integer x, Integer y) {
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setFont(new Font(font, style, size));
        g.setColor(Color.decode(hexColor));
        g.drawString(display, x, y);

        g.dispose();
    }

    /**
     * Draws the profile image of the player on the widget. The profile image is first loaded from the URL,
     * then processed to have rounded corners before being drawn onto the widget.
     *
     * @param image The BufferedImage object representing the widget onto which the profile image will be drawn.
     * @param url The URL of the player's profile image.
     * @param x The x-coordinate where the profile image will be drawn.
     * @param y The y-coordinate where the profile image will be drawn.
     */
    private void drawRoundImage(@NotNull BufferedImage image, String url, int x, int y, int width, int height) {
        BufferedImage profileImage = this.loadImageFromURL(url);

        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        BufferedImage roundedProfileImage = this.makeRoundedCorner(profileImage, 500);

        g.drawImage(roundedProfileImage, x, y, width, height, null);

        g.dispose();
    }

    /**
     * Creates a BufferedImage with rounded corners from the given image. This method is used to process
     * images such as profile pictures to fit the widget's aesthetic.
     *
     * @param image        The original BufferedImage to be processed.
     * @param cornerRadius The radius of the rounded corners.
     * @return A new BufferedImage with rounded corners.
     */
    public BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        return output;
    }

    /**
     * Draws the base design of the widget onto the given BufferedImage. This includes setting the background,
     * drawing rounded corners, and placing the Steam logo at a predefined position.
     *
     * @param image The BufferedImage object representing the widget onto which the base design will be drawn.
     */
    private void drawBaseWidget(@NotNull BufferedImage image) {
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setComposite(AlphaComposite.Src);
        g.setColor(Color.decode("#171d25"));
        g.fillRoundRect(0, 0, image.getWidth(), image.getHeight(), 100, 100);
        g.setColor(Color.decode("#1b2838"));
        g.drawRoundRect(0, 0, image.getWidth(), image.getHeight(), 100, 100);

        BufferedImage logo = this.loadImageFromResources("/static/img/steam_logo.png");
        g.drawImage(logo, image.getWidth() - 500, 100, 400, 120, Color.decode("#171d25"), null);

        g.dispose();
    }

    /**
     * Loads an image from the resources folder given a path. This method is primarily used to load static assets
     * like the Steam logo.
     *
     * @param path The path to the resource within the resources folder.
     * @return A BufferedImage object of the loaded image, or an empty BufferedImage if the image could not be loaded.
     */
    private BufferedImage loadImageFromResources(String path) {
        if (path != null && path.length() > 5) {
            try {
                BufferedImage image = ImageIO.read(getClass().getResource(path));
                if (image != null) {
                    return image;
                }
            } catch (Exception ignored) {

            }
        }

        return createTransparentPlaceholder();
    }

    /**
     * Loads an image from a given URL. This method is used to load external images, such as user profile pictures.
     *
     * @param url The URL from which the image will be loaded.
     * @return A BufferedImage object of the loaded image, or an empty BufferedImage if the image could not be loaded.
     */
    private BufferedImage loadImageFromURL(String url) {
        if (url != null && url.length() > 5) {
            try {
                BufferedImage image = ImageIO.read(new URI(url).toURL());
                if (image != null) {
                    return image;
                }
            } catch (Exception ignored) {

            }
        }

        return createTransparentPlaceholder();
    }

    private BufferedImage createTransparentPlaceholder() {
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }

    public BufferedImage scaleImage(BufferedImage image, int width) {
        return Scalr.resize(image, width);
    }

}
