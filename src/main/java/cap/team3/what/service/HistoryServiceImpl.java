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
import cap.team3.what.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;
    private final AIService aiService;
    
    @Override
    @Transactional
    public HistoryDto saveHistory(HistoryDto historyDto) {

        History newHistory = convertToModel(historyDto);

        return convertToDto(historyRepository.save(newHistory));
    }

    @Override
    public HistoryDto getHistory(Long id) {
        History history = historyRepository.findById(id)
                .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));
        return convertToDto(history);
    }

    @Override
    @Transactional
    public HistoryDto updateHistory(Long id, int spentTime) {
        History history = historyRepository.findById(id)
                .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));

        history.setSpentTime(spentTime);
        if (history.getKeywords().isEmpty()) {
            List<String> keywords = aiService.extractKeywords(history.getContent());
            history.setKeywords(new ArrayList<>(keywords));
        }
            
        return convertToDto(historyRepository.save(history));
    }

    @Override
    @Transactional
    public List<String> extractKeywords(Long id) {
        History history = historyRepository.findById(id)
                .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));

        List<String> keywords = aiService.extractKeywords(history.getContent());
        history.setKeywords(keywords);
        historyRepository.save(history);
        return keywords;
    }

    @Override
    @Transactional
    public void deleteHistory(Long id) {
        History history = historyRepository.findById(id)
                .orElseThrow(() -> new HistoryNotFoundException("No such history in DB"));
        
        historyRepository.delete(history);
    }

    @Override
    public List<HistoryDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime) {

        List<History> histories = historyRepository.findByVisitTimeBetween(startTime, endTime);
        if (histories.isEmpty()) {
            throw new HistoryNotFoundException("No histories in the time");
        }

        return histories.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
    }

    @Override
    public List<HistoryDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, String keyword) {

        List<History> histories = historyRepository.findByVisitTimeBetweenAndKeywordsContaining(startTime, endTime, keyword);
        if (histories.isEmpty()) {
            throw new HistoryNotFoundException("No corresponding histories that matches with keyword in the time");
        }

        return histories.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
    }

    @Override
    public int getKeywordFrequency(LocalDateTime startTime, LocalDateTime endTime, String keyword) {
        List<History> histories = historyRepository.findByKeywordsContaining(keyword);
        if (histories.isEmpty()) {
            return 0;
        }
        return histories.size();       
    }

    @Override
    public int getTotalSpentTime(LocalDateTime startTime, LocalDateTime endTime, String keyword) {
        List<History> histories = historyRepository.findByKeywordsContaining(keyword);
        if (histories.isEmpty()) {
            throw new HistoryNotFoundException("No such history in DB");
            
        }
        int totalSpentTime = histories.stream()
                    .mapToInt(History::getSpentTime)
                    .sum();
            
        return totalSpentTime;
    }

    private History convertToModel(HistoryDto historyDto) {
        return History.builder()
                      .title(historyDto.getTitle())
                      .content(historyDto.getContent())
                      .url(historyDto.getUrl())
                      .spentTime(historyDto.getSpentTime())
                      .visitTime(historyDto.getVisitTime())
                      .keywords(historyDto.getKeywords())
                      .build();
    }

    private HistoryDto convertToDto(History history) {
        return HistoryDto.builder()
                         .id(history.getId())
                         .title(history.getTitle())
                         .content(history.getContent())
                         .url(history.getUrl())
                         .spentTime(history.getSpentTime())
                         .visitTime(history.getVisitTime())
                         .keywords(history.getKeywords())
                         .build();
    }
}