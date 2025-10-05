package codes.sharky.steamwidget.repository.metrics;

import codes.sharky.steamwidget.entity.metrics.MetricsGlobalFullView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MetricsGlobalFullViewRepository extends JpaRepository<MetricsGlobalFullView, Integer>, JpaSpecificationExecutor<MetricsGlobalFullView> {

}