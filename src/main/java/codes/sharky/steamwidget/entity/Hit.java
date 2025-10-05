package codes.sharky.steamwidget.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hits")
public class Hit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "steam64id", nullable = false)
    private String steam64id;

    @Column(name = "datetime", nullable = false)
    private LocalDateTime datetime;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "ip", length = 15)
    private String ip;

    public Hit(String steam64id, LocalDateTime datetime, String purpose, String ip) {
        this.steam64id = steam64id;
        this.datetime = datetime;
        this.purpose = purpose;
        this.ip = ip;
    }
}