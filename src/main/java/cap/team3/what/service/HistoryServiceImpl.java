package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cap.team3.what.dto.DetailedHistoryResponseDto;
import cap.team3.what.dto.HistoryRequestDto;
import cap.team3.what.dto.HistoryResponseDto;
import cap.team3.what.dto.ParsedChatResponse;
import cap.team3.what.dto.VectorMetaData;
import cap.team3.what.exception.HistoryNotFoundException;
import cap.team3.what.model.History;
import cap.team3.what.model.Keyword;
import cap.team3.what.model.User;
import cap.team3.what.repository.HistoryRepository;
import cap.team3.what.repository.KeywordRepository;
import cap.team3.what.util.ChatResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;
    private final KeywordRepository keywordRepository;
    private final UserService userService;
    private final ChatService chatService;
    private final PineconeService pineconeService;
    
    @Override
    @Transactional
    public DetailedHistoryResponseDto saveHistory(HistoryRequestDto historyRequestDto) {

        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userService.getUserByEmail(email);

        History history = historyRepository.findByUserAndUrl(user, historyRequestDto.getUrl()).orElse(null);
        VectorMetaData metaData;

        if (history != null) {
            history.setVisitCount(history.getVisitCount() + 1);
            history.setVisitTime(LocalDateTime.now());
    
            metaData = convertModelToMetaData(history);
            
            pineconeService.updateDocument(metaData);
    
            return convertModelToDetailedHistoryDto(historyRepository.save(history));
        }

        List<History> histories = historyRepository.findByUrl(historyRequestDto.getUrl()); 
            
        if (histories.isEmpty()) {
            ParsedChatResponse parsedChatResponse = ChatResponseParser.parseChatResponse(chatService.analyzeContent(historyRequestDto.getContent()));

            metaData = VectorMetaData.builder()
                                        .email(email)
                                        .url(historyRequestDto.getUrl())
                                        .title(parsedChatResponse.getTitle())
                                        .visitTime(LocalDateTime.now())
                                        .shortSummary(parsedChatResponse.getShortSummary())
                                        .longSummary(parsedChatResponse.getLongSummary())
                                        .keywords(parsedChatResponse.getKeywords())
                                        .spentTime(0)
                                        .visitCount(1)
                                        .build();

            pineconeService.saveDocument(metaData);
        } else {
            log.info("Reuse Ohter User's Data");
            history = histories.get(0);

            metaData = convertModelToMetaData(history);
            metaData.setEmail(email);
            metaData.setVisitTime(LocalDateTime.now());
            metaData.setSpentTime(0);
            metaData.setVisitCount(1);

            List<Float> embeddingVector = pineconeService.getVector(metaData.getId());
            if (embeddingVector.isEmpty() || embeddingVector == null) {
                pineconeService.saveDocument(metaData);
            } else {
                pineconeService.saveDocument(metaData, embeddingVector);

            }
        }
        
        History newHistory = convertMetaDataToModel(metaData);
        newHistory.setUser(user);
        newHistory.setContent(historyRequestDto.getContent());
        return convertModelToDetailedHistoryDto(historyRepository.save(newHistory));
        
    }

    @Override
    public DetailedHistoryResponseDto getDetailedHistoryById(Long id) {

        History history = historyRepository.findById(id)
                .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));
        
        return convertModelToDetailedHistoryDto(history);
    }

    @Override
    public HistoryResponseDto getHistoryByUrl(String url) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
        
        History history = historyRepository.findByUserAndUrl(user, url)
            .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));
        return convertModelToHistoryDto(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryResponseDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, String orderBy) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);

        List<History> histories;
        if ("visitCount".equals(orderBy)) {
            histories = historyRepository.findByVisitTimeBetweenOrderByVisitCount(user, startTime, endTime);
        } else if ("spentTime".equals(orderBy)) {
            histories = historyRepository.findByVisitTimeBetweenOrderBySpentTime(user, startTime, endTime);
        } else {
            histories = historyRepository.findByVisitTimeBetweenOrderByVisitTime(user, startTime, endTime);
        }
        
        // if (histories.isEmpty()) {
        //     throw new HistoryNotFoundException("No corresponding histories in the given time range");
        // }

        return histories.stream()
                    .map(this::convertModelToHistoryDto)
                    .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DetailedHistoryResponseDto updateHistory(String url, int spentTime) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
        
        History history = historyRepository.findByUserAndUrl(user, url)
            .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));

        history.setSpentTime(history.getSpentTime() + spentTime);

        VectorMetaData metaData = convertModelToMetaData(history);
        pineconeService.updateDocument(metaData);

        return convertModelToDetailedHistoryDto(historyRepository.save(history));
    }

    @Override
    @Transactional
    public void deleteHistory(String url) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
        
        History history = historyRepository.findByUserAndUrl(user, url)
            .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));

        pineconeService.deleteDocument(history.getVectorId());
        historyRepository.delete(history);

        history.getKeywords().forEach(keyword -> {
            if (keywordRepository.countByKeywordsContains(keyword) == 0L) { // 다른 History와 연결되지 않은 경우만 삭제
                keywordRepository.delete(keyword);
            }
        });
    }

    @Override
    public List<HistoryResponseDto> searchHistory(String query) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<VectorMetaData> metaDatas = pineconeService.searchDocuments(query, email, 10);

        log.info("first match: " + metaDatas.get(0).getUrl());

        return metaDatas.stream()
                        .map(VectorMetaData::getUrl) // URL 추출
                        .map(this::getHistoryByUrl) // URL로 History 검색
                        .toList();
    }

    @Override
    public List<HistoryResponseDto> searchHistory(LocalDateTime startTime, LocalDateTime endTime, String query) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<VectorMetaData> metaDatas = pineconeService.searchDocuments(query, email,10, startTime, endTime);

        return metaDatas.stream()
                        .map(VectorMetaData::getUrl) // URL 추출
                        .map(this::getHistoryByUrl) // URL로 History 검색
                        .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public int getKeywordFrequency(LocalDateTime startTime, LocalDateTime endTime, String keyword) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);

        List<History> histories = historyRepository.findByVisitTimeBetweenAndKeyword(user, startTime, endTime, keyword);

        if (histories.isEmpty()) {
            return 0;
        }

        return histories.size();       
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalSpentTime(LocalDateTime startTime, LocalDateTime endTime, String keyword) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);

        List<History> histories = historyRepository.findByVisitTimeBetweenAndKeyword(user, startTime, endTime, keyword);
        
        if (histories.isEmpty()) {
            throw new HistoryNotFoundException("No such history in DB");
            
        }

        int totalSpentTime = histories.stream()
                    .mapToInt(History::getSpentTime)
                    .sum();
            
        return totalSpentTime;
    }







    // ----------------------------------private methods------------------------------

    private History convertMetaDataToModel(VectorMetaData metaData) {

        List<Keyword> keywords;

        if (metaData.getKeywords() != null) {
            keywords = metaData.getKeywords().stream()
                .map(keywordText -> Keyword.builder().keyword(keywordText).build())
                .collect(Collectors.toList());
        } else {
            keywords = new ArrayList<>();
        }
    
        return History.builder()
                        .url(metaData.getUrl())
                        .vectorId(metaData.getId())
                        .title(metaData.getTitle())
                        .longSummary(metaData.getLongSummary())
                        .shortSummary(metaData.getShortSummary())
                        .spentTime(metaData.getSpentTime())
                        .visitCount(metaData.getVisitCount())
                        .visitTime(metaData.getVisitTime())
                        .keywords(keywords)
                        .build();
    }

    private VectorMetaData convertModelToMetaData(History history) {

        List<String> keywords;

        if (history.getKeywords() != null) {
            keywords = history.getKeywords().stream()
                .map(Keyword::getKeyword)
                .collect(Collectors.toList());
        } else {
            keywords = new ArrayList<>();
        }
    
        return VectorMetaData.builder()
                                .id(history.getVectorId())
                                .email(history.getUser().getEmail())
                                .url(history.getUrl())
                                .title(history.getTitle())
                                .visitTime(history.getVisitTime())
                                .shortSummary(history.getShortSummary())
                                .longSummary(history.getLongSummary())
                                .keywords(keywords)
                                .spentTime(history.getSpentTime())
                                .visitCount(history.getVisitCount())
                                .build();
    }

    private HistoryResponseDto convertModelToHistoryDto(History history) {
        return HistoryResponseDto.builder()
                                    .id(history.getId())
                                    .url(history.getUrl())
                                    .title(history.getTitle())
                                    .visitTime(history.getVisitTime())
                                    .shortSummary(history.getShortSummary())
                                    .build();
    }
    
    private DetailedHistoryResponseDto convertModelToDetailedHistoryDto(History history) {

        List<String> keywords;

        if (history.getKeywords() != null) {
            keywords = history.getKeywords().stream()
                .map(Keyword::getKeyword)
                .collect(Collectors.toList());
        } else {
            keywords = new ArrayList<>();
        }
    
        return DetailedHistoryResponseDto.builder()
                                            .id(history.getId())
                                            .url(history.getUrl())
                                            .title(history.getTitle())
                                            .visitTime(history.getVisitTime())
                                            .shortSummary(history.getShortSummary())
                                            .longSummary(history.getLongSummary())
                                            .keywords(keywords)
                                            .spentTime(history.getSpentTime())
                                            .visitCount(history.getVisitCount())
                                            .build();
    }
}