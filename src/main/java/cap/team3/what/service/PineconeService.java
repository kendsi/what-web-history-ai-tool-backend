package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.List;

import cap.team3.what.dto.VectorMetaData;

public interface PineconeService {
    public void saveDocument(VectorMetaData metaData);
    public void saveDocument(VectorMetaData metaData, List<Float> embeddingVector);
    public List<Float> getVector(String id);
    public void updateDocument(VectorMetaData metaData);
    public void deleteDocument(String id);
    public List<VectorMetaData> searchDocuments(String query, String email, int topK);
    public List<VectorMetaData> searchDocuments(String query, String email, int topK, LocalDateTime startTime, LocalDateTime endTime);
}
