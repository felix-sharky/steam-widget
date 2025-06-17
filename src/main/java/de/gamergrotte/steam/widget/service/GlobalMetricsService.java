package de.gamergrotte.steam.widget.service;

import de.gamergrotte.steam.widget.entity.metrics.MetricsGlobalDayView;
import de.gamergrotte.steam.widget.entity.metrics.MetricsGlobalFullView;
import de.gamergrotte.steam.widget.entity.metrics.MetricsGlobalMonthView;
import de.gamergrotte.steam.widget.entity.metrics.MetricsGlobalYearView;
import de.gamergrotte.steam.widget.repository.metrics.MetricsGlobalDayViewRepository;
import de.gamergrotte.steam.widget.repository.metrics.MetricsGlobalFullViewRepository;
import de.gamergrotte.steam.widget.repository.metrics.MetricsGlobalMonthViewRepository;
import de.gamergrotte.steam.widget.repository.metrics.MetricsGlobalYearViewRepository;
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
