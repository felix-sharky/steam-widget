package codes.sharky.steamwidget.entity.metrics.id;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class ProfileDayCompositeId {

    @Column(name = "steam64id")
    private String steam64id;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

    @Column(name = "day")
    private Integer day;

}
