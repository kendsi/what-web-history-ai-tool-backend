package cap.team3.what.service;

import java.util.List;

import com.google.protobuf.Struct;

import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;

public interface PineconeService {
    public void upsertVector(String id, List<Float> values, Struct metadata, String namespace);
    public QueryResponseWithUnsignedIndices query(List<Float> queryVector, Struct filter, int topK, String namespace);
}
