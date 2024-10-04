package cap.team3.what.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.springframework.ai.chat.messages.*;

import java.io.IOException;

public class MessageDeserializer extends StdDeserializer<Message> {

    public MessageDeserializer() {
        super(Message.class);
    }

    @Override
    public Message deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        String role = node.get("role").asText();

        switch (role) {
            case "user":
                return new UserMessage(node.get("content").asText());
            case "system":
                return new SystemMessage(node.get("content").asText());
            case "assistant":
                return new AssistantMessage(node.get("content").asText());
            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
}