package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.entity.TrackingProfileDate;
import codes.sharky.steamwidget.entity.TrackingProfileDateId;
import codes.sharky.steamwidget.entity.TrackingProfileMonth;
import codes.sharky.steamwidget.entity.TrackingProfileMonthId;
import codes.sharky.steamwidget.repository.TrackingProfileDateRepository;
import codes.sharky.steamwidget.repository.TrackingProfileMonthRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service providing tracking data by month and by date, filling gaps with zeroed placeholders
 * so consumers receive continuous time ranges for a profile.
 */
@Service
public class TrackingProfileService {

    private final TrackingProfileMonthRepository monthRepository;

    private final TrackingProfileDateRepository dateRepository;

    /**
     * Creates the tracking profile service with repositories for monthly and daily tracking data.
     *
     * @param monthRepository repository for month-level tracking aggregates
     * @param dateRepository  repository for day-level tracking entries
     */
    public TrackingProfileService(TrackingProfileMonthRepository monthRepository, TrackingProfileDateRepository dateRepository) {
        this.monthRepository = monthRepository;
        this.dateRepository = dateRepository;
    }

    /**
     * Retrieves month-level tracking data for a profile and backfills missing months with zeroed entries
     * between the earliest and latest tracked months.
     *
     * @param steamId Steam ID whose monthly tracking data to load
     * @return list of tracking months with gaps filled by placeholder entries
     */
    public List<TrackingProfileMonth> getTrackingProfileMonth(String steamId) {
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

        return months;
    }

    /**
     * Retrieves day-level tracking data for a profile and backfills missing days with zeroed entries
     * between the earliest and latest tracked days.
     *
     * @param steamId Steam ID whose daily tracking data to load
     * @return list of tracking days with gaps filled by placeholder entries
     */
    public List<TrackingProfileDate> getTrackingProfileDate(String steamId) {
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

        return dates;
    }
}
