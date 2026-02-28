package codes.sharky.steamwidget.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "profile_cache")
@AllArgsConstructor
@NoArgsConstructor
public class ProfileCache {
    @Id
    @Column(name = "steam64id", nullable = false)
    private String steam64id;

    @Column(name = "lastgame")
    private String lastgame;

    @Column(name = "lastpersonastate")
    private Integer lastpersonastate;

    @Column(name = "lastupdate", nullable = false)
    private LocalDateTime lastupdate;

    @Column(name = "lastrequest")
    private LocalDateTime lastrequest;

    public ProfileCache(String steamId) {
        this.steam64id = steamId;
    }

}