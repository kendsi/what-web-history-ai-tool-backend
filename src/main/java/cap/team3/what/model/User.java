package cap.team3.what.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "what_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<History> histories;

    public User() {}

    @Builder
    public User(String email) {
        this.email = email;
    }
}