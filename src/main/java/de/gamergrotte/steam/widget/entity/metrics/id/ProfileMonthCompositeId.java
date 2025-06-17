package de.gamergrotte.steam.widget.entity.metrics.id;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class ProfileMonthCompositeId {

    @Column(name = "steam64id")
    private String steam64id;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;
}
