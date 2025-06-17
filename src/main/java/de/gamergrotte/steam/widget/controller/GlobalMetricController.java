package de.gamergrotte.steam.widget.controller;

import de.gamergrotte.steam.widget.entity.metrics.MetricsGlobalDayView;
import de.gamergrotte.steam.widget.entity.metrics.MetricsGlobalFullView;
import de.gamergrotte.steam.widget.entity.metrics.MetricsGlobalMonthView;
import de.gamergrotte.steam.widget.entity.metrics.MetricsGlobalYearView;
import de.gamergrotte.steam.widget.service.GlobalMetricsService;
import de.gamergrotte.steam.widget.service.SteamWidgetService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/api/metrics/global")
public class GlobalMetricController {

    private final GlobalMetricsService globalMetricsService;

    public GlobalMetricController(GlobalMetricsService globalMetricsService) {
        this.globalMetricsService = globalMetricsService;
    }

    @GetMapping("/day")
    public @ResponseBody List<MetricsGlobalDayView> getAllMetricsGlobalDayViews() {
        return globalMetricsService.getAllMetricsGlobalDayViews();
    }

    @GetMapping("/full")
    public @ResponseBody List<MetricsGlobalFullView> getAllMetricsGlobalFullViews() {
        return globalMetricsService.getAllMetricsGlobalFullViews();
    }

    @GetMapping("/month")
    public @ResponseBody List<MetricsGlobalMonthView> getAllMetricsGlobalMonthViews() {
        return globalMetricsService.getAllMetricsGlobalMonthViews();
    }

    @GetMapping("/year")
    public @ResponseBody List<MetricsGlobalYearView> getAllMetricsGlobalYearViews() {
        return globalMetricsService.getAllMetricsGlobalYearViews();
    }

}
