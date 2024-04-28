package de.gamergrotte.steam.widget.repository;

import de.gamergrotte.steam.widget.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ProfileRepository extends JpaRepository<Profile, String>, JpaSpecificationExecutor<Profile> {

    @Modifying
    @Transactional
    @Query(value = "update Profile p set p.hits = p.hits + 1 where p.steam64id = :steam64Id")
    void incrementHits(@Param("steam64Id") String steam64Id);

}