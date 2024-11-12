package cap.team3.what.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.protobuf.Struct;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;

@Service
public class PineconeServiceImpl implements PineconeService {

    @Value("${PINECONE_API_KEY}")
    private String pineconeApiKey;

    private final Index index;

    public PineconeServiceImpl() {
        Pinecone pineconeClient = new Pinecone.Builder(pineconeApiKey).build();
        this.index = pineconeClient.getIndexConnection("what"); // Replace with your index name
    }

    @Override
    public void upsertVector(String id, List<Float> values, Struct metadata, String namespace) {
        index.upsert(id, values, null, null, metadata, namespace);
    }

    @Override
    public QueryResponseWithUnsignedIndices query(List<Float> queryVector, Struct filter, int topK, String namespace) {
        return index.query(topK, queryVector, null, null, null, namespace, filter, false, true);
    }

}
