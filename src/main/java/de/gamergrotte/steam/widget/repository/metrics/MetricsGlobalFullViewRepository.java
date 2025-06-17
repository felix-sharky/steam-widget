package de.gamergrotte.steam.widget.repository.metrics;

import de.gamergrotte.steam.widget.entity.metrics.MetricsGlobalFullView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MetricsGlobalFullViewRepository extends JpaRepository<MetricsGlobalFullView, Integer>, JpaSpecificationExecutor<MetricsGlobalFullView> {

}