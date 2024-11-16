package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cap.team3.what.dto.HistoryDto;
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
    public HistoryDto saveHistory(HistoryDto historyDto) {

        // String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
String email = "test@example.com";
        VectorMetaData metaData = historyDto.getMetaData();
        metaData.setEmail(email);

        User user = userService.getUserByEmail(email);

        History history = historyRepository.findByUserAndUrl(user, metaData.getUrl()).orElse(null);

        if (history == null) {
            VectorMetaData analyzedContent = ChatResponseParser.parseChatResponse(chatService.analyzeContent(historyDto.getContent()));
            List<Keyword> keywords = analyzedContent.getKeywords().stream()
                .map(keywordText -> keywordRepository.findByKeyword(keywordText)
                    .orElseGet(() -> keywordRepository.save(new Keyword(keywordText))))
                .collect(Collectors.toList());

            metaData.setLongSummary(analyzedContent.getLongSummary());
            metaData.setShortSummary(analyzedContent.getShortSummary());
            metaData.setKeywords(analyzedContent.getKeywords());
            metaData.setVisitCount(1);
            metaData.setSpentTime(0);
            metaData.setVisitTime(LocalDateTime.now());
            metaData.setId(pineconeService.saveDocument(metaData));

            historyDto.setMetaData(metaData);
                
            History newHistory = convertToModel(historyDto);
            newHistory.setVisitCount(1);
            newHistory.setUser(user);
            newHistory.setKeywords(keywords);
            
            return convertToDto(historyRepository.save(newHistory));
        }

        history.setVisitCount(history.getVisitCount() + 1);
        history.setVisitTime(LocalDateTime.now());
        
        metaData.setSpentTime(history.getSpentTime());
        metaData.setVisitCount(history.getVisitCount() + 1);
        metaData.setVisitTime(LocalDateTime.now());
        metaData.setLongSummary(history.getLongSummary());
        metaData.setShortSummary(history.getShortSummary());
        metaData.setKeywords(history.getKeywords().stream()
                                                .map(Keyword::getKeyword)
                                                .collect(Collectors.toList()));
        metaData.setId(history.getVectorId());

        pineconeService.updateDocument(metaData);

        return convertToDto(historyRepository.save(history));
    }

    @Override
    public HistoryDto getHistoryById(Long id) {

        History history = historyRepository.findById(id)
                .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));
        
        return convertToDto(history);
    }

    @Override
    public HistoryDto getHistoryByUrl(String url) {
        // String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
String email = "test@example.com";
        User user = userService.getUserByEmail(email);
        
        History history = historyRepository.findByUserAndUrl(user, url)
            .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));
        return convertToDto(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, String orderBy) {
        // String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
String email = "test@example.com";
        User user = userService.getUserByEmail(email);

        List<History> histories;
        if ("visitCount".equals(orderBy)) {
            histories = historyRepository.findByVisitTimeBetweenOrderByVisitCount(user, startTime, endTime);
        } else if ("spentTime".equals(orderBy)) {
            histories = historyRepository.findByVisitTimeBetweenOrderBySpentTime(user, startTime, endTime);
        } else {
            histories = historyRepository.findByVisitTimeBetweenOrderByVisitTime(user, startTime, endTime);
        }
        
        if (histories.isEmpty()) {
            throw new HistoryNotFoundException("No corresponding histories in the given time range");
        }

        return histories.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HistoryDto updateHistory(String url, int spentTime) {
        // String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
String email = "test@example.com";
        User user = userService.getUserByEmail(email);
        
        History history = historyRepository.findByUserAndUrl(user, url)
            .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));

        history.setSpentTime(history.getSpentTime() + spentTime);
        return convertToDto(historyRepository.save(history));
    }

    @Override
    @Transactional
    public void deleteHistory(String url) {
        // String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
String email = "test@example.com";
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
    public List<HistoryDto> searchHistory(String query) {
        // String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
String email = "test@example.com";

        List<VectorMetaData> metaDatas = pineconeService.searchDocuments(query, email, 10);

        log.info("first match: " + metaDatas.get(0).getUrl());

        return metaDatas.stream()
                        .map(VectorMetaData::getUrl) // URL 추출
                        .map(this::getHistoryByUrl) // URL로 History 검색
                        .toList();
    }

//     @Override
//     public List<HistoryDto> searchHistory(LocalDateTime startTime, LocalDateTime endTime, String query) {
//         // String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
// String email = "test@example.com";

//         List<VectorMetaData> metaDatas = pineconeService.searchDocuments(query, 10, email, startTime, endTime);

//         return metaDatas.stream()
//                         .map(VectorMetaData::getUrl) // URL 추출
//                         .map(this::getHistoryByUrl) // URL로 History 검색
//                         .toList();
//     }


    @Override
    @Transactional(readOnly = true)
    public int getKeywordFrequency(LocalDateTime startTime, LocalDateTime endTime, String keyword) {
        // String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
String email = "test@example.com";
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
        // String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
String email = "test@example.com";
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

    private History convertToModel(HistoryDto historyDto) {

        List<Keyword> keywords;
        VectorMetaData metaData = historyDto.getMetaData();

        if (metaData.getKeywords() != null) {
            keywords = metaData.getKeywords().stream()
                .map(keywordText -> Keyword.builder().keyword(keywordText).build())
                .collect(Collectors.toList());
        } else {
            keywords = new ArrayList<>();
        }
    
        return History.builder()
                      .content(historyDto.getContent())
                      .vectorId(metaData.getId())
                      .longSummary(metaData.getLongSummary())
                      .shortSummary(metaData.getShortSummary())
                      .url(metaData.getUrl())
                      .spentTime(metaData.getSpentTime())
                      .visitCount(metaData.getVisitCount())
                      .visitTime(metaData.getVisitTime())
                      .keywords(keywords)
                      .build();
    }
    
    private HistoryDto convertToDto(History history) {

        List<String> keywords;

        if (history.getKeywords() != null) {
            keywords = history.getKeywords().stream()
                .map(Keyword::getKeyword)
                .collect(Collectors.toList());
        } else {
            keywords = new ArrayList<>();
        }

        VectorMetaData metaData = new VectorMetaData();

        metaData.setId(history.getVectorId());
        metaData.setEmail(history.getUser().getEmail());
        metaData.setLongSummary(history.getLongSummary());
        metaData.setShortSummary(history.getShortSummary());
        metaData.setKeywords(keywords);
        metaData.setUrl(history.getUrl());
        metaData.setVisitTime(history.getVisitTime());
    
        return HistoryDto.builder()
                         .id(history.getId())
                         .content("")
                         .metaData(metaData)
                         .spentTime(history.getSpentTime())
                         .visitCount(history.getVisitCount())
                         .build();
    }
}