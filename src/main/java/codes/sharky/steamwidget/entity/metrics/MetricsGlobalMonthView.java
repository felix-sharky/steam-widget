package codes.sharky.steamwidget.entity.metrics;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import codes.sharky.steamwidget.entity.metrics.id.GlobalMonthCompositeId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "metrics_global_month-view")
public class MetricsGlobalMonthView implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @JsonUnwrapped
    private GlobalMonthCompositeId globalMonthCompositeId;

    @Column(name = "count")
    private Long count;

}
