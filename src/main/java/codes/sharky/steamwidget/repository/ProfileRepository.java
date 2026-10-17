package codes.sharky.steamwidget.repository;

import codes.sharky.steamwidget.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Modifying
    @Transactional
    @Query(value = "update Profile p set p.name = :name, p.caching = :caching where p.steam64id = :steamId")
    void updateNameAndCaching(String steamId, String name, boolean caching);

    List<Profile> findAllByTrackingIsTrue();

    boolean existsBySteam64idAndTrackingIsTrue(String steam64id);

    boolean existsBySteam64idAndCachingIsTrue(String steam64id);

    List<Profile> findAllByCachingIsTrueAndProfileCache_LastrequestBefore(LocalDateTime lastrequestBefore);

    List<Profile> findAllByCachingIsTrue();

    @Modifying
    @Transactional
    @Query(value = "update Profile p set p.tracking = (not p.tracking) where p.steam64id = :steamId")
    void toggleTracking(String steamId);

    @Modifying
    @Transactional
    @Query(value = "update Profile p set p.claimed = true, p.claimedAt = :claimedAt where p.steam64id = :steamId")
    void claimProfile(@Param("steamId") String steamId, @Param("claimedAt") LocalDateTime claimedAt);

    @Modifying
    @Transactional
    @Query(value = "update Profile p set p.claimed = false, p.claimedAt = null, p.showNowPlaying = true, " +
            "p.showHeadlineStats = true, p.showTopGames = true, p.showWidgetGallery = true where p.steam64id = :steamId")
    void unclaimProfile(@Param("steamId") String steamId);

    @Modifying
    @Transactional
    @Query(value = "update Profile p set p.showNowPlaying = :showNowPlaying, p.showHeadlineStats = :showHeadlineStats, " +
            "p.showTopGames = :showTopGames, p.showWidgetGallery = :showWidgetGallery where p.steam64id = :steamId")
    void updateSectionVisibility(@Param("steamId") String steamId,
                                  @Param("showNowPlaying") boolean showNowPlaying,
                                  @Param("showHeadlineStats") boolean showHeadlineStats,
                                  @Param("showTopGames") boolean showTopGames,
                                  @Param("showWidgetGallery") boolean showWidgetGallery);

    boolean existsBySteam64idAndClaimedIsTrue(String steam64id);

    List<Profile> findAllByClaimedIsTrue();
}