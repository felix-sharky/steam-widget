package codes.sharky.steamwidget.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "profile")
@AllArgsConstructor
@NoArgsConstructor
public class Profile implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "steam64id", nullable = false)
    private String steam64id;

    @Column(name = "name")
    private String name;

    @Column(name = "hits")
    private Long hits;

    @ColumnDefault("false")
    @Column(name = "tracking", nullable = false)
    private Boolean tracking = false;

    @ColumnDefault("false")
    @Column(name = "caching", nullable = false)
    private Boolean caching = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "steam64id")
    private ProfileCache profileCache;

    @ColumnDefault("false")
    @Column(name = "claimed", nullable = false)
    private Boolean claimed = false;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @ColumnDefault("true")
    @Column(name = "show_now_playing", nullable = false)
    private Boolean showNowPlaying = true;

    @ColumnDefault("true")
    @Column(name = "show_headline_stats", nullable = false)
    private Boolean showHeadlineStats = true;

    @ColumnDefault("true")
    @Column(name = "show_top_games", nullable = false)
    private Boolean showTopGames = true;

    @ColumnDefault("true")
    @Column(name = "show_widget_gallery", nullable = false)
    private Boolean showWidgetGallery = true;

    public Profile(String steam64id, String name, Long hits) {
        this.steam64id = steam64id;
        this.name = name;
        this.hits = hits;
    }

    public Profile(String steam64id, String name, Long hits, Boolean tracking, Boolean caching) {
        this.steam64id = steam64id;
        this.name = name;
        this.hits = hits;
        this.tracking = tracking;
        this.caching = caching;
    }

}
