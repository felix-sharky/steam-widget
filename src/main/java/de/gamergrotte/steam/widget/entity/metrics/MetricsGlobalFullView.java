package de.gamergrotte.steam.widget.entity.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "metrics_global_full-view")
public class MetricsGlobalFullView implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @JsonIgnore
    private Integer id;

    @Column(name = "count")
    private Long count;

}
