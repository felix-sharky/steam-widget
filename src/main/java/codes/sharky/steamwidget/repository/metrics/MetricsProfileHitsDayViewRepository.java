package codes.sharky.steamwidget.repository.metrics;

import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsDayView;
import codes.sharky.steamwidget.entity.metrics.id.ProfileDayCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MetricsProfileHitsDayViewRepository extends JpaRepository<MetricsProfileHitsDayView, ProfileDayCompositeId>, JpaSpecificationExecutor<MetricsProfileHitsDayView> {

    List<MetricsProfileHitsDayView> findAllByIdSteam64id (String steam64id);

}