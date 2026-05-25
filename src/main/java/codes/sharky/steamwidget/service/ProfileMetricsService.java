package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsDayView;
import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsFullView;
import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsMonthView;
import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsYearView;
import codes.sharky.steamwidget.repository.metrics.MetricsProfileHitsDayViewRepository;
import codes.sharky.steamwidget.repository.metrics.MetricsProfileHitsFullViewRepository;
import codes.sharky.steamwidget.repository.metrics.MetricsProfileHitsMonthViewRepository;
import codes.sharky.steamwidget.repository.metrics.MetricsProfileHitsYearViewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Service providing profile metrics hits data with optional date range filtering.
 */
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
        return getMetricsProfileHitsDayViewsBySteam64Id(steam64Id, null, null);
    }

    /**
     * Get daily metrics hits with optional date range filtering.
     *
     * @param steam64Id Steam64 ID to query
     * @param startDate optional start date (inclusive)
     * @param endDate   optional end date (inclusive)
     * @return list of daily metrics hits filtered by date range
     */
    public List<MetricsProfileHitsDayView> getMetricsProfileHitsDayViewsBySteam64Id(String steam64Id, LocalDate startDate, LocalDate endDate) {
        List<MetricsProfileHitsDayView> metrics = metricsProfileHitsDayViewRepository.findAllByIdSteam64id(steam64Id);
        return filterByDateRange(metrics, startDate, endDate);
    }

    public List<MetricsProfileHitsFullView> getMetricsProfileHitsFullViewsBySteam64Id(String steam64Id) {
        return metricsProfileHitsFullViewRepository.findAllByIdSteam64id(steam64Id);
    }

    public List<MetricsProfileHitsMonthView> getMetricsProfileHitsMonthViewsBySteam64Id(String steam64Id) {
        return getMetricsProfileHitsMonthViewsBySteam64Id(steam64Id, null, null);
    }

    /**
     * Get monthly metrics hits with optional month range filtering.
     * Date filters are interpreted at month granularity (inclusive).
     *
     * @param steam64Id Steam64 ID to query
     * @param startDate optional start date (inclusive, interpreted as start of month)
     * @param endDate   optional end date (inclusive, interpreted as end of month)
     * @return list of monthly metrics hits filtered by date range
     */
    public List<MetricsProfileHitsMonthView> getMetricsProfileHitsMonthViewsBySteam64Id(String steam64Id, LocalDate startDate, LocalDate endDate) {
        List<MetricsProfileHitsMonthView> metrics = metricsProfileHitsMonthViewRepository.findAllByIdSteam64id(steam64Id);
        return filterByMonthRange(metrics, startDate, endDate);
    }

    public List<MetricsProfileHitsYearView> getMetricsProfileHitsYearViewsBySteam64Id(String steam64Id) {
        return metricsProfileHitsYearViewRepository.findAllByIdSteam64id(steam64Id);
    }

    /**
     * Filter metrics by date range (for daily data).
     */
    private List<MetricsProfileHitsDayView> filterByDateRange(List<MetricsProfileHitsDayView> metrics, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return metrics;
        }
        return metrics.stream()
                .filter(metric -> {
                    LocalDate metricDate = LocalDate.of(
                            metric.getId().getYear(),
                            metric.getId().getMonth(),
                            metric.getId().getDay()
                    );
                    if (startDate != null && metricDate.isBefore(startDate)) {
                        return false;
                    }
                    return endDate == null || !metricDate.isAfter(endDate);
                })
                .toList();
    }

    /**
     * Filter metrics by month range (for monthly data).
     * Interprets dates at month granularity.
     */
    private List<MetricsProfileHitsMonthView> filterByMonthRange(List<MetricsProfileHitsMonthView> metrics, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return metrics;
        }
        YearMonth startMonth = startDate != null ? YearMonth.from(startDate) : null;
        YearMonth endMonth = endDate != null ? YearMonth.from(endDate) : null;
        return metrics.stream()
                .filter(metric -> {
                    YearMonth metricMonth = YearMonth.of(metric.getId().getYear(), metric.getId().getMonth());
                    if (startMonth != null && metricMonth.isBefore(startMonth)) {
                        return false;
                    }
                    return endMonth == null || !metricMonth.isAfter(endMonth);
                })
                .toList();
    }

}
