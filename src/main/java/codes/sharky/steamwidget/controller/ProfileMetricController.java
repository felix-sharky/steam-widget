package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.service.SteamWebAPIService;
import com.lukaspradel.steamapi.core.exception.SteamApiException;
import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsDayView;
import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsFullView;
import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsMonthView;
import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsYearView;
import codes.sharky.steamwidget.service.ProfileMetricsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller exposing profile metrics data endpoints for monthly and daily profile hits aggregates.
 */
@RestController
@RequestMapping("/api/metrics/profile")
public class ProfileMetricController {

    private final ProfileMetricsService profileMetricsService;
    private final SteamWebAPIService steamWebAPIService;

    /**
     * Creates the controller with the profile metrics service dependency.
     *
     * @param profileMetricsService service supplying profile metrics aggregates
     * @param steamWebAPIService    service for Steam Web API operations
     */
    public ProfileMetricController(ProfileMetricsService profileMetricsService, SteamWebAPIService steamWebAPIService) {
        this.profileMetricsService = profileMetricsService;
        this.steamWebAPIService = steamWebAPIService;
    }

    /**
     * Returns daily profile metrics hits for a given Steam ID.
     *
     * @param steam64id Steam ID to query
     * @param startDate optional start date (inclusive)
     * @param endDate   optional end date (inclusive)
     * @return list of daily profile metrics hits
     */
    @GetMapping("/day")
    public ResponseEntity<List<MetricsProfileHitsDayView>> getAllMetricsProfileDayViews(
            @RequestParam String steam64id,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws SteamApiException {
        String id = steamWebAPIService.resolveSteamId(steam64id);
        return ResponseEntity.ok(profileMetricsService.getMetricsProfileHitsDayViewsBySteam64Id(id, startDate, endDate));
    }

    /**
     * Returns full profile metrics hits for a given Steam ID.
     *
     * @param steam64id Steam ID to query
     * @return list of full profile metrics hits
     */
    @GetMapping("/full")
    public ResponseEntity<List<MetricsProfileHitsFullView>> getAllMetricsProfileFullViews(
            @RequestParam String steam64id
    ) throws SteamApiException {
        String id = steamWebAPIService.resolveSteamId(steam64id);
        return ResponseEntity.ok(profileMetricsService.getMetricsProfileHitsFullViewsBySteam64Id(id));
    }

    /**
     * Returns monthly profile metrics hits for a given Steam ID.
     *
     * @param steam64id Steam ID to query
     * @param startDate optional start date (inclusive, interpreted at month granularity)
     * @param endDate   optional end date (inclusive, interpreted at month granularity)
     * @return list of monthly profile metrics hits
     */
    @GetMapping("/month")
    public ResponseEntity<List<MetricsProfileHitsMonthView>> getAllMetricsProfileMonthViews(
            @RequestParam String steam64id,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws SteamApiException {
        String id = steamWebAPIService.resolveSteamId(steam64id);
        return ResponseEntity.ok(profileMetricsService.getMetricsProfileHitsMonthViewsBySteam64Id(id, startDate, endDate));
    }

    /**
     * Returns yearly profile metrics hits for a given Steam ID.
     *
     * @param steam64id Steam ID to query
     * @return list of yearly profile metrics hits
     */
    @GetMapping("/year")
    public ResponseEntity<List<MetricsProfileHitsYearView>> getAllMetricsProfileYearViews(
            @RequestParam String steam64id
    ) throws SteamApiException {
        String id = steamWebAPIService.resolveSteamId(steam64id);
        return ResponseEntity.ok(profileMetricsService.getMetricsProfileHitsYearViewsBySteam64Id(id));
    }

}
