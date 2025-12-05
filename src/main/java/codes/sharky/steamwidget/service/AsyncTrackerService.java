package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.entity.PlayingTracker;
import com.lukaspradel.steamapi.data.json.ownedgames.Game;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncTrackerService {

    private final ProfileService profileService;

    @Async("trackerExecutor")
    public void saveGameTrackerAsync(String steamId, Game game, boolean init) {
        try {
            // Reuse existing logic from SteamTrackerService.saveGameTracker
            var trackerOpt = profileService.getLastPlayingTracker(steamId, game.getAppid().toString());
            if (trackerOpt.isEmpty() || trackerOpt.get().getTotalPlayingTime() < game.getPlaytimeForever()) {
                long newPlaytime = init ? 0L : trackerOpt.map(playingTracker -> game.getPlaytimeForever() - playingTracker.getTotalPlayingTime()).orElseGet(game::getPlaytimeForever);
                log.info("[async] User {} played {} for {} minutes.", steamId, game.getName(), newPlaytime);
                PlayingTracker newTracker = new PlayingTracker(steamId, game.getAppid().toString(), game.getName(), newPlaytime, game.getPlaytimeForever());
                profileService.savePlayingTracker(newTracker);
            }
        } catch (Exception exception) {
            log.error("[async] Failed to track playtime for user {}, game {}", steamId, game.getAppid(), exception);
        }
    }
}

