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

import cap.team3.what.dto.SearchRequestDto;
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
    public void saveDocument(VectorMetaData metaData) {
        List<Float> embeddingVector = embeddingService.embeddingVector(metaData.getKeywords().toString() + metaData.getCategory());

        Map<String, Value> metaDataMap = Map.ofEntries(
            Map.entry("email", Value.newBuilder().setStringValue(metaData.getEmail()).build()),
            Map.entry("url", Value.newBuilder().setStringValue(metaData.getUrl()).build()),
            Map.entry("title", Value.newBuilder().setStringValue(metaData.getTitle()).build()),
            Map.entry("shortSummary", Value.newBuilder().setStringValue(metaData.getShortSummary()).build()),
            Map.entry("longSummary", Value.newBuilder().setStringValue(metaData.getLongSummary()).build()),
            Map.entry("domain", Value.newBuilder().setStringValue(metaData.getDomain()).build()),
            Map.entry("category", Value.newBuilder().setStringValue(metaData.getCategory()).build()),
            Map.entry("keywords", Value.newBuilder().setListValue(toListValue(metaData.getKeywords())).build()),
            Map.entry("spentTime", Value.newBuilder().setNumberValue((double) metaData.getSpentTime()).build()),
            Map.entry("visitCount", Value.newBuilder().setNumberValue((double) metaData.getVisitCount()).build()),
            Map.entry("visitTime", Value.newBuilder().setNumberValue((double) metaData.getVisitTime().toEpochSecond(ZoneOffset.UTC)).build())
        );

        Struct metaDatas = Struct.newBuilder()
                                .putAllFields(metaDataMap)
                                .build();
                                
        String id = new RandomIdGenerator().generateId(metaData.getEmail() + metaData.getUrl());

        index.upsert(id, embeddingVector, null, null, metaDatas, null);

        metaData.setId(id);
    }

    @Override
    public void saveDocument(VectorMetaData metaData, List<Float> embeddingVector) {
        Map<String, Value> metaDataMap = Map.ofEntries(
            Map.entry("email", Value.newBuilder().setStringValue(metaData.getEmail()).build()),
            Map.entry("url", Value.newBuilder().setStringValue(metaData.getUrl()).build()),
            Map.entry("title", Value.newBuilder().setStringValue(metaData.getTitle()).build()),
            Map.entry("shortSummary", Value.newBuilder().setStringValue(metaData.getShortSummary()).build()),
            Map.entry("longSummary", Value.newBuilder().setStringValue(metaData.getLongSummary()).build()),
            Map.entry("domain", Value.newBuilder().setStringValue(metaData.getDomain()).build()),
            Map.entry("category", Value.newBuilder().setStringValue(metaData.getCategory()).build()),
            Map.entry("keywords", Value.newBuilder().setListValue(toListValue(metaData.getKeywords())).build()),
            Map.entry("spentTime", Value.newBuilder().setNumberValue((double) metaData.getSpentTime()).build()),
            Map.entry("visitCount", Value.newBuilder().setNumberValue((double) metaData.getVisitCount()).build()),
            Map.entry("visitTime", Value.newBuilder().setNumberValue((double) metaData.getVisitTime().toEpochSecond(ZoneOffset.UTC)).build())
        );

        Struct metaDatas = Struct.newBuilder()
                                .putAllFields(metaDataMap)
                                .build();
                                
        String id = new RandomIdGenerator().generateId(metaData.getEmail() + metaData.getUrl());

        index.upsert(id, embeddingVector, null, null, metaDatas, null);

        metaData.setId(id);
    }

    @Override
    public List<Float> getVector(String id) {
        return index.fetch(List.of(id)).getVectorsOrDefault(id, null).getValuesList();
    }

    @Override
    public void updateDocument(VectorMetaData metaData) {

        Map<String, Value> metaDataMap = Map.ofEntries(
            Map.entry("email", Value.newBuilder().setStringValue(metaData.getEmail()).build()),
            Map.entry("url", Value.newBuilder().setStringValue(metaData.getUrl()).build()),
            Map.entry("title", Value.newBuilder().setStringValue(metaData.getTitle()).build()),
            Map.entry("shortSummary", Value.newBuilder().setStringValue(metaData.getShortSummary()).build()),
            Map.entry("longSummary", Value.newBuilder().setStringValue(metaData.getLongSummary()).build()),
            Map.entry("domain", Value.newBuilder().setStringValue(metaData.getDomain()).build()),
            Map.entry("category", Value.newBuilder().setStringValue(metaData.getCategory()).build()),
            Map.entry("keywords", Value.newBuilder().setListValue(toListValue(metaData.getKeywords())).build()),
            Map.entry("spentTime", Value.newBuilder().setNumberValue((double) metaData.getSpentTime()).build()),
            Map.entry("visitCount", Value.newBuilder().setNumberValue((double) metaData.getVisitCount()).build()),
            Map.entry("visitTime", Value.newBuilder().setNumberValue((double) metaData.getVisitTime().toEpochSecond(ZoneOffset.UTC)).build())
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
        public List<VectorMetaData> searchDocuments(SearchRequestDto searchRequestDto, String email, int topK) {

        List<Float> embeddingVector = embeddingService.embeddingVector(searchRequestDto.getQuery());
    
        // 필터 빌더 생성
        Struct.Builder filterBuilder = Struct.newBuilder();
    
        // email 조건 추가
        filterBuilder.putFields("email", Value.newBuilder()
            .setStructValue(
                Struct.newBuilder()
                    .putFields("$eq", Value.newBuilder()
                        .setStringValue(email)
                        .build()
                    ).build()
            ).build());
    
        // visitTime 조건 추가
        if (searchRequestDto.getStartTime() != null && searchRequestDto.getEndTime() != null) {
            filterBuilder.putFields("visitTime", Value.newBuilder()
                .setStructValue(
                    Struct.newBuilder()
                        .putFields("$gte", Value.newBuilder()
                            .setNumberValue((double) searchRequestDto.getStartTime().toEpochSecond(ZoneOffset.UTC))
                            .build())
                        .putFields("$lte", Value.newBuilder()
                            .setNumberValue((double) searchRequestDto.getEndTime().toEpochSecond(ZoneOffset.UTC))
                            .build())
                        .build()
                ).build());
        }
    
        // domain 조건 추가
        if (searchRequestDto.getDomain() != null && !searchRequestDto.getDomain().isEmpty()) {
            filterBuilder.putFields("domain", Value.newBuilder()
                .setStructValue(
                    Struct.newBuilder()
                        .putFields("$eq", Value.newBuilder()
                            .setStringValue(searchRequestDto.getDomain())
                            .build())
                        .build()
                ).build());
        }
    
        // category 조건 추가
        if (searchRequestDto.getCategory() != null && !searchRequestDto.getCategory().isEmpty()) {
            filterBuilder.putFields("category", Value.newBuilder()
                .setStructValue(
                    Struct.newBuilder()
                        .putFields("$eq", Value.newBuilder()
                            .setStringValue(searchRequestDto.getCategory())
                            .build())
                        .build()
                ).build());
        }
    
        // 전체 필터 생성
        Struct filter = filterBuilder.build();

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
    
        VectorMetaData metaData = VectorMetaData.builder()
        .id(result.getId())
        .email(metadataStruct.getFieldsOrDefault("email", Value.newBuilder().setStringValue("").build()).getStringValue())
        .url(metadataStruct.getFieldsOrDefault("url", Value.newBuilder().setStringValue("").build()).getStringValue())
        .title(metadataStruct.getFieldsOrDefault("title", Value.newBuilder().setStringValue("").build()).getStringValue())
        .longSummary(metadataStruct.getFieldsOrDefault("longSummary", Value.newBuilder().setStringValue("").build()).getStringValue())
        .shortSummary(metadataStruct.getFieldsOrDefault("shortSummary", Value.newBuilder().setStringValue("").build()).getStringValue())
        .domain(metadataStruct.getFieldsOrDefault("domain", Value.newBuilder().setStringValue("").build()).getStringValue())
        .category(metadataStruct.getFieldsOrDefault("category", Value.newBuilder().setStringValue("").build()).getStringValue())
        .keywords(metadataStruct.getFieldsOrDefault("keywords", Value.newBuilder().setListValue(ListValue.newBuilder().build()).build()).getListValue().getValuesList().stream().map(Value::getStringValue).toList())
        .spentTime((int) metadataStruct.getFieldsOrDefault("spentTime", Value.newBuilder().setNumberValue(0).build()).getNumberValue())
        .visitCount((int) metadataStruct.getFieldsOrDefault("visitCount", Value.newBuilder().setNumberValue(0).build()).getNumberValue())
        .visitTime(LocalDateTime.ofEpochSecond((long) metadataStruct.getFieldsOrDefault("visitTime", Value.newBuilder().setNumberValue(0).build()).getNumberValue(), 0, ZoneOffset.UTC))
        .build();
    
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
