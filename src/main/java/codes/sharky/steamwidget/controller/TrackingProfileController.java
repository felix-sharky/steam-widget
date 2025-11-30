package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.entity.TrackingProfileMonth;
import codes.sharky.steamwidget.service.TrackingProfileMonthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TrackingProfileController {

    private final TrackingProfileMonthService trackingProfileMonthService;

    public TrackingProfileController(TrackingProfileMonthService trackingProfileMonthService) {
        this.trackingProfileMonthService = trackingProfileMonthService;
    }

    @GetMapping("/api/tracking/profile-month")
    public ResponseEntity<List<TrackingProfileMonth>> profileMonth(@RequestParam("steamid") String steamId) {
        return ResponseEntity.ok(trackingProfileMonthService.getTrackingProfileMonth(steamId));
    }
}

