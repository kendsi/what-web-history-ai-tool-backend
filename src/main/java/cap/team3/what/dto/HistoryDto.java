package cap.team3.what.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class HistoryDto {

    private Long id;
    private String content;
    private VectorMetaData metaData;

    public HistoryDto() {}
    
    @Builder
    public HistoryDto(Long id, String content, VectorMetaData metaData, int spentTime, int visitCount) {
        this.id = id;
        this.content = content;
        this.metaData = metaData;
    }
}
