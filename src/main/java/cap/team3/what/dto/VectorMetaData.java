package cap.team3.what.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class VectorMetaData {
    private String id;
    private String email;
    private String url;
    private String longSummary;
    private String shortSummary;
    private List<String> keywords;
    private int spentTime;
    private int visitCount;
    private LocalDateTime visitTime;

    public VectorMetaData() {}
}
