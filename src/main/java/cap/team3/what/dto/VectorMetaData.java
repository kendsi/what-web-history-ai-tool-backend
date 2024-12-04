package cap.team3.what.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class VectorMetaData {
    private String id;
    private String email;
    private String url;
    private String title;
    private LocalDateTime visitTime;
    private String shortSummary;
    private String longSummary;
    private String domain;
    private String category;
    private List<String> keywords;
    private int spentTime;
    private int visitCount;

    @Builder
    public VectorMetaData(String id, String email, String url, String title, LocalDateTime visitTime, String shortSummary, String longSummary, String domain, String category, List<String> keywords, int spentTime, int visitCount) {
        this.id = id;
        this.email = email;
        this.url = url;
        this.title = title;
        this.visitTime = visitTime;
        this.shortSummary = shortSummary;
        this.longSummary = longSummary;
        this.domain = domain;
        this.category = category;
        this.keywords = keywords;
        this.spentTime = spentTime;
        this.visitCount = visitCount;
    }
}
