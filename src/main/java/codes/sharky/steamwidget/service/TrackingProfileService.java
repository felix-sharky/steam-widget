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

@Service
public class TrackingProfileService {

    private final TrackingProfileMonthRepository monthRepository;

    private final TrackingProfileDateRepository dateRepository;

    public TrackingProfileService(TrackingProfileMonthRepository monthRepository, TrackingProfileDateRepository dateRepository) {
        this.monthRepository = monthRepository;
        this.dateRepository = dateRepository;
    }

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

