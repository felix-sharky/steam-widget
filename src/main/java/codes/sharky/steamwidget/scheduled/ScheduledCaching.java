package codes.sharky.steamwidget.scheduled;

import codes.sharky.steamwidget.repository.ProfileCacheRepository;
import codes.sharky.steamwidget.service.ProfileCachingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks that refresh and prune profile caches for the primary profile.
 */
@Component
@EnableScheduling
@Slf4j
public class ScheduledCaching {

    private final ProfileCachingService profileCachingService;
    private final Environment env;

    /**
     * Creates the scheduler with caching service and environment to gate execution by profile.
     *
     * @param profileCachingService service used to refresh and deactivate caches
     * @param env                   environment used to check active profiles
     */
    public ScheduledCaching(ProfileCachingService profileCachingService, Environment env) {
        this.profileCachingService = profileCachingService;
        this.env = env;
    }

    /**
     * Refreshes active profile caches every minute at second 50 when running in the primary profile.
     */
    @Scheduled(cron = "50 * * * * *")
    public void caching() {
        if (env.acceptsProfiles(Profiles.of("primary"))) {
            log.debug("Updating active profile caches");
            profileCachingService.updateActiveProfileCaches();
        }
    }

    /**
     * Deactivates stale caches at the top of every hour when running in the primary profile.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void deactivateOldCaches() {
        if (env.acceptsProfiles(Profiles.of("primary"))) {
            log.debug("Deactivating old profile caches");
            profileCachingService.deactivateOldCaches();
        }
    }

}
