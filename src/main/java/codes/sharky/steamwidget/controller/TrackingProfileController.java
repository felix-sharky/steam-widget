package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.entity.TrackingProfileInsightsActivity;
import codes.sharky.steamwidget.entity.TrackingProfileInsightsGame;
import codes.sharky.steamwidget.entity.TrackingProfileInsightsPlaytime;
import codes.sharky.steamwidget.entity.TrackingProfileMonth;
import codes.sharky.steamwidget.service.TrackingProfileService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller exposing tracking data endpoints for monthly, daily and insights aggregates.
 */
@RestController
public class TrackingProfileController {

    private final TrackingProfileService trackingProfileService;

    public TrackingProfileController(TrackingProfileService trackingProfileService) {
        this.trackingProfileService = trackingProfileService;
    }

    /**
     * Returns monthly tracking aggregates for a given Steam ID.
     */
    @GetMapping("/api/tracking/profile-month")
    public ResponseEntity<List<TrackingProfileMonth>> profileMonth(
            @RequestParam("steamid") String steamId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(trackingProfileService.getTrackingProfileMonth(steamId, startDate, endDate));
    }

    /**
     * Returns daily tracking aggregates for a given Steam ID.
     */
    @GetMapping("/api/tracking/profile-date")
    public ResponseEntity<?> profileDate(
            @RequestParam("steamid") String steamId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(trackingProfileService.getTrackingProfileDate(steamId, startDate, endDate));
    }

    // -----------------------------------------------------------------------
    // Insights – Activity (Streaks & Activity)
    // -----------------------------------------------------------------------

    /**
     * Returns streaks-and-activity insights for a single profile.
     */
    @GetMapping("/api/tracking/insights/activity")
    public ResponseEntity<TrackingProfileInsightsActivity> insightsActivity(
            @RequestParam("steamid") String steamId
    ) {
        return trackingProfileService.getInsightsActivity(steamId);
    }

    // -----------------------------------------------------------------------
    // Insights – Playtime Stats
    // -----------------------------------------------------------------------

    /**
     * Returns playtime stats insights for a single profile.
     */
    @GetMapping("/api/tracking/insights/playtime")
    public ResponseEntity<TrackingProfileInsightsPlaytime> insightsPlaytime(
            @RequestParam("steamid") String steamId
    ) {
        return trackingProfileService.getInsightsPlaytime(steamId);
    }

    // -----------------------------------------------------------------------
    // Insights – Game Insights
    // -----------------------------------------------------------------------

    /**
     * Returns game insights for a single profile.
     */
    @GetMapping("/api/tracking/insights/games")
    public ResponseEntity<TrackingProfileInsightsGame> insightsGames(
            @RequestParam("steamid") String steamId
    ) {
        return trackingProfileService.getInsightsGame(steamId);
    }
}


