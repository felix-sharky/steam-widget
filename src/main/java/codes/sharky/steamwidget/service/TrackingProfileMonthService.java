package codes.sharky.steamwidget.service;

import codes.sharky.steamwidget.entity.TrackingProfileMonth;
import codes.sharky.steamwidget.repository.TrackingProfileMonthRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackingProfileMonthService {

    private final TrackingProfileMonthRepository repository;

    public TrackingProfileMonthService(TrackingProfileMonthRepository repository) {
        this.repository = repository;
    }

    public List<TrackingProfileMonth> getTrackingProfileMonth(String steamId) {
        return repository.findByIdSteam64id(steamId);
    }
}

