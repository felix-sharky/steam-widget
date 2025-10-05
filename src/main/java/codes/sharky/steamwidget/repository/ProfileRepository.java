package codes.sharky.steamwidget.repository;

import codes.sharky.steamwidget.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, String>, JpaSpecificationExecutor<Profile> {

    @Modifying
    @Transactional
    @Query(value = "update Profile p set p.hits = p.hits + 1, p.name = :name where p.steam64id = :steam64Id")
    void incrementHits(@Param("steam64Id") String steam64Id, @Param("name") String name);

    @Modifying
    @Transactional
    @Query(value = "update Profile p set p.name = :name, p.tracking = :tracking where p.steam64id = :steamId")
    void updateNameAndTracking(String steamId, String name, boolean tracking);

    List<Profile> findAllByTrackingIsTrue();

    boolean existsBySteam64idAndTrackingIsTrue(String steam64id);

    @Modifying
    @Transactional
    @Query(value = "update Profile p set p.tracking = (not p.tracking) where p.steam64id = :steamId")
    void toggleTracking(String steamId);
}