package cap.team3.what.service;

import java.util.List;

import cap.team3.what.dto.VectorMetaData;

public interface PineconeService {
    public String saveDocument(VectorMetaData metaData);
    public void updateDocument(VectorMetaData metaData);
    public void deleteDocument(String id);
    public List<VectorMetaData> searchDocuments(String query, String email, int topK);
}
