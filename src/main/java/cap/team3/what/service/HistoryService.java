package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.List;

import cap.team3.what.dto.HistoryDto;
import cap.team3.what.dto.VectorMetaData;

public interface HistoryService {
    public HistoryDto saveHistory(HistoryDto historyDto);
    public HistoryDto getHistoryById(Long id);
    public HistoryDto getHistoryByUrl(String url);
    public List<HistoryDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, String orderBy);
    public HistoryDto updateHistory(String url, int spentTime);
    public VectorMetaData analyzeHistory(String url);
    public void deleteHistory(String url);
    // public List<HistoryDto> searchHistory(String query);
    // public List<HistoryDto> searchHistory(LocalDateTime startTime, LocalDateTime endTime, String query);

    public int getKeywordFrequency(LocalDateTime startTime, LocalDateTime endTime, String keyword);
    public int getTotalSpentTime(LocalDateTime startTime, LocalDateTime endTime, String keyword);
}