package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.entity.TrackingProfileMonth;
import codes.sharky.steamwidget.service.TrackingProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TrackingProfileController {

    private final TrackingProfileService trackingProfileService;

    public TrackingProfileController(TrackingProfileService trackingProfileService) {
        this.trackingProfileService = trackingProfileService;
    }

    @GetMapping("/api/tracking/profile-month")
    public ResponseEntity<List<TrackingProfileMonth>> profileMonth(@RequestParam("steamid") String steamId) {
        return ResponseEntity.ok(trackingProfileService.getTrackingProfileMonth(steamId));
    }

    @GetMapping("/api/tracking/profile-date")
    public ResponseEntity<?> profileDate(@RequestParam("steamid") String steamId) {
        return ResponseEntity.ok(trackingProfileService.getTrackingProfileDate(steamId));
    }
}

