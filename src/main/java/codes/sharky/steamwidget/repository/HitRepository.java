package codes.sharky.steamwidget.repository;

import codes.sharky.steamwidget.entity.Hit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface HitRepository extends JpaRepository<Hit, Integer>, JpaSpecificationExecutor<Hit> {

    long countHitsBySteam64idAndPurpose(String steam64id, String purpose);

    long countHitsBySteam64id(String steam64id);

    /**
     * Nullifies the stored IP address for all hit records older than the given cutoff.
     * This is the least-destructive GDPR retention approach: hit counts and timestamps
     * are preserved for analytics while the personal identifier (IP) is erased.
     *
     * @param cutoff datetime before which IP addresses should be set to null
     * @return number of rows updated
     */
    @Modifying
    @Transactional
    @Query("UPDATE Hit h SET h.ip = NULL WHERE h.datetime < :cutoff AND h.ip IS NOT NULL")
    int nullifyIpBefore(@Param("cutoff") LocalDateTime cutoff);

    /**
     * Deletes all hit records older than the given cutoff.
     * Applied after the long-term retention window (e.g. 2 years) once records are already
     * fully anonymised (IP already nullified). Removes the remaining pseudonymous identifiers
     * (steam64id, purpose, datetime) from the raw table.
     * Aggregated metrics views remain unaffected for the retained period.
     *
     * @param cutoff datetime before which hit records are deleted
     * @return number of rows deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Hit h WHERE h.datetime < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);

}