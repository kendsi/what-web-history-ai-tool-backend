package cap.team3.what.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class HistoryDto {

    private Long id;

    private String title;
    private String content;
    private String domain;
    private int spentTime;
    private List<String> keywords;
    
    @Builder
    public HistoryDto(Long id, String title, String content, String domain, int spentTime, List<String> keywords) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.domain = domain;
        this.spentTime = spentTime;
        this.keywords = keywords;
    }
}
