package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.component.SteamWebAPI;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SteamWebAPIService {

    private final SteamWebAPI api;

    public SteamWebAPIService(SteamWebAPI api) {
        this.api = api;
    }

    /**
     * Retrieves a {@link Player} object by their Steam ID. If the Steam ID is not in the correct format,
     * it attempts to resolve it. This method also logs the access attempt by adding a hit to the profile
     * associated with the Steam ID.
     *
     * @param steamId The Steam ID of the user, which can be either a numeric ID or a vanity URL.
     * @return A {@link Player} object containing the user's Steam profile information. Returns an empty
     * {@link Player} object if no information could be retrieved.
     */
    public Player getUserBySteamId(String steamId) {
        try {
            List<Player> players = getUsersBySteamIds(List.of(steamId));
            if (!players.isEmpty()) {
                return players.getFirst();
            }
        } catch (Exception ignored) {
            log.warn(ignored.getMessage());
        }
        return new Player();
    }

    /**
     * Retrieves a {@link List} of {@link Player} objects by their Steam IDs. If any Steam ID is not in the correct format,
     * it attempts to resolve it. This method processes multiple Steam IDs in a single request.
     *
     * @param steamIds A {@link List} of Steam IDs, which can be either numeric IDs or vanity URLs.
     * @return A {@link List} of {@link Player} objects containing the users' Steam profile information. Returns an empty
     * {@link List} if no information could be retrieved.
     */
    public List<Player> getUsersBySteamIds(List<String> steamIds) {
        try {
            List<String> resolvedIds = new ArrayList<>();
            for (String steamId : steamIds) {
                String id = resolveSteamId(steamId);
                resolvedIds.add(id == null ? steamId : id);
            }
            GetPlayerSummariesRequest request = new GetPlayerSummariesRequest.GetPlayerSummariesRequestBuilder(resolvedIds).buildRequest();
            GetPlayerSummaries playerSummaries = api.getClient().<GetPlayerSummaries>processRequest(request);
            return playerSummaries.getResponse().getPlayers();
        } catch (Exception ignored) {
            log.warn(ignored.getMessage());
        }
        return new ArrayList<>();
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
        try {
            GetOwnedGamesRequest request = new GetOwnedGamesRequest.GetOwnedGamesRequestBuilder(steamId).includeAppInfo(true).includePlayedFreeGames(true).buildRequest();
            GetOwnedGames ownedGames = api.getClient().processRequest(request);
            ownedGames.getResponse().getGames().sort((g1, g2) -> (Integer) g2.getAdditionalProperties().get("rtime_last_played") - (Integer) g1.getAdditionalProperties().get("rtime_last_played"));
            return ownedGames.getResponse().getGames();
        } catch (Exception ignored) {
            log.warn(ignored.getMessage());
        }
        return new ArrayList<>();
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
        try {
            GetRecentlyPlayedGamesRequest request = new GetRecentlyPlayedGamesRequest.GetRecentlyPlayedGamesRequestBuilder(steamId).buildRequest();
            GetRecentlyPlayedGames recentlyPlayedGames = api.getClient().processRequest(request);
            recentlyPlayedGames.getResponse().getGames().sort((g1, g2) -> Math.toIntExact(g2.getPlaytime2weeks() - g1.getPlaytime2weeks()));
            return recentlyPlayedGames.getResponse().getGames();
        } catch (Exception ignored) {
            log.warn(ignored.getMessage());
        }
        return new ArrayList<>();
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
        try {
            GetOwnedGamesRequest request = new GetOwnedGamesRequest.GetOwnedGamesRequestBuilder(steamId).includeAppInfo(true).includePlayedFreeGames(true).buildRequest();
            GetOwnedGames ownedGames = api.getClient().processRequest(request);
            ownedGames.getResponse().getGames().sort((g1, g2) -> Math.toIntExact(g2.getPlaytimeForever() - g1.getPlaytimeForever()));
            return ownedGames.getResponse().getGames();
        } catch (Exception ignored) {
            log.warn(ignored.getMessage());
        }
        return new ArrayList<>();
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
        try {
            if (!(id.matches("[0-9]+")) && id.length() != 17) {
                if (id.contains("https://steamcommunity.com/id/")) {
                    id = id.replaceAll("https://steamcommunity.com/id/", "").replaceAll("/", "");
                }
                ResolveVanityUrlRequest request = new ResolveVanityUrlRequest.ResolveVanityUrlRequestBuilder(id).buildRequest();
                ResolveVanityURL vanityURL = api.getClient().processRequest(request);
                id = vanityURL.getResponse().getSteamid();
            }
        } catch (Exception ignored) {
            log.warn(ignored.getMessage());
        }
        return id;
    }

}
