package codes.sharky.steamwidget.repository.metrics;

import codes.sharky.steamwidget.entity.metrics.MetricsGlobalYearView;
import codes.sharky.steamwidget.entity.metrics.id.GlobalYearCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MetricsGlobalYearViewRepository extends JpaRepository<MetricsGlobalYearView, GlobalYearCompositeId>, JpaSpecificationExecutor<MetricsGlobalYearView> {

}