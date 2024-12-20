package cap.team3.what.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cap.team3.what.dto.DetailedHistoryResponseDto;
import cap.team3.what.dto.HistoryRequestDto;
import cap.team3.what.dto.HistoryResponseDto;
import cap.team3.what.dto.ParsedChatResponse;
import cap.team3.what.dto.SearchRequestDto;
import cap.team3.what.dto.VectorMetaData;
import cap.team3.what.exception.HistoryNotFoundException;
import cap.team3.what.model.Category;
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
    private final CategoryService categoryService;
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

        History newHistory = History.builder()
                .url(historyRequestDto.getUrl())
                .content(historyRequestDto.getContent())
                .user(user)
                .build();
        historyRepository.save(newHistory);

        ParsedChatResponse parsedChatResponse = ChatResponseParser.parseChatResponse(chatService.analyzeContent("categories: " + categoryService.getAllCategories() + "\n방문 페이지 내용: [" + historyRequestDto.getContent() + "]"));

        metaData = VectorMetaData.builder()
                                    .email(email)
                                    .url(historyRequestDto.getUrl())
                                    .title(parsedChatResponse.getTitle())
                                    .visitTime(LocalDateTime.now())
                                    .shortSummary(parsedChatResponse.getShortSummary())
                                    .longSummary(parsedChatResponse.getLongSummary())
                                    .domain(extractDomain(historyRequestDto.getUrl()))
                                    .category(parsedChatResponse.getCategory())
                                    .keywords(parsedChatResponse.getKeywords())
                                    .spentTime(0)
                                    .visitCount(1)
                                    .build();

        pineconeService.saveDocument(metaData);
    
        convertMetaDataToModel(metaData, newHistory);
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
    public Page<HistoryResponseDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);

        Page<History> histories = historyRepository.findByVisitTimeBetween(user, startTime, endTime, pageable);

        return histories.map(this::convertModelToHistoryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoryResponseDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, String domain, String category, Pageable pageable) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);

        Page<History> histories = historyRepository.findByVisitTimeBetweenAndFilters(user.getId(), startTime, endTime, domain, category, pageable);

        return histories.map(this::convertModelToHistoryDto);
    }

    @Override
    @Transactional
    public DetailedHistoryResponseDto updateHistory(String url, int spentTime, String category) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
        
        History history = historyRepository.findByUserAndUrl(user, url)
            .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));

        history.setSpentTime(history.getSpentTime() + spentTime);

        if (!category.isEmpty() || !category.equals("")) {
            history.setCategory(categoryService.findByName(category));
        } else {
            history.setCategory(null);
        }

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
    public List<HistoryResponseDto> searchHistory(SearchRequestDto searchRequestDto) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<VectorMetaData> metaDatas = pineconeService.searchDocuments(searchRequestDto, email,10);

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

    @Override
    @Transactional(readOnly = true)
    public List<String> getDomainFrequency(LocalDateTime startTime, LocalDateTime endTime, int k) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);

        List<String> domains = historyRepository.findTopKDistinctDomains(user.getId(), startTime, endTime, k);

        return domains;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCategoryFrequency(LocalDateTime startTime, LocalDateTime endTime, int k) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);

        List<String> domains = historyRepository.findTopKCategories(user.getId(), startTime, endTime, k);

        return domains;
    }



    // ----------------------------------private methods------------------------------

    private void convertMetaDataToModel(VectorMetaData metaData, History history) {

        List<Keyword> keywords;

        if (metaData.getKeywords() != null) {
            keywords = metaData.getKeywords().stream()
                .map(keywordText -> Keyword.builder().keyword(keywordText).build())
                .collect(Collectors.toList());
        } else {
            keywords = new ArrayList<>();
        }

        Category category = categoryService.findByName(metaData.getCategory());

        history.setVectorId(metaData.getId());
        history.setTitle(metaData.getTitle());
        history.setLongSummary(metaData.getLongSummary());
        history.setShortSummary(metaData.getShortSummary());
        history.setCategory(category);
        history.setKeywords(keywords);
        history.setSpentTime(metaData.getSpentTime());
        history.setVisitCount(metaData.getVisitCount());
        history.setVisitTime(metaData.getVisitTime());
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
                                .domain(extractDomain(history.getUrl()))
                                .category(history.getCategory().getName())
                                .keywords(keywords)
                                .spentTime(history.getSpentTime())
                                .visitCount(history.getVisitCount())
                                .build();
    }

    private HistoryResponseDto convertModelToHistoryDto(History history) {

        List<String> keywords;

        if (history.getKeywords() != null) {
            keywords = history.getKeywords().stream()
                .map(Keyword::getKeyword)
                .collect(Collectors.toList());
        } else {
            keywords = new ArrayList<>();
        }

        return HistoryResponseDto.builder()
                                    .id(history.getId())
                                    .url(history.getUrl())
                                    .title(history.getTitle())
                                    .keywords(keywords)
                                    .visitTime(history.getVisitTime())
                                    .shortSummary(history.getShortSummary())
                                    .category(history.getCategory().getName())
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
                                            .category(history.getCategory().getName())
                                            .keywords(keywords)
                                            .spentTime(history.getSpentTime())
                                            .visitCount(history.getVisitCount())
                                            .build();
    }

    private String extractDomain(String urlString) {
        try {
            String host = URI.create(urlString).getHost();
            return host;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL: " + urlString, e);
        }
    }

}