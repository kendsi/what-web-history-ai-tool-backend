package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.List;

import cap.team3.what.dto.HistoryDto;

public interface HistoryService {
    public HistoryDto saveHistory(HistoryDto historyDto);
    public HistoryDto getHistory(String url);
    public HistoryDto updateHistory(String url, int spentTime);
    public List<String> extractKeywords(String url);
    public void deleteHistory(String url);
    public List<HistoryDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, String orderBy);
    public List<HistoryDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, String orderBy, List<String> keywords);
    public int getKeywordFrequency(LocalDateTime startTime, LocalDateTime endTime, String keyword);
    public int getTotalSpentTime(LocalDateTime startTime, LocalDateTime endTime, String keyword);
}