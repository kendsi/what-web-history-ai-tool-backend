package cap.team3.what.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.document.id.RandomIdGenerator;
import org.springframework.stereotype.Service;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import cap.team3.what.dto.VectorMetaData;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PineconeServiceImpl implements PineconeService {

    private final Pinecone pc;
    private final Index index;
    private final EmbeddingService embeddingService;

    public PineconeServiceImpl(@org.springframework.beans.factory.annotation.Value("${pinecone.apiKey}") String apiKey, EmbeddingService embeddingService) {
        this.pc = new Pinecone.Builder(apiKey).build();
        this.index = pc.getIndexConnection("page-summary");
        this.embeddingService = embeddingService;
    }

    @Override
    public String saveDocument(VectorMetaData metaData) {
        List<Float> embeddingVector = embeddingService.embeddingVector(metaData.getLongSummary());

        Map<String, Value> metaDataMap = Map.of(
            "email", Value.newBuilder().setStringValue(metaData.getEmail()).build(),
            "url", Value.newBuilder().setStringValue(metaData.getUrl()).build(),
            "shortSummary", Value.newBuilder().setStringValue(metaData.getShortSummary()).build(),
            "longSummary", Value.newBuilder().setStringValue(metaData.getLongSummary()).build(),
            "keywords", Value.newBuilder().setListValue(toListValue(metaData.getKeywords())).build(),
            "spentTime", Value.newBuilder().setNumberValue((double) metaData.getSpentTime()).build(),
            "visitCount", Value.newBuilder().setNumberValue((double) metaData.getVisitCount()).build(),
            "visitTime", Value.newBuilder().setNumberValue((double) metaData.getVisitTime().toEpochSecond(ZoneOffset.UTC)).build()
        );

        Struct metaDatas = Struct.newBuilder()
                                .putAllFields(metaDataMap)
                                .build();
                                
        String id = new RandomIdGenerator().generateId(metaData.getLongSummary());

        index.upsert(id, embeddingVector, null, null, metaDatas, null);

        return id;
    }

    @Override
    public void updateDocument(VectorMetaData metaData) {

        Map<String, Value> metaDataMap = Map.of(
            "email", Value.newBuilder().setStringValue(metaData.getEmail()).build(),
            "url", Value.newBuilder().setStringValue(metaData.getUrl()).build(),
            "shortSummary", Value.newBuilder().setStringValue(metaData.getShortSummary()).build(),
            "longSummary", Value.newBuilder().setStringValue(metaData.getLongSummary()).build(),
            "keywords", Value.newBuilder().setListValue(toListValue(metaData.getKeywords())).build(),
            "spentTime", Value.newBuilder().setNumberValue((double) metaData.getSpentTime()).build(),
            "visitCount", Value.newBuilder().setNumberValue((double) metaData.getVisitCount()).build(),
            "visitTime", Value.newBuilder().setNumberValue((double) metaData.getVisitTime().toEpochSecond(ZoneOffset.UTC)).build()
        );

        Struct metaDatas = Struct.newBuilder()
                                .putAllFields(metaDataMap)
                                .build();

        index.update(metaData.getId(), null, metaDatas, null, null, null);
    }

    @Override
    public void deleteDocument(String id) {
        index.deleteByIds(List.of(id));
    }

    @Override
    public List<VectorMetaData> searchDocuments(String query, String email, int topK) {
        List<Float> embeddingVector = embeddingService.embeddingVector(query);

        Struct filter = Struct.newBuilder()
        .putFields("email", Value.newBuilder()
            .setStructValue(
                Struct.newBuilder()
                    .putFields("$eq", Value.newBuilder()
                        .setStringValue(email)
                        .build()
                    ).build()
            ).build()
        ).build();

        QueryResponseWithUnsignedIndices queryResponse = index.query(
            topK, // 검색할 유사 벡터 개수
            embeddingVector, // 검색에 사용할 벡터
            null, // Score 임계값
            null, // 필드 선택
            null, // Sparse 필드
            null, // 네임스페이스
            filter, // 필터 조건
            true, // 메타데이터 포함 여부
            true // 벡터 값 포함 여부
        );

        return queryResponse.getMatchesList().stream()
            .map(this::convertToVectorMetaData)
            .collect(Collectors.toList());
    }

    @Override
    public List<VectorMetaData> searchDocuments(String query, String email, int topK, LocalDateTime startTime, LocalDateTime endTime) {
        List<Float> embeddingVector = embeddingService.embeddingVector(query);

        // visitTime 범위 조건 생성
        Struct visitTimeCondition = Struct.newBuilder()
            .putFields("$gte", Value.newBuilder()
                .setNumberValue((double) startTime.toEpochSecond(ZoneOffset.UTC))
                .build())
            .putFields("$lte", Value.newBuilder()
                .setNumberValue((double) endTime.toEpochSecond(ZoneOffset.UTC))
                .build())
            .build();

        // 전체 필터 생성
        Struct filter = Struct.newBuilder()
            .putFields("email", Value.newBuilder()
                .setStructValue(
                    Struct.newBuilder()
                        .putFields("$eq", Value.newBuilder()
                            .setStringValue(email)
                            .build()
                        ).build()
                ).build())
            .putFields("visitTime", Value.newBuilder()
                .setStructValue(visitTimeCondition)
                .build())
            .build();

        QueryResponseWithUnsignedIndices queryResponse = index.query(
            topK, // 검색할 유사 벡터 개수
            embeddingVector, // 검색에 사용할 벡터
            null, // Score 임계값
            null, // 필드 선택
            null, // Sparse 필드
            null, // 네임스페이스
            filter, // 필터 조건
            true, // 메타데이터 포함 여부
            true // 벡터 값 포함 여부
        );

        return queryResponse.getMatchesList().stream()
            .map(this::convertToVectorMetaData)
            .collect(Collectors.toList());
    }

    private VectorMetaData convertToVectorMetaData(ScoredVectorWithUnsignedIndices result) {
        Struct metadataStruct = result.getMetadata();
    
        VectorMetaData metaData = new VectorMetaData();
        metaData.setId(result.getId());
        metaData.setEmail(metadataStruct.getFieldsOrDefault("email", Value.newBuilder().setStringValue("").build()).getStringValue());
        metaData.setUrl(metadataStruct.getFieldsOrDefault("url", Value.newBuilder().setStringValue("").build()).getStringValue());
        metaData.setLongSummary(metadataStruct.getFieldsOrDefault("longSummary", Value.newBuilder().setStringValue("").build()).getStringValue());
        metaData.setShortSummary(metadataStruct.getFieldsOrDefault("shortSummary", Value.newBuilder().setStringValue("").build()).getStringValue());
        metaData.setKeywords(metadataStruct.getFieldsOrDefault("keywords", Value.newBuilder().setListValue(ListValue.newBuilder().build()).build())
            .getListValue()
            .getValuesList()
            .stream()
            .map(Value::getStringValue)
            .toList());
        metaData.setSpentTime((int) metadataStruct.getFieldsOrDefault("spentTime", Value.newBuilder().setNumberValue(0).build()).getNumberValue());
        metaData.setVisitCount((int) metadataStruct.getFieldsOrDefault("visitCount", Value.newBuilder().setNumberValue(0).build()).getNumberValue());
        metaData.setVisitTime(LocalDateTime.ofEpochSecond(
            (long) metadataStruct.getFieldsOrDefault("visitTime", Value.newBuilder().setNumberValue(0).build()).getNumberValue(),
            0,
            ZoneOffset.UTC
        ));
    
        return metaData;
    }

    private ListValue toListValue(List<String> keywords) {
        ListValue.Builder listValueBuilder = ListValue.newBuilder();
        for (String keyword : keywords) {
            listValueBuilder.addValues(Value.newBuilder().setStringValue(keyword).build());
        }
        return listValueBuilder.build();
    }
}
