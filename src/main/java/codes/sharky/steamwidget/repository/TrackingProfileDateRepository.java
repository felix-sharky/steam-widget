package codes.sharky.steamwidget.repository;

import codes.sharky.steamwidget.entity.TrackingProfileDate;
import codes.sharky.steamwidget.entity.TrackingProfileDateId;
import codes.sharky.steamwidget.entity.TrackingProfileMonth;
import codes.sharky.steamwidget.entity.TrackingProfileMonthId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingProfileDateRepository extends JpaRepository<TrackingProfileDate, TrackingProfileDateId> {

    List<TrackingProfileDate> findByIdSteam64id(String steam64Id);
    
}
