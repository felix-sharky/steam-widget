package codes.sharky.steamwidget.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class TrackingProfileMonthId implements Serializable {
    private static final long serialVersionUID = 3350841574983040324L;
    @Column(name = "steam64id")
    private String steam64id;

    @Column(name = "game")
    private String game;

    @Column(name = "year")
    private Double year;

    @Column(name = "month")
    private Double month;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TrackingProfileMonthId that = (TrackingProfileMonthId) o;
        return Objects.equal(steam64id, that.steam64id) && Objects.equal(game, that.game) && Objects.equal(year, that.year) && Objects.equal(month, that.month);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(steam64id, game, year, month);
    }
}