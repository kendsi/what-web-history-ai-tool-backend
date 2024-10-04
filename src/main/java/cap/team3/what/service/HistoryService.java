package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.List;

import cap.team3.what.dto.HistoryDto;

public interface HistoryService {
    public HistoryDto saveHistory(HistoryDto historyDto);
    public HistoryDto getHistory(Long id);
    public HistoryDto updateHistory(Long id, int spentTime);
    public void deleteHistory(Long id);
    public List<HistoryDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime);
    public List<HistoryDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, String keyword);
    public int getKeywordFrequency(String keyword);
    public int getTotalSpentTime(String keyword);
}