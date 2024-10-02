package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cap.team3.what.dto.HistoryDto;
import cap.team3.what.model.History;
import cap.team3.what.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;
    private final AIService aiService;
    
    @Override
    public HistoryDto saveHistory(HistoryDto historyDto) {

        History newHistory = convertToModel(historyDto);

        return convertToDto(historyRepository.save(newHistory));
    }

    @Override
    public HistoryDto getHistory(Long id) {
        Optional<History> history = historyRepository.findById(id);
        if (history.isPresent()) {
            return convertToDto(history.get());
        } else {
            throw new RuntimeException("No such history in DB");
        }
    }

    @Override
    public HistoryDto updateHistory(Long id, int spentTime) {
        Optional<History> history = historyRepository.findById(id);
        if (history.isPresent()) {
            History updatedHistory = history.get();
            
            List<String> keywords = aiService.extractKeywords(updatedHistory.getContent());

            updatedHistory.setSpentTime(spentTime);
            updatedHistory.setKeywords(keywords);

            return convertToDto(historyRepository.save(updatedHistory));
        } else {
            throw new RuntimeException("No such history in DB");
        }
    }

    @Override
    public List<HistoryDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime) {
        Optional<List<History>> histories = historyRepository.findByVisitTimeBetween(startTime, endTime);
        if (histories.isPresent()) {
            return histories.get().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
        }
        else {
            throw new RuntimeException("No histories in the time");
        }
    }

    @Override
    public List<HistoryDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, String keyword) {
        Optional<List<History>> histories = historyRepository.findByVisitTimeBetweenAndKeyword(startTime, endTime, keyword);
        if (histories.isPresent()) {
            return histories.get().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
        }
        else {
            throw new RuntimeException("No corresponding histories that matches with keyword in the time");
        }
    }

    @Override
    public int getKeywordFrequency(String keyword) {
        Optional<List<History>> histories = historyRepository.findByKeyword(keyword);
        if (histories.isPresent()) {
            return histories.get().size();
        }
        else {
            return 0;
        }        
    }

    @Override
    public int getTotalSpentTime(String keyword) {
        Optional<List<History>> histories = historyRepository.findByKeyword(keyword);
        if (histories.isPresent()) {
            int totalSpentTime = histories.get().stream()
                .mapToInt(History::getSpentTime)
                .sum();
            
            return totalSpentTime;
        }
        else {
            throw new RuntimeException("No such history in DB");
        }  
    }

    private History convertToModel(HistoryDto historyDto) {
        return History.builder()
                      .title(historyDto.getTitle())
                      .content(historyDto.getContent())
                      .domain(historyDto.getDomain())
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
                         .domain(history.getDomain())
                         .spentTime(history.getSpentTime())
                         .visitTime(history.getVisitTime())
                         .keywords(history.getKeywords())
                         .build();
    }
}
