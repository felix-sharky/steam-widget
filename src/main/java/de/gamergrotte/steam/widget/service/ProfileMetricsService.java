package de.gamergrotte.steam.widget.service;

import de.gamergrotte.steam.widget.entity.metrics.MetricsProfileHitsDayView;
import de.gamergrotte.steam.widget.entity.metrics.MetricsProfileHitsFullView;
import de.gamergrotte.steam.widget.entity.metrics.MetricsProfileHitsMonthView;
import de.gamergrotte.steam.widget.entity.metrics.MetricsProfileHitsYearView;
import de.gamergrotte.steam.widget.repository.metrics.MetricsProfileHitsDayViewRepository;
import de.gamergrotte.steam.widget.repository.metrics.MetricsProfileHitsFullViewRepository;
import de.gamergrotte.steam.widget.repository.metrics.MetricsProfileHitsMonthViewRepository;
import de.gamergrotte.steam.widget.repository.metrics.MetricsProfileHitsYearViewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileMetricsService {

    private final MetricsProfileHitsDayViewRepository metricsProfileHitsDayViewRepository;
    private final MetricsProfileHitsFullViewRepository metricsProfileHitsFullViewRepository;
    private final MetricsProfileHitsMonthViewRepository metricsProfileHitsMonthViewRepository;
    private final MetricsProfileHitsYearViewRepository metricsProfileHitsYearViewRepository;

    public ProfileMetricsService(MetricsProfileHitsDayViewRepository metricsProfileHitsDayViewRepository, MetricsProfileHitsFullViewRepository metricsProfileHitsFullViewRepository, MetricsProfileHitsMonthViewRepository metricsProfileHitsMonthViewRepository, MetricsProfileHitsYearViewRepository metricsProfileHitsYearViewRepository) {
        this.metricsProfileHitsDayViewRepository = metricsProfileHitsDayViewRepository;
        this.metricsProfileHitsFullViewRepository = metricsProfileHitsFullViewRepository;
        this.metricsProfileHitsMonthViewRepository = metricsProfileHitsMonthViewRepository;
        this.metricsProfileHitsYearViewRepository = metricsProfileHitsYearViewRepository;
    }

    public List<MetricsProfileHitsDayView> getMetricsProfileHitsDayViewsBySteam64Id(String steam64Id) {
        return metricsProfileHitsDayViewRepository.findAllByIdSteam64id(steam64Id);
    }

    public List<MetricsProfileHitsFullView> getMetricsProfileHitsFullViewsBySteam64Id(String steam64Id) {
        return metricsProfileHitsFullViewRepository.findAllByIdSteam64id(steam64Id);
    }

    public List<MetricsProfileHitsMonthView> getMetricsProfileHitsMonthViewsBySteam64Id(String steam64Id) {
        return metricsProfileHitsMonthViewRepository.findAllByIdSteam64id(steam64Id);
    }

    public List<MetricsProfileHitsYearView> getMetricsProfileHitsYearViewsBySteam64Id(String steam64Id) {
        return metricsProfileHitsYearViewRepository.findAllByIdSteam64id(steam64Id);
    }

}
