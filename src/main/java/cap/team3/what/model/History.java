package cap.team3.what.model;

import java.util.List;
import java.time.LocalDateTime;

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

    private String userId;

    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    private String url;

    private int spentTime;

    private LocalDateTime visitTime;

    @ElementCollection
    @CollectionTable(name = "history_keywords", joinColumns = @JoinColumn(name = "history_id"))
    @Column(name = "keyword")
    private List<String> keywords;

    @Builder
    public History(String userId, String url, String title, String content, int spentTime, LocalDateTime visitTime, List<String> keywords) {
        this.userId = userId;
        this.url = url;
        this.title = title;
        this.content = content;
        this.spentTime = spentTime;
        this.visitTime = visitTime;
        this.keywords = keywords;
    }

    public History() {}
}
