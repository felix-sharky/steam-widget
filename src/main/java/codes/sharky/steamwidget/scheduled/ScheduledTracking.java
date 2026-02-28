package codes.sharky.steamwidget.scheduled;

import codes.sharky.steamwidget.service.SteamTrackerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that triggers gameplay tracking for registered users when running on the primary profile.
 */
@Component
@EnableScheduling
@Slf4j
public class ScheduledTracking {

    private final SteamTrackerService steamTrackerService;
    private final Environment env;

    /**
     * Creates the scheduler with tracking service and environment used to gate execution by active profile.
     *
     * @param steamTrackerService service that performs tracking of registered users
     * @param env                 environment to check active profiles
     */
    public ScheduledTracking(SteamTrackerService steamTrackerService, Environment env) {
        this.steamTrackerService = steamTrackerService;
        this.env = env;
    }

    /**
     * Runs tracking each hour on the hour when the application is running under the "primary" profile.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void scheduledTracking() {
        if (env.acceptsProfiles(Profiles.of("primary"))) {
            log.debug("Starting scheduled tracking of registered users.");
            steamTrackerService.trackRegisteredUsers();
        }
    }

}
