package codes.sharky.steamwidget.entity.metrics;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import codes.sharky.steamwidget.entity.metrics.id.ProfileYearCompositeId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "metrics_profile-hits_year-view")
public class MetricsProfileHitsYearView implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @JsonUnwrapped
    private ProfileYearCompositeId id;

    @Column(name = "name")
    private String name;

    @Column(name = "count")
    private Long count;

}
