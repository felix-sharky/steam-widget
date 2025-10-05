package codes.sharky.steamwidget.entity.metrics.id;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class GlobalMonthCompositeId {

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private Integer month;

}
