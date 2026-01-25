package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.entity.TrackingProfileDate;
import codes.sharky.steamwidget.entity.TrackingProfileMonth;
import codes.sharky.steamwidget.repository.TrackingProfileDateRepository;
import codes.sharky.steamwidget.repository.TrackingProfileMonthRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackingProfileService {

    private final TrackingProfileMonthRepository monthRepository;

    private final TrackingProfileDateRepository dateRepository;

    public TrackingProfileService(TrackingProfileMonthRepository monthRepository, TrackingProfileDateRepository dateRepository) {
        this.monthRepository = monthRepository;
        this.dateRepository = dateRepository;
    }

    public List<TrackingProfileMonth> getTrackingProfileMonth(String steamId) {
        return monthRepository.findByIdSteam64id(steamId);
    }

    public List<TrackingProfileDate> getTrackingProfileDate(String steamId) {
        return dateRepository.findByIdSteam64id(steamId);
    }
}

