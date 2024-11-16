package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.List;

import cap.team3.what.dto.VectorMetaData;

public interface VectorStoreService {
    public String addDocument(VectorMetaData metaData);
    public void updateDocument(VectorMetaData metaData);
    public List<VectorMetaData> searchSimilarDocuments(String query, int topK, String email);
    public List<VectorMetaData> searchSimilarDocuments(String query, int topK, String email, LocalDateTime starTime, LocalDateTime endTime);
    public void deleteDocument(String id);
}