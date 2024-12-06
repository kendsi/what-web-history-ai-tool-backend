package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.List;

import cap.team3.what.dto.DetailedHistoryResponseDto;
import cap.team3.what.dto.HistoryRequestDto;
import cap.team3.what.dto.HistoryResponseDto;
import cap.team3.what.dto.SearchRequestDto;

public interface HistoryService {
    public DetailedHistoryResponseDto saveHistory(HistoryRequestDto historyDto);
    public DetailedHistoryResponseDto getDetailedHistoryById(Long id);
    public HistoryResponseDto getHistoryByUrl(String url);
    public List<HistoryResponseDto> getHistoriesByTime(LocalDateTime startTime, LocalDateTime endTime, String orderBy);
    public DetailedHistoryResponseDto updateHistory(String url, int spentTime, String category);
    public void deleteHistory(String url);
    public List<HistoryResponseDto> searchHistory(String query);
    public List<HistoryResponseDto> searchHistory(SearchRequestDto searchRequestDto);
    public int getTotalSpentTime(LocalDateTime startTime, LocalDateTime endTime, String keyword);
    public int getKeywordFrequency(LocalDateTime startTime, LocalDateTime endTime, String keyword);
    public List<String> getDomainFrequency(LocalDateTime startTime, LocalDateTime endTime, int k);
    public List<String> getCategoryFrequency(LocalDateTime startTime, LocalDateTime endTime, int k);
}