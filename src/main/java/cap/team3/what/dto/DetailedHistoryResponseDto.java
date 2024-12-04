package cap.team3.what.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class DetailedHistoryResponseDto {
    private Long id;
    private String url;
    private String title;
    private LocalDateTime visitTime;
    private String shortSummary;
    private String longSummary;
    private String category;
    private List<String> keywords;
    private int spentTime;
    private int visitCount;

    @Builder
    public DetailedHistoryResponseDto(Long id, String url, String title, LocalDateTime visitTime, String shortSummary, String longSummary, String category, List<String> keywords, int spentTime, int visitCount) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.visitTime = visitTime;
        this.shortSummary = shortSummary;
        this.longSummary = longSummary;
        this.category = category;
        this.keywords = keywords;
        this.spentTime = spentTime;
        this.visitCount = visitCount;
    }
}
