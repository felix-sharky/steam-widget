package codes.sharky.steamwidget.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
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
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TrackingProfileMonthId entity = (TrackingProfileMonthId) o;
        return Objects.equals(this.steam64id, entity.steam64id) &&
                Objects.equals(this.game, entity.game) &&
                Objects.equals(this.month, entity.month) &&
                Objects.equals(this.year, entity.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(steam64id, game, month, year);
    }

}