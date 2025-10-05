package codes.sharky.steamwidget.repository.metrics;

import codes.sharky.steamwidget.entity.metrics.MetricsGlobalMonthView;
import codes.sharky.steamwidget.entity.metrics.id.GlobalMonthCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MetricsGlobalMonthViewRepository extends JpaRepository<MetricsGlobalMonthView, GlobalMonthCompositeId>, JpaSpecificationExecutor<MetricsGlobalMonthView> {

}