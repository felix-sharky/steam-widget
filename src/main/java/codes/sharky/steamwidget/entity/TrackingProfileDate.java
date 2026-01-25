package codes.sharky.steamwidget.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Getter
@Setter
@Entity
@Immutable
@Table(name = "tracking_profile_date")
public class TrackingProfileDate {
    @EmbeddedId
    private TrackingProfileDateId id;

    @Column(name = "name")
    private String name;

    @Column(name = "gamename")
    private String gamename;

    @Column(name = "playtime_hours")
    private Long playtimeHours;

    @Column(name = "playtime_minutes")
    private Long playtimeMinutes;

}