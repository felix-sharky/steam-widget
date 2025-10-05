package codes.sharky.steamwidget.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "playing_tracker")
public class PlayingTracker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "steam64id", nullable = false)
    private String steam64id;

    @Column(name = "game", nullable = false)
    private String game;

    @Column(name = "datetime", nullable = false)
    private LocalDateTime datetime = LocalDateTime.now();

    @Column(name = "gamename")
    private String gamename;

    @ColumnDefault("0")
    @Column(name = "delta_playing_time", nullable = false)
    private Long deltaPlayingTime;

    @ColumnDefault("0")
    @Column(name = "total_playing_time", nullable = false)
    private Long totalPlayingTime;

    public PlayingTracker(String steam64id, String game, String gamename, Long deltaPlayingTime, Long totalPlayingTime) {
        this.steam64id = steam64id;
        this.game = game;
        this.gamename = gamename;
        this.deltaPlayingTime = deltaPlayingTime;
        this.totalPlayingTime = totalPlayingTime;
    }
}