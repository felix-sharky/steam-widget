package codes.sharky.steamwidget.controller;

import codes.sharky.steamwidget.entity.metrics.MetricsGlobalDayView;
import codes.sharky.steamwidget.entity.metrics.MetricsGlobalFullView;
import codes.sharky.steamwidget.entity.metrics.MetricsGlobalMonthView;
import codes.sharky.steamwidget.entity.metrics.MetricsGlobalYearView;
import codes.sharky.steamwidget.service.GlobalMetricsService;
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
