package cap.team3.what.dto;

import java.util.List;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
public class HistoryDto {

    private Long id;
    private String content;

    private VectorMetaData metaData;
    
    private int spentTime;
    private int visitCount;

    public HistoryDto() {}
    
    @Builder
    public HistoryDto(Long id, String content, VectorMetaData metaData, int spentTime, int visitCount) {
        this.id = id;
        this.content = content;
        this.metaData = metaData;
        this.spentTime = spentTime;
        this.visitCount = visitCount;
    }
}
