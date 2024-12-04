package cap.team3.what.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SearchRequestDto {
    private String query;
    private String domain;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String category;

    public SearchRequestDto (String query, String domain, LocalDateTime startTime, LocalDateTime endTime, String category) {
        this.query = query;
        this.domain = domain;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
    }
}
