package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cap.team3.what.dto.HistoryDto;
import cap.team3.what.exception.HistoryNotFoundException;
import cap.team3.what.model.History;
import cap.team3.what.model.Keyword;
import cap.team3.what.model.User;
import cap.team3.what.repository.HistoryRepository;
import cap.team3.what.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;
    private final KeywordRepository keywordRepository;
    private final UserService userService;
    private final ChatService aiService;
    
    @Override
    @Transactional
    public HistoryDto saveHistory(HistoryDto historyDto) {

        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
        log.info(user.getId().toString());

        History history = historyRepository.findByUserAndUrl(user, historyDto.getUrl()).orElse(null);

        if (history == null) {
            History newHistory = convertToModel(historyDto);
            newHistory.setVisitCount(1);
            newHistory.setUser(user);
            return convertToDto(historyRepository.save(newHistory));
        }
        
        history.setVisitCount(history.getVisitCount() + 1);
        history.setVisitTime(historyDto.getVisitTime());

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
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
        
        History history = historyRepository.findByUserAndUrl(user, url)
            .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));
        return convertToDto(history);
    }

    @Override
    @Transactional
    public HistoryDto updateHistory(String url, int spentTime) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
        
        History history = historyRepository.findByUserAndUrl(user, url)
            .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));

        history.setSpentTime(history.getSpentTime() + spentTime);
        return convertToDto(historyRepository.save(history));
    }

    @Override
    @Transactional
    public List<String> extractKeywords(String url) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
        
        History history = historyRepository.findByUserAndUrl(user, url)
            .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));

        if (!history.getKeywords().isEmpty()) {
            throw new RuntimeException("Keyword already extracted for url: " + history.getUrl());
        }

        List<String> extractedKeywords = aiService.extractKeywords(history.getContent());
        List<Keyword> keywords = extractedKeywords.stream()
            .map(keywordText -> keywordRepository.findByKeyword(keywordText)
                .orElseGet(() -> keywordRepository.save(new Keyword(keywordText))))
            .collect(Collectors.toList());

        history.setKeywords(keywords);
        historyRepository.save(history);

        return extractedKeywords;
    }

    @Override
    @Transactional
    public void deleteHistory(String url) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
        
        History history = historyRepository.findByUserAndUrl(user, url)
            .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));
        
        historyRepository.delete(history);

        history.getKeywords().forEach(keyword -> {
            if (keywordRepository.countByKeywordsContains(keyword) == 0L) { // 다른 History와 연결되지 않은 경우만 삭제
                keywordRepository.delete(keyword);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, String orderBy) {
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
        
        if (histories.isEmpty()) {
            throw new HistoryNotFoundException("No corresponding histories in the given time range");
        }

        return histories.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, String orderBy, List<String> keywords) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);

        List<History> histories;
        if ("visitCount".equals(orderBy)) {
            histories = historyRepository.findByVisitTimeBetweenAndKeywordsOrderByVisitCount(user, startTime, endTime, keywords, Long.valueOf(keywords.size()));
        } else if ("spentTime".equals(orderBy)) {
            histories = historyRepository.findByVisitTimeBetweenAndKeywordsOrderBySpentTime(user, startTime, endTime, keywords, Long.valueOf(keywords.size()));
        } else {
            histories = historyRepository.findByVisitTimeBetweenAndKeywordsOrderByVisitTime(user, startTime, endTime, keywords, Long.valueOf(keywords.size()));
        }
        
        if (histories.isEmpty()) {
            throw new HistoryNotFoundException("No corresponding histories in the given time range");
        }

        return histories.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
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

    private History convertToModel(HistoryDto historyDto) {

        List<Keyword> keywords;

        if (historyDto.getKeywords() != null) {
            keywords = historyDto.getKeywords().stream()
                .map(keywordText -> Keyword.builder().keyword(keywordText).build())
                .collect(Collectors.toList());
        } else {
            keywords = new ArrayList<>();
        }
    
        return History.builder()
                      .title(historyDto.getTitle())
                      .content(historyDto.getContent())
                      .url(historyDto.getUrl())
                      .spentTime(historyDto.getSpentTime())
                      .visitCount(historyDto.getVisitCount())
                      .visitTime(historyDto.getVisitTime())
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
    
        return HistoryDto.builder()
                         .id(history.getId())
                         .email(history.getUser().getEmail())
                         .title(history.getTitle())
                         .content(history.getContent())
                         .url(history.getUrl())
                         .spentTime(history.getSpentTime())
                         .visitCount(history.getVisitCount())
                         .visitTime(history.getVisitTime())
                         .keywords(keywords)
                         .build();
    }
}