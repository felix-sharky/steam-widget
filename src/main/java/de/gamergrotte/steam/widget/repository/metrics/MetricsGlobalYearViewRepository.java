package de.gamergrotte.steam.widget.repository.metrics;

import de.gamergrotte.steam.widget.entity.metrics.MetricsGlobalYearView;
import de.gamergrotte.steam.widget.entity.metrics.id.GlobalYearCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MetricsGlobalYearViewRepository extends JpaRepository<MetricsGlobalYearView, GlobalYearCompositeId>, JpaSpecificationExecutor<MetricsGlobalYearView> {

}