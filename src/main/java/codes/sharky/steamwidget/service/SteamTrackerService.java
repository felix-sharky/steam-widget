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

@Service
@Slf4j
public class SteamTrackerService {

    private final SteamWebAPIService steamWebAPIService;
    private final ProfileService profileService;

    public SteamTrackerService(SteamWebAPIService steamWebAPIService, ProfileService profileService) {
        this.steamWebAPIService = steamWebAPIService;
        this.profileService = profileService;
    }

    public void registerUser(@NotNull Player player) {
        if (profileService.profileTrackingActive(player.getSteamid())) {
            return;
        }
        profileService.upsertProfile(player.getSteamid(), player.getPersonaname(), true);
        trackUserGames(player.getSteamid(), true);
    }

    public void unregisterUser(@NotNull Player player) {
        if (!profileService.profileTrackingActive(player.getSteamid())) {
            return;
        }
        profileService.upsertProfile(player.getSteamid(), player.getPersonaname(), false);
        profileService.resetPlayerTrackers(player.getSteamid());
    }

    @Async
    public void toggleTracking(String steamId) {
        Player player = steamWebAPIService.getUserBySteamId(steamId);
        if (profileService.profileTrackingActive(player.getSteamid())) {
            this.unregisterUser(player);
        } else {
            this.registerUser(player);
        }
    }

    public void enableTracking(String steamId) {
        Player player = steamWebAPIService.getUserBySteamId(steamId);
        this.registerUser(player);
    }

    public void disableTracking(String steamId) {
        Player player = steamWebAPIService.getUserBySteamId(steamId);
        this.unregisterUser(player);
    }

    public void trackRegisteredUsers() {
        List<String> steamIds = profileService.getProfilesWithTracking().stream()
                .map(profile -> resolveSteamIdSafe(profile.getSteam64id()))
                .filter(id -> !id.isEmpty())
                .toList();

        steamIds.forEach(id -> trackUserGames(id, false));
    }

    private String resolveSteamIdSafe(String steam64id) {
        try {
            return steamWebAPIService.resolveSteamId(steam64id);
        } catch (SteamApiException ignored) {
            return "";
        }
    }

    private void trackUserGames(String steamId, boolean init) {
        try {
            List<Game> games = steamWebAPIService.getRecentlyPlayedGames(steamId);
            games.forEach(game -> saveGameTracker(steamId, game, init));
        } catch (Exception exception) {
            log.error("Failed to track playtime for user {}", steamId, exception);
        }
    }

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