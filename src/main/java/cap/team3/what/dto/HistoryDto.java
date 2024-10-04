package cap.team3.what.dto;

import java.util.List;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
public class HistoryDto {

    private Long id;

    private String userId;
    private String title;
    private String content;
    private String url;
    
    private int spentTime;
    private LocalDateTime visitTime;
    private List<String> keywords;
    
    @Builder
    public HistoryDto(Long id, String userId, String title, String content, String url, int spentTime, LocalDateTime visitTime, List<String> keywords) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.url = url;
        this.spentTime = spentTime;
        this.visitTime = visitTime;
        this.keywords = keywords;
    }
}
