package codes.sharky.steamwidget.repository.metrics;

import codes.sharky.steamwidget.entity.metrics.MetricsGlobalDayView;
import codes.sharky.steamwidget.entity.metrics.id.GlobalDayCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MetricsGlobalDayViewRepository extends JpaRepository<MetricsGlobalDayView, GlobalDayCompositeId>, JpaSpecificationExecutor<MetricsGlobalDayView> {

}