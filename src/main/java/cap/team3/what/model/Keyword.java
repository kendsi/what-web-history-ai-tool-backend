package cap.team3.what.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "keyword")
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    @ManyToMany(mappedBy = "keywords")
    private List<History> histories;

    public Keyword() {}

    @Builder
    public Keyword(String keyword) {
        this.keyword = keyword;
    }
}