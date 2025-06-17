package de.gamergrotte.steam.widget.entity.metrics;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.gamergrotte.steam.widget.entity.metrics.id.ProfileDayCompositeId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "metrics_profile-hits_day-view")
public class MetricsProfileHitsDayView implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @JsonUnwrapped
    private ProfileDayCompositeId id;

    @Column(name = "name")
    private String name;

    @Column(name = "count")
    private Long count;

}
