package codes.sharky.steamwidget.repository.metrics;

import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsMonthView;
import codes.sharky.steamwidget.entity.metrics.id.ProfileMonthCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MetricsProfileHitsMonthViewRepository extends JpaRepository<MetricsProfileHitsMonthView, ProfileMonthCompositeId>, JpaSpecificationExecutor<MetricsProfileHitsMonthView> {

    List<MetricsProfileHitsMonthView> findAllByIdSteam64id (String steam64id);

}