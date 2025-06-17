package de.gamergrotte.steam.widget.repository.metrics;

import de.gamergrotte.steam.widget.entity.metrics.MetricsGlobalMonthView;
import de.gamergrotte.steam.widget.entity.metrics.id.GlobalMonthCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MetricsGlobalMonthViewRepository extends JpaRepository<MetricsGlobalMonthView, GlobalMonthCompositeId>, JpaSpecificationExecutor<MetricsGlobalMonthView> {

}