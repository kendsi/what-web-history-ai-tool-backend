package cap.team3.what.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
public class HistoryResponseDto {
    private Long id;
    private String url;
    private String title;
    private LocalDateTime visitTime;
    private String shortSummary;

    @Builder
    public HistoryResponseDto(Long id, String url, String title, LocalDateTime visitTime, String shortSummary) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.visitTime = visitTime;
        this.shortSummary = shortSummary;
    }
}
