package cap.team3.what.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Table(name = "history")
public class History {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    private String domain;
    private int spentTime;

    @ElementCollection
    @CollectionTable(name = "history_keywords", joinColumns = @JoinColumn(name = "history_id"))
    @Column(name = "keyword")
    private List<String> keywords;

    @Builder
    public History(String title, String content, String domain, int spentTime, List<String> keywords) {
        this.title = title;
        this.content = content;
        this.domain = domain;
        this.spentTime = spentTime;
        this.keywords = keywords;
    }
}
