package de.gamergrotte.steam.widget.repository.metrics;

import de.gamergrotte.steam.widget.entity.metrics.MetricsProfileHitsDayView;
import de.gamergrotte.steam.widget.entity.metrics.id.ProfileDayCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MetricsProfileHitsDayViewRepository extends JpaRepository<MetricsProfileHitsDayView, ProfileDayCompositeId>, JpaSpecificationExecutor<MetricsProfileHitsDayView> {

    List<MetricsProfileHitsDayView> findAllByIdSteam64id (String steam64id);

}