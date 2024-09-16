package cap.team3.what.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class HistoryDto {

    private Long id;

    private String title;
    private String content;
    private List<String> keywords;
    private LocalDateTime time;

    public HistoryDto() {}
}
