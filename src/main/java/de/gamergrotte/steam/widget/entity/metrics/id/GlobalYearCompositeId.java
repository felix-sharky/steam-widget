package de.gamergrotte.steam.widget.entity.metrics.id;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class GlobalYearCompositeId {

    @Column(name = "year")
    private Integer year;

}
