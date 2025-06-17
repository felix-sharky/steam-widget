package de.gamergrotte.steam.widget.entity.metrics;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.gamergrotte.steam.widget.entity.metrics.id.ProfileMonthCompositeId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "metrics_profile-hits_month-view")
public class MetricsProfileHitsMonthView implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @JsonUnwrapped
    private ProfileMonthCompositeId id;

    @Column(name = "name")
    private String name;

    @Column(name = "count")
    private Long count;

}
