package cap.team3.what.dto;

import java.util.List;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
public class HistoryDto {

    private Long id;

    private String email;
    private String title;
    private String content;
    private String url;
    
    private int spentTime;
    private int visitCount;
    private LocalDateTime visitTime;
    private List<String> keywords;

    public HistoryDto() {}
    
    @Builder
    public HistoryDto(Long id, String email, String title, String content, String url, int spentTime, int visitCount, LocalDateTime visitTime, List<String> keywords) {
        this.id = id;
        this.email = email;
        this.title = title;
        this.content = content;
        this.url = url;
        this.spentTime = spentTime;
        this.visitCount = visitCount;
        this.visitTime = visitTime;
        this.keywords = keywords;
    }
}
