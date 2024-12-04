package cap.team3.what.dto;

import java.util.List;

import lombok.Data;

@Data
public class ParsedChatResponse {
    private String title;
    private String shortSummary;
    private String longSummary;
    private String category;
    private List<String> keywords;

    public ParsedChatResponse(String title, String shortSummary, String longSummary, String category, List<String> keywords) {
        this.title = title;
        this.shortSummary = shortSummary;
        this.longSummary = longSummary;
        this.category = category;
        this.keywords = keywords;
    }
}
