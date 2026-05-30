package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.entity.TrackingProfileDate;
import codes.sharky.steamwidget.entity.TrackingProfileDateId;
import codes.sharky.steamwidget.entity.TrackingProfileInsightsActivity;
import codes.sharky.steamwidget.entity.TrackingProfileInsightsGame;
import codes.sharky.steamwidget.entity.TrackingProfileInsightsPlaytime;
import codes.sharky.steamwidget.entity.TrackingProfileMonth;
import codes.sharky.steamwidget.entity.TrackingProfileMonthId;
import codes.sharky.steamwidget.repository.TrackingProfileDateRepository;
import codes.sharky.steamwidget.repository.TrackingProfileInsightsActivityRepository;
import codes.sharky.steamwidget.repository.TrackingProfileInsightsGameRepository;
import codes.sharky.steamwidget.repository.TrackingProfileInsightsPlaytimeRepository;
import codes.sharky.steamwidget.repository.TrackingProfileMonthRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Service providing tracking data by month and by date, filling gaps with zeroed placeholders
 * so consumers receive continuous time ranges for a profile.
 */
@Service
public class TrackingProfileService {

    private final TrackingProfileMonthRepository monthRepository;
    private final TrackingProfileDateRepository dateRepository;
    private final TrackingProfileInsightsActivityRepository activityRepository;
    private final TrackingProfileInsightsGameRepository gameRepository;
    private final TrackingProfileInsightsPlaytimeRepository playtimeRepository;

    /**
     * Creates the tracking profile service with repositories for monthly, daily and insights tracking data.
     */
    public TrackingProfileService(TrackingProfileMonthRepository monthRepository,
                                  TrackingProfileDateRepository dateRepository,
                                  TrackingProfileInsightsActivityRepository activityRepository,
                                  TrackingProfileInsightsGameRepository gameRepository,
                                  TrackingProfileInsightsPlaytimeRepository playtimeRepository) {
        this.monthRepository = monthRepository;
        this.dateRepository = dateRepository;
        this.activityRepository = activityRepository;
        this.gameRepository = gameRepository;
        this.playtimeRepository = playtimeRepository;
    }

    /**
     * Retrieves month-level tracking data for a profile and backfills missing months with zeroed entries
     * between the earliest and latest tracked months.
     *
     * @param steamId Steam ID whose monthly tracking data to load
     * @return list of tracking months with gaps filled by placeholder entries
     */
    public List<TrackingProfileMonth> getTrackingProfileMonth(String steamId) {
        return getTrackingProfileMonth(steamId, null, null);
    }

    /**
     * Retrieves month-level tracking data for a profile with optional date filtering.
     * Date filters are interpreted at month granularity (inclusive).
     *
     * @param steamId   Steam ID whose monthly tracking data to load
     * @param startDate optional start date (inclusive)
     * @param endDate   optional end date (inclusive)
     * @return list of tracking months with gaps filled by placeholder entries and filtered by range
     */
    public List<TrackingProfileMonth> getTrackingProfileMonth(String steamId, LocalDate startDate, LocalDate endDate) {
        List<TrackingProfileMonth> months = monthRepository.findByIdSteam64id(steamId);
        if (months.isEmpty()) {
            return months;
        }

        List<YearMonth> byMonth = months.stream().map(month -> YearMonth.of(month.getId().getYear().intValue(), month.getId().getMonth().intValue()))
                .toList();

        YearMonth minMonth = Collections.min(byMonth);
        YearMonth maxMonth = Collections.max(byMonth);
        String profileName = months.getFirst().getName();

        for (YearMonth current = minMonth; !current.isAfter(maxMonth); current = current.plusMonths(1)) {
            if (!byMonth.contains(current)) {
                TrackingProfileMonth missing = new TrackingProfileMonth();
                missing.setId(new TrackingProfileMonthId(steamId, "", Integer.valueOf(current.getYear()).doubleValue(), Integer.valueOf(current.getMonth().getValue()).doubleValue()));
                missing.setGamename("");
                missing.setName(profileName);
                missing.setPlaytimeHours(0L);
                missing.setPlaytimeMinutes(0L);
                months.add(missing);
            }
        }

        YearMonth startMonth = startDate != null ? YearMonth.from(startDate) : null;
        YearMonth endMonth = endDate != null ? YearMonth.from(endDate) : null;
        return months.stream()
                .filter(month -> {
                    YearMonth current = YearMonth.of(month.getId().getYear().intValue(), month.getId().getMonth().intValue());
                    if (startMonth != null && current.isBefore(startMonth)) {
                        return false;
                    }
                    return endMonth == null || !current.isAfter(endMonth);
                })
                .toList();
    }

    /**
     * Retrieves day-level tracking data for a profile and backfills missing days with zeroed entries
     * between the earliest and latest tracked days.
     *
     * @param steamId Steam ID whose daily tracking data to load
     * @return list of tracking days with gaps filled by placeholder entries
     */
    public List<TrackingProfileDate> getTrackingProfileDate(String steamId) {
        return getTrackingProfileDate(steamId, null, null);
    }

    /**
     * Retrieves day-level tracking data for a profile with optional date filtering.
     *
     * @param steamId   Steam ID whose daily tracking data to load
     * @param startDate optional start date (inclusive)
     * @param endDate   optional end date (inclusive)
     * @return list of tracking days with gaps filled by placeholder entries and filtered by range
     */
    public List<TrackingProfileDate> getTrackingProfileDate(String steamId, LocalDate startDate, LocalDate endDate) {
        List<TrackingProfileDate> dates = dateRepository.findByIdSteam64id(steamId);
        if (dates.isEmpty()) {
            return dates;
        }

        List<LocalDate> byDate = dates.stream().map( date -> date.getId().getDate())
                .toList();

        LocalDate minDate = Collections.min(byDate);
        LocalDate maxDate = Collections.max(byDate);
        String profileName = dates.getFirst().getName();

        for (LocalDate current = minDate; !current.isAfter(maxDate); current = current.plusDays(1)) {
            if (!byDate.contains(current)) {
                TrackingProfileDate missing = new TrackingProfileDate();
                missing.setId(new TrackingProfileDateId(steamId, "", current));
                missing.setGamename("");
                missing.setName(profileName);
                missing.setPlaytimeHours(0L);
                missing.setPlaytimeMinutes(0L);
                dates.add(missing);
            }

        }
        return dates.stream()
                .filter(date -> {
                    LocalDate current = date.getId().getDate();
                    if (startDate != null && current.isBefore(startDate)) {
                        return false;
                    }
                    return endDate == null || !current.isAfter(endDate);
                })
                .toList();
    }

    /**
     * Returns streaks-and-activity insights for a single profile.
     */
    public ResponseEntity<TrackingProfileInsightsActivity> getInsightsActivity(String steamId) {
        return activityRepository.findBySteam64id(steamId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Returns game insights for a single profile.
     */
    public ResponseEntity<TrackingProfileInsightsGame> getInsightsGame(String steamId) {
        return gameRepository.findBySteam64id(steamId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Returns playtime stats insights for a single profile.
     */
    public ResponseEntity<TrackingProfileInsightsPlaytime> getInsightsPlaytime(String steamId) {
        return playtimeRepository.findBySteam64id(steamId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
