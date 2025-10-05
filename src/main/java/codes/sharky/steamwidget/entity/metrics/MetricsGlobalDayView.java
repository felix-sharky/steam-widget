package codes.sharky.steamwidget.entity.metrics;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import codes.sharky.steamwidget.entity.metrics.id.GlobalDayCompositeId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "metrics_global_day-view")
public class MetricsGlobalDayView implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @JsonUnwrapped
    private GlobalDayCompositeId globalDayCompositeId;

    @Column(name = "count")
    private Long count;

}
