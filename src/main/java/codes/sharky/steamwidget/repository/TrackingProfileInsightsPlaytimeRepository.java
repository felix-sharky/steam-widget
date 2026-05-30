package codes.sharky.steamwidget.repository;

import codes.sharky.steamwidget.entity.TrackingProfileInsightsPlaytime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingProfileInsightsPlaytimeRepository extends JpaRepository<TrackingProfileInsightsPlaytime, String> {

    Optional<TrackingProfileInsightsPlaytime> findBySteam64id(String steam64id);

    List<TrackingProfileInsightsPlaytime> findAll();
}

