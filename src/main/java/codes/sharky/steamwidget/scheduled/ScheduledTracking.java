package codes.sharky.steamwidget.scheduled;

import codes.sharky.steamwidget.service.SteamTrackerService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ScheduledTracking {

    SteamTrackerService steamTrackerService;

    public ScheduledTracking(SteamTrackerService steamTrackerService) {
        this.steamTrackerService = steamTrackerService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void scheduledTracking() {
        steamTrackerService.trackRegisteredUsers();
    }

}
