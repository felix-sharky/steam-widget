package codes.sharky.steamwidget.repository.metrics;

import codes.sharky.steamwidget.entity.metrics.MetricsProfileHitsYearView;
import codes.sharky.steamwidget.entity.metrics.id.ProfileYearCompositeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MetricsProfileHitsYearViewRepository extends JpaRepository<MetricsProfileHitsYearView, ProfileYearCompositeId>, JpaSpecificationExecutor<MetricsProfileHitsYearView> {

    List<MetricsProfileHitsYearView> findAllByIdSteam64id (String steam64id);

}