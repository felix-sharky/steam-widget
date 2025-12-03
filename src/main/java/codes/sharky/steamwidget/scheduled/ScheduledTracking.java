package codes.sharky.steamwidget.scheduled;

import codes.sharky.steamwidget.service.SteamTrackerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class ScheduledTracking {

    private final SteamTrackerService steamTrackerService;
    private final Environment env;

    public ScheduledTracking(SteamTrackerService steamTrackerService, Environment env) {
        this.steamTrackerService = steamTrackerService;
        this.env = env;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void scheduledTracking() {
        if (env.acceptsProfiles(Profiles.of("primary"))) {
            log.debug("Starting scheduled tracking of registered users.");
            steamTrackerService.trackRegisteredUsers();
        }
    }

}
