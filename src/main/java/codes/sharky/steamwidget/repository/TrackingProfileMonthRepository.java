package codes.sharky.steamwidget.repository;

import codes.sharky.steamwidget.entity.TrackingProfileMonth;
import codes.sharky.steamwidget.entity.TrackingProfileMonthId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingProfileMonthRepository extends JpaRepository<TrackingProfileMonth, TrackingProfileMonthId> {

    List<TrackingProfileMonth> findByIdSteam64id(String steam64Id);
}

