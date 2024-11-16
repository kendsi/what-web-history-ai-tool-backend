package cap.team3.what.service;

import java.util.List;

import cap.team3.what.dto.VectorMetaData;

public interface VectorStoreService {
    public void addDocument(VectorMetaData metaData);
    public List<VectorMetaData> searchSimilarDocuments(String query, int topK);
}