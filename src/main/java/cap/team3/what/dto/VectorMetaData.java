package cap.team3.what.dto;

import java.util.List;

import lombok.Data;

@Data
public class VectorMetaData {
    String longSummary;
    String shortSummary;

    List<String> keywords;
}
