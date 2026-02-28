package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.entity.PlayingTracker;
import com.lukaspradel.steamapi.core.exception.SteamApiException;
import com.lukaspradel.steamapi.data.json.ownedgames.Game;
import com.lukaspradel.steamapi.data.json.playersummaries.Player;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service that toggles and executes gameplay tracking for Steam users.
 * Resolves Steam users, manages tracking flags, and records playtime deltas via async trackers.
 */
@Service
@Slf4j
public class SteamTrackerService {

    private final SteamWebAPIService steamWebAPIService;
    private final ProfileService profileService;
    private final AsyncTrackerService asyncTrackerService;

    /**
     * Creates a tracker service with Steam API access, profile management, and async tracking support.
     *
     * @param steamWebAPIService client for Steam Web API lookups
     * @param profileService     service used to persist tracking flags and trackers
     * @param asyncTrackerService async handler that persists game tracker records
     */
    public SteamTrackerService(SteamWebAPIService steamWebAPIService, ProfileService profileService, AsyncTrackerService asyncTrackerService) {
        this.steamWebAPIService = steamWebAPIService;
        this.profileService = profileService;
        this.asyncTrackerService = asyncTrackerService;
    }

    /**
     * Registers a user for tracking and immediately seeds trackers with recent games.
     *
     * @param player Steam player summary to register
     */
    public void registerUser(@NotNull Player player) {
        if (profileService.profileTrackingActive(player.getSteamid())) {
            return;
        }
        profileService.upsertProfileTracking(player.getSteamid(), player.getPersonaname(), true);
        trackUserGames(player.getSteamid(), true);
    }

    /**
     * Unregisters a tracked user and clears their stored game trackers.
     *
     * @param player Steam player summary to unregister
     */
    public void unregisterUser(@NotNull Player player) {
        if (!profileService.profileTrackingActive(player.getSteamid())) {
            return;
        }
        profileService.upsertProfileTracking(player.getSteamid(), player.getPersonaname(), false);
        profileService.resetPlayerTrackers(player.getSteamid());
    }

    /**
     * Asynchronously toggles tracking for the given Steam ID by resolving the user and flipping their tracking state.
     *
     * @param steamId Steam ID whose tracking status should be toggled
     */
    @Async
    public void toggleTracking(String steamId) {
        Player player = steamWebAPIService.getUserBySteamId(steamId);
        if (profileService.profileTrackingActive(player.getSteamid())) {
            this.unregisterUser(player);
        } else {
            this.registerUser(player);
        }
    }

    /**
     * Enables tracking for the provided Steam ID.
     *
     * @param steamId Steam ID to enable tracking for
     */
    public void enableTracking(String steamId) {
        Player player = steamWebAPIService.getUserBySteamId(steamId);
        this.registerUser(player);
    }

    /**
     * Disables tracking for the provided Steam ID.
     *
     * @param steamId Steam ID to disable tracking for
     */
    public void disableTracking(String steamId) {
        Player player = steamWebAPIService.getUserBySteamId(steamId);
        this.unregisterUser(player);
    }

    /**
     * Iterates over all tracked profiles and records recent playtime deltas for each.
     */
    public void trackRegisteredUsers() {
        List<String> steamIds = profileService.getProfilesWithTracking().stream()
                .map(profile -> resolveSteamIdSafe(profile.getSteam64id()))
                .filter(id -> !id.isEmpty())
                .toList();

        steamIds.forEach(id -> trackUserGames(id, false));
    }

    /**
     * Resolves a Steam ID safely, returning an empty string when resolution fails.
     *
     * @param steam64id Steam ID or vanity to resolve
     * @return resolved Steam ID or empty string on error
     */
    private String resolveSteamIdSafe(String steam64id) {
        try {
            return steamWebAPIService.resolveSteamId(steam64id);
        } catch (SteamApiException ignored) {
            return "";
        }
    }

    /**
     * Fetches recently played games for a Steam ID and delegates persistence to the async tracker.
     *
     * @param steamId Steam ID whose recent games to track
     * @param init    when true, seeds trackers without logging playtime deltas
     */
    private void trackUserGames(String steamId, boolean init) {
        try {
            List<Game> games = steamWebAPIService.getRecentlyPlayedGames(steamId);
            games.forEach(game -> asyncTrackerService.saveGameTrackerAsync(steamId, game, init));
        } catch (Exception exception) {
            log.error("Failed to track playtime for user {}", steamId, exception);
        }
    }

    /**
     * Persists a game tracker record if playtime increased since the last snapshot.
     *
     * @param steamId Steam ID associated with the game
     * @param game    Steam game data containing playtime totals
     * @param init    when true, initializes tracker without calculating deltas
     */
    private void saveGameTracker(String steamId, @NotNull Game game, boolean init) {
        try {
            Optional<PlayingTracker> tracker = profileService.getLastPlayingTracker(steamId, game.getAppid().toString());
            if (tracker.isEmpty() || tracker.get().getTotalPlayingTime() < game.getPlaytimeForever()) {
                long newPlaytime = init ? 0L : tracker.map(playingTracker -> game.getPlaytimeForever() - playingTracker.getTotalPlayingTime()).orElseGet(game::getPlaytimeForever);
                log.info("User {} played {} for {} minutes.", steamId, game.getName(), newPlaytime);
                PlayingTracker newTracker = new PlayingTracker(steamId, game.getAppid().toString(), game.getName(), newPlaytime, game.getPlaytimeForever());
                profileService.savePlayingTracker(newTracker);
            }
        } catch (Exception exception) {
            log.error("Failed to track playtime for user {}, game {}", steamId, game.getAppid(), exception);
        }
    }
}