package codes.sharky.steamwidget.repository;

import codes.sharky.steamwidget.entity.TrackingProfileInsightsGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingProfileInsightsGameRepository extends JpaRepository<TrackingProfileInsightsGame, String> {

    Optional<TrackingProfileInsightsGame> findBySteam64id(String steam64id);

    List<TrackingProfileInsightsGame> findAll();
}

