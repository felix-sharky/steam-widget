package de.gamergrotte.steam.widget.service;

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
import de.gamergrotte.steam.widget.component.SteamWebAPI;
import de.gamergrotte.steam.widget.entity.Hit;
import de.gamergrotte.steam.widget.entity.Profile;
import de.gamergrotte.steam.widget.model.ShowedGames;
import de.gamergrotte.steam.widget.repository.HitRepository;
import de.gamergrotte.steam.widget.repository.ProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.imgscalr.Scalr;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    private final SteamWebAPI api;

    private final ProfileRepository repository;

    private final HitRepository hitRepository;

    public SteamWidgetService(SteamWebAPI api, ProfileRepository repository, HitRepository hitRepository) {
        this.api = api;
        this.repository = repository;
        this.hitRepository = hitRepository;
    }

    /**
     * Retrieves the IP address of the client from the HTTP request.
     * <p>
     * This method checks the "X-Forwarded-For" header to determine if the request
     * was forwarded by a proxy. If the header is present and not empty, it returns
     * the first IP address in the list. Otherwise, it returns the remote address
     * of the request.
     * </p>
     *
     * @param request The HttpServletRequest object containing the client's request.
     * @return The IP address of the client as a String.
     */
    public String getIPAddress(@NotNull HttpServletRequest request) {
        if (Strings.isNullOrEmpty(request.getHeader("X-Forwarded-For")))
            return request.getRemoteAddr();
        else {
            return request.getHeader("X-Forwarded-For").split(",")[0].trim();
        }
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
        String id = resolveSteamId(steamId);
        GetPlayerSummariesRequest request = new GetPlayerSummariesRequest.GetPlayerSummariesRequestBuilder(List.of(id == null ? steamId : id)).buildRequest();
        GetPlayerSummaries playerSummaries = api.getClient().<GetPlayerSummaries>processRequest(request);
        List<Player> players = playerSummaries.getResponse().getPlayers();
        if (!players.isEmpty()) {
            addHitToProfile(id, players.getFirst().getPersonaname(), purpose, ip, LocalDateTime.now());
            return players.getFirst();
        }
        return new Player();
    }

    /**
     * Retrieves a list of recently played games for a given Steam ID.
     * <p>
     * This method sends a request to the Steam Web API to fetch the recently played games
     * for the specified Steam ID. It constructs a {@link GetOwnedGamesRequest} using the
     * provided Steam ID, processes the request, and returns a {@link List} of {@link com.lukaspradel.steamapi.data.json.ownedgames.Game} objects.
     * The games are sorted by the time they were last played, in descending order.
     * </p>
     *
     * @param steamId The Steam ID of the user whose recently played games are to be retrieved.
     * @return A list of {@link com.lukaspradel.steamapi.data.json.ownedgames.Game} objects representing the recently played games.
     * @throws SteamApiException If there is an issue with accessing the Steam Web API.
     */
    public List<com.lukaspradel.steamapi.data.json.ownedgames.Game> getRecentlyPlayedGames(String steamId) throws SteamApiException {
        GetOwnedGamesRequest request = new GetOwnedGamesRequest.GetOwnedGamesRequestBuilder(steamId).includeAppInfo(true).includePlayedFreeGames(true).buildRequest();
        GetOwnedGames ownedGames = api.getClient().processRequest(request);
        ownedGames.getResponse().getGames().sort((g1, g2) -> (Integer) g2.getAdditionalProperties().get("rtime_last_played") - (Integer) g1.getAdditionalProperties().get("rtime_last_played"));
        return ownedGames.getResponse().getGames();
    }

    /**
     * Retrieves a list of recently played games for a given Steam ID.
     * <p>
     * This method sends a request to the Steam Web API to fetch the recently played games
     * for the specified Steam ID. It constructs a {@link GetRecentlyPlayedGamesRequest} using the
     * provided Steam ID, processes the request, and returns a {@link List} of {@link com.lukaspradel.steamapi.data.json.recentlyplayedgames.Game} objects.
     * The games are sorted by the playtime in the last two weeks, in descending order.
     * </p>
     *
     * @param steamId The Steam ID of the user whose recently played games are to be retrieved.
     * @return A list of {@link com.lukaspradel.steamapi.data.json.recentlyplayedgames.Game} objects representing the recently played games.
     * @throws SteamApiException If there is an issue with accessing the Steam Web API.
     */
    public List<com.lukaspradel.steamapi.data.json.recentlyplayedgames.Game> getTopRecentlyPlayedGames(String steamId) throws SteamApiException {
        GetRecentlyPlayedGamesRequest request = new GetRecentlyPlayedGamesRequest.GetRecentlyPlayedGamesRequestBuilder(steamId).buildRequest();
        GetRecentlyPlayedGames recentlyPlayedGames = api.getClient().processRequest(request);
        recentlyPlayedGames.getResponse().getGames().sort((g1, g2) -> Math.toIntExact(g2.getPlaytime2weeks() - g1.getPlaytime2weeks()));
        return recentlyPlayedGames.getResponse().getGames();
    }

    /**
     * Retrieves a list of top owned games for a given Steam ID.
     * <p>
     * This method sends a request to the Steam Web API to fetch the owned games
     * for the specified Steam ID. It constructs a {@link GetOwnedGamesRequest} using the
     * provided Steam ID, processes the request, and returns a {@link List} of {@link com.lukaspradel.steamapi.data.json.ownedgames.Game} objects.
     * The games are sorted by the total playtime, in descending order.
     * </p>
     *
     * @param steamId The Steam ID of the user whose owned games are to be retrieved.
     * @return A list of {@link com.lukaspradel.steamapi.data.json.ownedgames.Game} objects representing the top owned games.
     * @throws SteamApiException If there is an issue with accessing the Steam Web API.
     */
    public List<com.lukaspradel.steamapi.data.json.ownedgames.Game> getTopOwnedGames(String steamId) throws SteamApiException {
        GetOwnedGamesRequest request = new GetOwnedGamesRequest.GetOwnedGamesRequestBuilder(steamId).includeAppInfo(true).includePlayedFreeGames(true).buildRequest();
        GetOwnedGames ownedGames = api.getClient().processRequest(request);
        ownedGames.getResponse().getGames().sort((g1, g2) -> Math.toIntExact(g2.getPlaytimeForever() - g1.getPlaytimeForever()));
        return ownedGames.getResponse().getGames();
    }

    /**
     * Resolves the Steam ID to a numeric format if it is not already. This method handles both direct numeric Steam IDs
     * and vanity URLs (custom user URLs). If the input is a vanity URL, it uses the Steam Web API to resolve it to a numeric ID.
     *
     * @param steamId The Steam ID or vanity URL of the user.
     * @return The numeric Steam ID corresponding to the input, or the original input if it's already a numeric ID or cannot be resolved.
     * @throws SteamApiException If there is an issue with accessing the Steam Web API.
     */
    public String resolveSteamId(String steamId) throws SteamApiException {
        String id = steamId;
        if (!(id.matches("[0-9]+")) && id.length() != 17) {
            if (id.contains("https://steamcommunity.com/id/")) {
                id = id.replaceAll("https://steamcommunity.com/id/", "").replaceAll("/", "");
            }
            ResolveVanityUrlRequest request = new ResolveVanityUrlRequest.ResolveVanityUrlRequestBuilder(id).buildRequest();
            ResolveVanityURL vanityURL = api.getClient().processRequest(request);
            id = vanityURL.getResponse().getSteamid();
        }
        return id;
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
        String ip = getIPAddress(request);
        return generateWidgetImage(steamId, showGames, recentGamesCount, showPlayingRightNow, purpose, ip);
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
        Player player = getUserBySteamId(steamId, purpose, ip);

        List<Object> games = switch (showGames) {
            case TOP_GAMES_RECENT -> {
                List<Object> objects = new ArrayList<>(player.getSteamid() != null ? getTopRecentlyPlayedGames(player.getSteamid()) : new ArrayList<>());
                yield objects.stream().limit(recentGamesCount).toList();
            }
            case TOP_GAMES_TOTAL -> {
                List<Object> objects = new ArrayList<>(player.getSteamid() != null ? getTopOwnedGames(player.getSteamid()) : new ArrayList<>());
                yield objects.stream().limit(recentGamesCount).toList();
            }
            case RECENT_GAMES -> {
                List<Object> objects = new ArrayList<>(player.getSteamid() != null ? getRecentlyPlayedGames(player.getSteamid()) : new ArrayList<>());
                yield objects.stream().limit(recentGamesCount).toList();
            }
            default -> new ArrayList<>();
        };

        BufferedImage bufferedImage = new BufferedImage(3500, 750 + (games.size() * 500), BufferedImage.TYPE_INT_ARGB);
        this.drawBaseWidget(bufferedImage);
        if (player.getSteamid() != null) {
            drawRoundImage(bufferedImage, player.getAvatarfull(), 125, 125, 500, 500);
            drawUserInformation(bufferedImage, player, showPlayingRightNow);

            drawGameSection(bufferedImage, games);
        }

        return bufferedImage;
    }

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
                String iconUrl = "https://media.steampowered.com/steamcommunity/public/images/apps/" + game.getAppid() + "/" + (game.getImgIconUrl().isEmpty() ? game.getImgLogoUrl() : game.getImgIconUrl()) + ".jpg";
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
                String iconUrl = "https://media.steampowered.com/steamcommunity/public/images/apps/" + game.getAppid() + "/" + (game.getImgIconUrl().isEmpty() ? game.getImgLogoUrl() : game.getImgIconUrl()) + ".jpg";
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

        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

        g2.setComposite(AlphaComposite.SrcAtop);
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
                return ImageIO.read(getClass().getResource(path));
            } catch (Exception ignored) {

            }
        }

        return new BufferedImage(0, 0, BufferedImage.TYPE_INT_ARGB);
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
                return ImageIO.read(new URI(url).toURL());
            } catch (Exception ignored) {

            }
        }

        return new BufferedImage(0, 0, BufferedImage.TYPE_INT_ARGB);
    }

    public BufferedImage scaleImage(BufferedImage image, int width) {
        return Scalr.resize(image, width);
    }

    /**
     * Asynchronously adds a hit to a profile identified by the Steam ID. If the profile does not exist, it creates a new profile
     * with the given Steam ID and name, initializing the hit count to 1. Otherwise, it increments the hit count for the existing profile.
     * Additionally, it records the hit details including the Steam ID, timestamp, purpose, and IP address in the Hit entity.
     *
     * @param steamId       The Steam ID of the user for whom the hit is being recorded.
     * @param name          The name of the user associated with the Steam ID.
     * @param purpose       The reason for the hit, describing why the user's information was accessed.
     * @param ip            The IP address from which the request to access the user's information originated.
     * @param localDateTime The timestamp when the hit occurred.
     */
    @Async
    public void addHitToProfile(String steamId, String name, String purpose, String ip, LocalDateTime localDateTime) {
        if (!repository.existsById(steamId)) {
            Profile profile = new Profile(steamId, name, 1L);
            repository.save(profile);
        } else {
            repository.incrementHits(steamId, name);
        }

        Hit hit = new Hit(steamId, localDateTime, purpose, ip);
        hitRepository.save(hit);
    }

    /**
     * Retrieves the total number of hits for a given profile identified by the Steam ID.
     *
     * @param steamId The Steam ID of the profile for which the hit count is being queried.
     * @return The total number of hits for the profile. Returns 0 if the profile does not exist.
     */
    public long getProfileHitByProfile(String steamId) {
        Optional<Profile> profileOptional = repository.findById(steamId);
        if (profileOptional.isPresent()) {
            return profileOptional.get().getHits();
        } else {
            return 0;
        }
    }

    /**
     * Retrieves the number of hits for a given profile identified by the Steam ID, optionally filtered by purpose.
     * If the purpose is not specified (empty string), it returns the total hit count for the profile.
     *
     * @param steamId The Steam ID of the profile for which the hit count is being queried.
     * @param purpose The purpose for filtering the hits. If empty, all hits for the profile are counted.
     * @return The number of hits for the profile, filtered by purpose if specified.
     */
    public long getHitByProfileAndPurpose(String steamId, @NotNull String purpose) {
        if (purpose.isEmpty()) {
            return hitRepository.countHitsBySteam64id(steamId);
        } else {
            return hitRepository.countHitsBySteam64idAndPurpose(steamId, purpose);
        }
    }

    /**
     * Retrieves the profile information for a given Steam ID. If the profile does not exist, it returns a new, empty Profile object.
     *
     * @param steamId The Steam ID of the profile to retrieve.
     * @return A Profile object containing the profile information. Returns an empty Profile object if the profile does not exist.
     */
    public Profile getProfile(String steamId) {
        Optional<Profile> profileOptional = repository.findById(steamId);
        return profileOptional.orElseGet(Profile::new);
    }
}
