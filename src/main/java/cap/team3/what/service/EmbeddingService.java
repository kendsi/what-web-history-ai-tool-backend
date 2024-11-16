package cap.team3.what.service;

import java.util.List;

public interface EmbeddingService {
    public List<Float> embeddingVector(String content);
}
