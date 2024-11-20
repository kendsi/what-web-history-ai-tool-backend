package cap.team3.what.service;

import java.util.List;

import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Service;

import com.google.common.primitives.Floats;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {
    
    private final OpenAiEmbeddingModel embeddingModel;

    public List<Float> embeddingVector(String content) {
        EmbeddingResponse embeddingResponse = embeddingModel.embedForResponse(List.of(content));

        float[] outputArray = embeddingResponse.getResults().get(0).getOutput();
        return Floats.asList(outputArray);
    }
}
