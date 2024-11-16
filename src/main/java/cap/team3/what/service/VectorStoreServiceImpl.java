package cap.team3.what.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.PineconeVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import cap.team3.what.dto.VectorMetaData;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VectorStoreServiceImpl implements VectorStoreService {

    private final PineconeVectorStore vectorStore;

    // 문서를 Pinecone에 추가
    @Override
    public void addDocument(VectorMetaData metaData) {
        List<Document> documents = List.of(
            new Document(
                metaData.getLongSummary(),
                Map.of("shortSummary", metaData.getShortSummary(), "keywords", metaData.getKeywords()))
        );

        vectorStore.add(documents);
    }

    // 유사한 문서 검색
    @Override
    public List<VectorMetaData> searchSimilarDocuments(String query, int topK) {
        return vectorStore.similaritySearch(SearchRequest.query(query).withTopK(topK))
            .stream()
            .map(this::mapDocumentToVectorMetaData) // Document를 VectorMetaData로 매핑
            .collect(Collectors.toList());
    }

    private VectorMetaData mapDocumentToVectorMetaData(Document document) {
        // Document에서 데이터를 추출하여 VectorMetaData로 변환
        VectorMetaData metaData = new VectorMetaData();
        metaData.setLongSummary(document.getContent()); // longSummary는 Document의 Content
        metaData.setShortSummary((String) document.getMetadata().get("shortSummary")); // shortSummary 추출
        metaData.setKeywords((List<String>) document.getMetadata().get("keywords"));  // keywords 추출
        return metaData;
    }
}