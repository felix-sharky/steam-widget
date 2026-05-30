package codes.sharky.steamwidget.repository;

import codes.sharky.steamwidget.entity.TrackingProfileInsightsActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingProfileInsightsActivityRepository extends JpaRepository<TrackingProfileInsightsActivity, String> {

    Optional<TrackingProfileInsightsActivity> findBySteam64id(String steam64id);

    List<TrackingProfileInsightsActivity> findAll();
}

