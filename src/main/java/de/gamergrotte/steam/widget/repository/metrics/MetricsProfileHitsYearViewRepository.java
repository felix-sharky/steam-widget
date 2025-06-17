package de.gamergrotte.steam.widget.repository.metrics;

import de.gamergrotte.steam.widget.entity.metrics.MetricsProfileHitsYearView;
import de.gamergrotte.steam.widget.entity.metrics.id.ProfileYearCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MetricsProfileHitsYearViewRepository extends JpaRepository<MetricsProfileHitsYearView, ProfileYearCompositeId>, JpaSpecificationExecutor<MetricsProfileHitsYearView> {

    List<MetricsProfileHitsYearView> findAllByIdSteam64id (String steam64id);

}