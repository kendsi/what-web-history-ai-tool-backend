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

    @Column(length = 765)
    private String url;

    private int spentTime;

    private int visitCount;

    private LocalDateTime visitTime;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "history_keyword_mapping",
        joinColumns = @JoinColumn(name = "history_id"),
        inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    private List<Keyword> keywords;

    @Builder
    public History(String userId, String url, String title, String content, int spentTime, int visitCount, LocalDateTime visitTime, List<Keyword> keywords) {
        this.userId = userId;
        this.url = url;
        this.title = title;
        this.content = content;
        this.spentTime = spentTime;
        this.visitCount = visitCount;
        this.visitTime = visitTime;
        this.keywords = keywords;
    }

    public History() {}
}