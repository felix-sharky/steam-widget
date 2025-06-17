package de.gamergrotte.steam.widget.repository.metrics;

import de.gamergrotte.steam.widget.entity.metrics.MetricsGlobalDayView;
import de.gamergrotte.steam.widget.entity.metrics.id.GlobalDayCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MetricsGlobalDayViewRepository extends JpaRepository<MetricsGlobalDayView, GlobalDayCompositeId>, JpaSpecificationExecutor<MetricsGlobalDayView> {

}