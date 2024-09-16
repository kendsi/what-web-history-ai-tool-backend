package cap.team3.what.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class AIServiceimpl implements AIService {

    @Override
    public List<String> extractKeywords(String title, String content) {
        return new ArrayList<>();
    }
}
