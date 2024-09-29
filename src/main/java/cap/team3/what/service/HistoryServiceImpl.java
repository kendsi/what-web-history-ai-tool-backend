package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;



import cap.team3.what.dto.HistoryDto;
import cap.team3.what.model.History;
import cap.team3.what.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;
    
    @Override
    public HistoryDto saveHistory(HistoryDto historyDto) {

        History newHistory = convertToModel(historyDto);

        return convertToDto(historyRepository.save(newHistory));
    }

    @Override
    public HistoryDto getHistory(Long id) {
        return HistoryDto.builder().build();
    }

    @Override
    public List<HistoryDto> getHistoriesByDate(LocalDateTime startTime, LocalDateTime endTime) {
        return new ArrayList<>();
    }

    private History convertToModel(HistoryDto historyDto) {
        return History.builder()
                      .title(historyDto.getTitle())
                      .content(historyDto.getContent())
                      .domain(historyDto.getDomain())
                      .spentTime(historyDto.getSpentTime())
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
                         .keywords(history.getKeywords())
                         .build();
    }
}
