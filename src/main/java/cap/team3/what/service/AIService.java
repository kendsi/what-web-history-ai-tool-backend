package cap.team3.what.service;

import java.util.List;

public interface AIService {
    public List<String> extractKeywords(String title, String content);
}
