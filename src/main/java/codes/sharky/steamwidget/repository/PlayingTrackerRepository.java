package codes.sharky.steamwidget.repository;

import codes.sharky.steamwidget.entity.Hit;
import codes.sharky.steamwidget.entity.PlayingTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PlayingTrackerRepository extends JpaRepository<PlayingTracker, Integer>, JpaSpecificationExecutor<PlayingTracker> {

    Optional<PlayingTracker> findFirstBySteam64idAndGameOrderByDatetimeDesc(String steam64id, String game);

    @Modifying
    @Transactional
    void deleteAllBySteam64id(String steam64id);

}
