package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.service.SteamWebAPIService;
import com.lukaspradel.steamapi.core.exception.SteamApiException;
import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsDayView;
import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsFullView;
import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsMonthView;
import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsYearView;
import codes.sharky.steamwidget.service.ProfileMetricsService;
import codes.sharky.steamwidget.service.SteamWidgetService;
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
    private final SteamWebAPIService steamWebAPIService;

    public ProfileMetricController(ProfileMetricsService profileMetricsService, SteamWidgetService steamWidgetService, SteamWebAPIService steamWebAPIService) {
        this.profileMetricsService = profileMetricsService;
        this.steamWidgetService = steamWidgetService;
        this.steamWebAPIService = steamWebAPIService;
    }

    @GetMapping("/day")
    public @ResponseBody List<MetricsProfileHitsDayView> getAllMetricsProfileDayViews(@RequestParam String steam64id) throws SteamApiException {
        String id = steamWebAPIService.resolveSteamId(steam64id);
        return profileMetricsService.getMetricsProfileHitsDayViewsBySteam64Id(id);
    }

    @GetMapping("/full")
    public @ResponseBody List<MetricsProfileHitsFullView> getAllMetricsProfileFullViews(@RequestParam String steam64id) throws SteamApiException {
        String id = steamWebAPIService.resolveSteamId(steam64id);
        return profileMetricsService.getMetricsProfileHitsFullViewsBySteam64Id(id);
    }

    @GetMapping("/month")
    public @ResponseBody List<MetricsProfileHitsMonthView> getAllMetricsProfileMonthViews(@RequestParam String steam64id) throws SteamApiException {
        String id = steamWebAPIService.resolveSteamId(steam64id);
        return profileMetricsService.getMetricsProfileHitsMonthViewsBySteam64Id(id);
    }

    @GetMapping("/year")
    public @ResponseBody List<MetricsProfileHitsYearView> getAllMetricsProfileYearViews(@RequestParam String steam64id) throws SteamApiException {
        String id = steamWebAPIService.resolveSteamId(steam64id);
        return profileMetricsService.getMetricsProfileHitsYearViewsBySteam64Id(id);
    }

}
