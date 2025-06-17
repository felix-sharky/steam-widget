package de.gamergrotte.steam.widget.repository.metrics;

import de.gamergrotte.steam.widget.entity.metrics.MetricsProfileHitsFullView;
import de.gamergrotte.steam.widget.entity.metrics.id.ProfileCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MetricsProfileHitsFullViewRepository extends JpaRepository<MetricsProfileHitsFullView, ProfileCompositeId>, JpaSpecificationExecutor<MetricsProfileHitsFullView> {

    List<MetricsProfileHitsFullView> findAllByIdSteam64id (String steam64id);

}