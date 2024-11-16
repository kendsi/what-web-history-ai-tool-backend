package cap.team3.what.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.PineconeVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import cap.team3.what.dto.VectorMetaData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorStoreServiceImpl implements VectorStoreService {

    private final PineconeVectorStore vectorStore;

    // 문서를 Pinecone에 추가
    @Override
    public String addDocument(VectorMetaData metaData) {
        List<Document> documents = List.of(
            new Document(
                metaData.getLongSummary(),
                Map.of(
                "email", metaData.getEmail(),
                "url", metaData.getUrl(),
                "shortSummary", metaData.getShortSummary(),
                "keywords", metaData.getKeywords(),
                "visitTime", metaData.getVisitTime().toEpochSecond(ZoneOffset.UTC)))
        );

        vectorStore.add(documents);

        return documents.get(0).getId();
    }

    @Override
    public void updateDocument(VectorMetaData metaData) {
        List<Document> documents = List.of(
            new Document(
                metaData.getLongSummary(),
                Map.of(
                "email", metaData.getEmail(),
                "url", metaData.getUrl(),
                "shortSummary", metaData.getShortSummary(),
                "keywords", metaData.getKeywords(),
                "visitTime", metaData.getVisitTime().toEpochSecond(ZoneOffset.UTC)))
        );

        vectorStore.write(documents);
    }

    // 유사한 문서 검색
    @Override
    public List<VectorMetaData> searchSimilarDocuments(String query, int topK, String email) {

        return vectorStore.similaritySearch(SearchRequest
                                            .query(query)
                                            .withTopK(topK)
                                            .withFilterExpression("email == '" + email + "'"))
            .stream()
            .map(this::mapDocumentToVectorMetaData) // Document를 VectorMetaData로 매핑
            .collect(Collectors.toList());
    }

    @Override
    public List<VectorMetaData> searchSimilarDocuments(String query, int topK, String email, LocalDateTime startTime, LocalDateTime endTime) {

        long startTimestamp = startTime.toEpochSecond(ZoneOffset.UTC);
        long endTimestamp = endTime.toEpochSecond(ZoneOffset.UTC);

        return vectorStore.similaritySearch(SearchRequest
                                            .query(query)
                                            .withTopK(topK)
                                            .withFilterExpression("email == '" + email + "'" + 
                                            " && visitTime >= " + startTimestamp + 
                                            " && visitTime <= " + endTimestamp))
            .stream()
            .map(this::mapDocumentToVectorMetaData) // Document를 VectorMetaData로 매핑
            .collect(Collectors.toList());
    }

    @Override
    public void deleteDocument(String id) {
        vectorStore.delete(List.of(id));
    }

    private VectorMetaData mapDocumentToVectorMetaData(Document document) {
        // Document에서 데이터를 추출하여 VectorMetaData로 변환
        VectorMetaData metaData = new VectorMetaData();
        metaData.setId(document.getId());
        metaData.setEmail((String) document.getMetadata().get("email"));
        metaData.setUrl((String) document.getMetadata().get("url"));
        metaData.setLongSummary(document.getContent()); // longSummary는 Document의 Content
        metaData.setShortSummary((String) document.getMetadata().get("shortSummary")); // shortSummary 추출
        metaData.setKeywords((List<String>) document.getMetadata().get("keywords"));  // keywords 추출
        metaData.setVisitTime(LocalDateTime.ofEpochSecond(((Double) document.getMetadata().get("visitTime")).longValue(), 0, ZoneOffset.UTC));
        return metaData;
    }
}