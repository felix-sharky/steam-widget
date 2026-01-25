package codes.sharky.steamwidget.entity;

import com.google.common.base.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@Embeddable
public class TrackingProfileDateId implements Serializable {
    private static final long serialVersionUID = 2071069361682689632L;
    @Column(name = "steam64id")
    private String steam64id;

    @Column(name = "game")
    private String game;

    @Column(name = "date")
    private LocalDate date;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TrackingProfileDateId that = (TrackingProfileDateId) o;
        return Objects.equal(steam64id, that.steam64id) && Objects.equal(game, that.game) && Objects.equal(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(steam64id, game, date);
    }
}