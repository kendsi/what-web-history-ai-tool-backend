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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(length = 1000)
    private String url;

    private String vectorId;
    private String title;
    @Column(length = 3000)
    private String longSummary;
    @Column(length = 2000)
    private String shortSummary;

    private String category;

    private int spentTime;
    private int visitCount;
    private LocalDateTime visitTime;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "history_keyword_mapping",
        joinColumns = @JoinColumn(name = "history_id"),
        inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    private List<Keyword> keywords;

    @Builder
    public History(User user, String content, String vectorId, String title, String longSummary, String shortSummary, String category, String url, int spentTime, int visitCount, LocalDateTime visitTime, List<Keyword> keywords) {
        this.user = user;
        this.content = content;
        this.vectorId = vectorId;
        this.title = title;
        this.longSummary = longSummary;
        this.shortSummary = shortSummary;
        this.category = category;
        this.url = url;
        this.spentTime = spentTime;
        this.visitCount = visitCount;
        this.visitTime = visitTime;
        this.keywords = keywords;
    }

    public History() {}
}