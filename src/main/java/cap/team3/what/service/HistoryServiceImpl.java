package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cap.team3.what.dto.HistoryDto;
import cap.team3.what.exception.HistoryNotFoundException;
import cap.team3.what.model.History;
import cap.team3.what.model.Keyword;
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
    private final AIService aiService;
    
    @Override
    @Transactional
    public HistoryDto saveHistory(HistoryDto historyDto) {
        History history = historyRepository.findByUrl(historyDto.getUrl()).orElse(null);

        if (history == null) {
            History newHistory = convertToModel(historyDto);
            newHistory.setVisitCount(1);
            return convertToDto(historyRepository.save(newHistory));
        }
        
        int oldVisitCount = history.getVisitCount();
        history.setVisitCount(oldVisitCount + 1);

        return convertToDto(historyRepository.save(history));
    }

    @Override
    public HistoryDto getHistory(String url) {
        History history = historyRepository.findByUrl(url)
                .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));
        return convertToDto(history);
    }

    @Override
    @Transactional
    public HistoryDto updateHistory(String url, int spentTime) {
        History history = historyRepository.findByUrl(url)
                .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));

        int oldSpentTime = history.getSpentTime();
        history.setSpentTime(oldSpentTime + spentTime);
        return convertToDto(historyRepository.save(history));
    }

    @Override
    @Transactional
    public List<String> extractKeywords(String url) {
        History history = historyRepository.findByUrl(url)
                .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));

        if (!history.getKeywords().isEmpty() && history.getKeywords() != null) {
            
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
        History history = historyRepository.findByUrl(url)
                .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));
        
        historyRepository.delete(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, String orderBy) {
        
        List<History> histories;
        switch (orderBy) {
            case "visitCount":
                histories = historyRepository.findByVisitTimeBetweenOrderByVisitCount(startTime, endTime);
                break;
            case "spentTime":
                histories = historyRepository.findByVisitTimeBetweenOrderBySpentTime(startTime, endTime);
                break;
            case "visitTime":
            default:
                histories = historyRepository.findByVisitTimeBetweenOrderByVisitTime(startTime, endTime);
                break;
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

        List<History> histories;
        switch (orderBy) {
            case "visitCount":
                histories = historyRepository.findByVisitTimeBetweenAndKeywordsOrderByVisitCount(startTime, endTime, keywords, Long.valueOf(keywords.size()));
                break;
            case "spentTime":
                histories = historyRepository.findByVisitTimeBetweenAndKeywordsOrderBySpentTime(startTime, endTime, keywords, Long.valueOf(keywords.size()));
                break;
            case "visitTime":
            default:
                histories = historyRepository.findByVisitTimeBetweenAndKeywordsOrderByVisitTime(startTime, endTime, keywords, Long.valueOf(keywords.size()));
                break;
        }
        
        if (histories.isEmpty()) {
            throw new HistoryNotFoundException("No corresponding histories that match the keywords in the given time range");
        }

        return histories.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public int getKeywordFrequency(LocalDateTime startTime, LocalDateTime endTime, String keyword) {
        List<History> histories = historyRepository.findByKeyword(keyword);
        if (histories.isEmpty()) {
            return 0;
        }
        return histories.size();       
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalSpentTime(LocalDateTime startTime, LocalDateTime endTime, String keyword) {
        List<History> histories = historyRepository.findByKeyword(keyword);
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