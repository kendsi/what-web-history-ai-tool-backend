package cap.team3.what.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import org.springframework.stereotype.Service;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.chat.messages.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cap.team3.what.config.ObjectMapperConfig;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    @Value("classpath:prompts/prompt.json")
    private Resource promptResource;

    private final OpenAiChatModel openAiChatModel;
    
    @Override
    public List<String> extractKeywords(String content) {
        try {
            // JSON 파일을 읽어 List<Message>로 매핑
            ObjectMapper objectMapper = ObjectMapperConfig.createObjectMapper();
            List<Message> messages = objectMapper.readValue(
                    promptResource.getInputStream(), new TypeReference<List<Message>>() {});

            // 마지막 메시지가 항상 user 메시지인 경우 해당 메시지의 내용 변경
            if (!messages.isEmpty() && messages.get(messages.size() - 1) instanceof UserMessage) {
                UserMessage newUserMessage = new UserMessage(content);
                messages.set(messages.size() - 1, newUserMessage);  // 기존 메시지를 새 메시지로 대체
            }

            // Prompt 객체 생성
            Prompt prompt = new Prompt(messages);

            // 프롬프트 로그 출력
            log.info("Generated Prompt: {}", prompt.toString());

            ChatResponse response = this.openAiChatModel.call(prompt);

            if (!response.getResults().isEmpty()) {
                String result = response.getResults().get(0).getOutput().getContent();
                log.info(result);
                return regexKeywords(result);
            } else {
                throw new RuntimeException("No response from GPT");
            }

        } catch (IOException e) {
            log.error("Failed to read the prompt file", e);
            throw new RuntimeException("Failed to read the prompt file", e);
        }
    }


    private List<String> regexKeywords(String input) {
        String trimmedInput = input.replaceAll("[\\[\\]]", "").trim();
        List<String> keywords = Arrays.asList(trimmedInput.split("\\s*,\\s*"));

        return keywords;
    }
}
