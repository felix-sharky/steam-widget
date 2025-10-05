package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.entity.metrics.MetricsGlobalDayView;
import codes.sharky.steamwidget.entity.metrics.MetricsGlobalFullView;
import codes.sharky.steamwidget.entity.metrics.MetricsGlobalMonthView;
import codes.sharky.steamwidget.entity.metrics.MetricsGlobalYearView;
import codes.sharky.steamwidget.repository.metrics.MetricsGlobalDayViewRepository;
import codes.sharky.steamwidget.repository.metrics.MetricsGlobalFullViewRepository;
import codes.sharky.steamwidget.repository.metrics.MetricsGlobalMonthViewRepository;
import codes.sharky.steamwidget.repository.metrics.MetricsGlobalYearViewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlobalMetricsService {

    private final MetricsGlobalDayViewRepository metricsGlobalDayViewRepository;
    private final MetricsGlobalFullViewRepository metricsGlobalFullViewRepository;
    private final MetricsGlobalMonthViewRepository metricsGlobalMonthViewRepository;
    private final MetricsGlobalYearViewRepository metricsGlobalYearViewRepository;

    public GlobalMetricsService(MetricsGlobalDayViewRepository metricsGlobalDayViewRepository, MetricsGlobalFullViewRepository metricsGlobalFullViewRepository, MetricsGlobalMonthViewRepository metricsGlobalMonthViewRepository, MetricsGlobalYearViewRepository metricsGlobalYearViewRepository) {
        this.metricsGlobalDayViewRepository = metricsGlobalDayViewRepository;
        this.metricsGlobalFullViewRepository = metricsGlobalFullViewRepository;
        this.metricsGlobalMonthViewRepository = metricsGlobalMonthViewRepository;
        this.metricsGlobalYearViewRepository = metricsGlobalYearViewRepository;
    }

    public List<MetricsGlobalDayView> getAllMetricsGlobalDayViews() {
        return metricsGlobalDayViewRepository.findAll();
    }

    public List<MetricsGlobalFullView> getAllMetricsGlobalFullViews() {
        return metricsGlobalFullViewRepository.findAll();
    }

    public List<MetricsGlobalMonthView> getAllMetricsGlobalMonthViews() {
        return metricsGlobalMonthViewRepository.findAll();
    }

    public List<MetricsGlobalYearView> getAllMetricsGlobalYearViews() {
        return metricsGlobalYearViewRepository.findAll();
    }

}
