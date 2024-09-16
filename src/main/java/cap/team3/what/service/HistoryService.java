package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.List;

import cap.team3.what.dto.HistoryDto;

public interface HistoryService {
    public void saveHistory(HistoryDto historyDto);
    public HistoryDto getHistory(Long id);
    public List<HistoryDto> getHistoriesByDate(LocalDateTime startTime, LocalDateTime endTime);
}