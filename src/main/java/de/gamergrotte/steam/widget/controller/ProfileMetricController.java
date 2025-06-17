package de.gamergrotte.steam.widget.controller;

import com.lukaspradel.steamapi.core.exception.SteamApiException;
import de.gamergrotte.steam.widget.entity.metrics.MetricsProfileHitsDayView;
import de.gamergrotte.steam.widget.entity.metrics.MetricsProfileHitsFullView;
import de.gamergrotte.steam.widget.entity.metrics.MetricsProfileHitsMonthView;
import de.gamergrotte.steam.widget.entity.metrics.MetricsProfileHitsYearView;
import de.gamergrotte.steam.widget.service.ProfileMetricsService;
import de.gamergrotte.steam.widget.service.SteamWidgetService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/api/metrics/profile")
public class ProfileMetricController {

    private final ProfileMetricsService profileMetricsService;
    private final SteamWidgetService steamWidgetService;

    public ProfileMetricController(ProfileMetricsService profileMetricsService, SteamWidgetService steamWidgetService) {
        this.profileMetricsService = profileMetricsService;
        this.steamWidgetService = steamWidgetService;
    }

    @GetMapping("/day")
    public @ResponseBody List<MetricsProfileHitsDayView> getAllMetricsProfileDayViews(@RequestParam String steam64id) throws SteamApiException {
        String id = steamWidgetService.resolveSteamId(steam64id);
        return profileMetricsService.getMetricsProfileHitsDayViewsBySteam64Id(steam64id);
    }

    @GetMapping("/full")
    public @ResponseBody List<MetricsProfileHitsFullView> getAllMetricsProfileFullViews(@RequestParam String steam64id) throws SteamApiException {
        String id = steamWidgetService.resolveSteamId(steam64id);
        return profileMetricsService.getMetricsProfileHitsFullViewsBySteam64Id(steam64id);
    }

    @GetMapping("/month")
    public @ResponseBody List<MetricsProfileHitsMonthView> getAllMetricsProfileMonthViews(@RequestParam String steam64id) throws SteamApiException {
        String id = steamWidgetService.resolveSteamId(steam64id);
        return profileMetricsService.getMetricsProfileHitsMonthViewsBySteam64Id(steam64id);
    }

    @GetMapping("/year")
    public @ResponseBody List<MetricsProfileHitsYearView> getAllMetricsProfileYearViews(@RequestParam String steam64id) throws SteamApiException {
        String id = steamWidgetService.resolveSteamId(steam64id);
        return profileMetricsService.getMetricsProfileHitsYearViewsBySteam64Id(steam64id);
    }

}
