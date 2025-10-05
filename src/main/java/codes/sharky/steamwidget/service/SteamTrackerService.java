package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.entity.PlayingTracker;
import codes.sharky.steamwidget.entity.Profile;
import com.lukaspradel.steamapi.core.exception.SteamApiException;
import com.lukaspradel.steamapi.data.json.ownedgames.Game;
import com.lukaspradel.steamapi.data.json.playersummaries.Player;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SteamTrackerService {

    private final SteamWebAPIService steamWebAPIService;
    private final ProfileService profileService;

    public SteamTrackerService(SteamWebAPIService steamWebAPIService, ProfileService profileService) {
        this.steamWebAPIService = steamWebAPIService;
        this.profileService = profileService;
    }

    public void registerUser(@NotNull Player player) {
        profileService.upsertProfile(player.getSteamid(), player.getPersonaname(), true);
        trackUserGames(player.getSteamid(), true);
    }

    public void unregisterUser(@NotNull Player player) {
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
        } catch (Exception ignored) {
        }
    }

    private void saveGameTracker(String steamId, @NotNull Game game, boolean init) {
        PlayingTracker tracker = profileService.getLastPlayingTracker(steamId, game.getAppid().toString());
        if (tracker == null || tracker.getId() == null || tracker.getTotalPlayingTime() < game.getPlaytimeForever()) {
            long newPlaytime = init ? 0L : tracker == null ? game.getPlaytimeForever() : game.getPlaytimeForever() - tracker.getTotalPlayingTime();
            PlayingTracker newTracker = new PlayingTracker(steamId, game.getAppid().toString(), game.getName(), newPlaytime, game.getPlaytimeForever());
            profileService.savePlayingTracker(newTracker);
        }
    }
}