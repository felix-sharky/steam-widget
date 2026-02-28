package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.entity.TrackingProfileMonth;
import codes.sharky.steamwidget.service.TrackingProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller exposing tracking data endpoints for monthly and daily playtime aggregates.
 */
@RestController
public class TrackingProfileController {

    private final TrackingProfileService trackingProfileService;

    /**
     * Creates the controller with the tracking profile service dependency.
     *
     * @param trackingProfileService service supplying tracking aggregates
     */
    public TrackingProfileController(TrackingProfileService trackingProfileService) {
        this.trackingProfileService = trackingProfileService;
    }

    /**
     * Returns monthly tracking aggregates for a given Steam ID.
     *
     * @param steamId Steam ID to query
     * @return list of monthly tracking entries
     */
    @GetMapping("/api/tracking/profile-month")
    public ResponseEntity<List<TrackingProfileMonth>> profileMonth(@RequestParam("steamid") String steamId) {
        return ResponseEntity.ok(trackingProfileService.getTrackingProfileMonth(steamId));
    }

    /**
     * Returns daily tracking aggregates for a given Steam ID.
     *
     * @param steamId Steam ID to query
     * @return list of daily tracking entries
     */
    @GetMapping("/api/tracking/profile-date")
    public ResponseEntity<?> profileDate(@RequestParam("steamid") String steamId) {
        return ResponseEntity.ok(trackingProfileService.getTrackingProfileDate(steamId));
    }
}
