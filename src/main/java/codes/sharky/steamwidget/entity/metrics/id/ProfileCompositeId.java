package codes.sharky.steamwidget.entity.metrics.id;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class ProfileCompositeId {

    @Column(name = "steam64id")
    private String steam64id;

    @Column(name = "purpose")
    private String purpose;

}
