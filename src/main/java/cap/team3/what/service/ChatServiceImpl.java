package cap.team3.what.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import org.springframework.stereotype.Service;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.chat.messages.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cap.team3.what.config.ObjectMapperConfig;
import cap.team3.what.exception.GptResponseException;
import cap.team3.what.exception.ReadPromptException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    @Value("classpath:prompts/prompt.json")
    private Resource promptResource;

    private final OpenAiApi openAiApi;
    private final OpenAiChatModel openAiChatModel;
    
    @Override
    public String analyzeContent(String content) {
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
            
            // gpt-4o로 첫 번째 요청 시도
            try {
                return sendRequestWithModel(prompt, openAiChatModel);
            } catch (GptResponseException e) {
                log.warn("GPT-4o failed, retrying with gpt-4o-mini: {}", e.getMessage());

                // gpt-4o-mini로 재요청
                OpenAiChatOptions openAiChatOptions = (OpenAiChatOptions) this.openAiChatModel.getDefaultOptions();
                OpenAiChatOptions miniOptions = OpenAiChatOptions.builder()
                                                                .withModel("gpt-4o-mini")
                                                                .withTemperature(openAiChatOptions.getTemperature())
                                                                .withFrequencyPenalty(openAiChatOptions.getFrequencyPenalty())
                                                                .withN(openAiChatOptions.getN())
                                                                .build();
                OpenAiChatModel miniModel = new OpenAiChatModel(openAiApi, miniOptions);
                return sendRequestWithModel(prompt, miniModel);
            }

        } catch (IOException e) {
            log.error("Failed to read the prompt file", e);
            throw new ReadPromptException("Failed to read the prompt file");
        }
    }

    // 요청을 처리하는 메서드
    private String sendRequestWithModel(Prompt prompt, OpenAiChatModel model) {
        ChatResponse response = model.call(prompt);

        if (!response.getResults().isEmpty()) {
            String result = response.getResults().get(0).getOutput().getContent();
            log.info("GPT Response: {}", result);
            return result;
        } else {
            throw new GptResponseException("No response from GPT");
        }
    }
}
