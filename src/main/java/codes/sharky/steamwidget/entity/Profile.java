package codes.sharky.steamwidget.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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

}
