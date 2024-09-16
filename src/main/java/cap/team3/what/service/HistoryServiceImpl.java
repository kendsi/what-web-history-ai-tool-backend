package cap.team3.what.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import cap.team3.what.dto.HistoryDto;

@Service
public class HistoryServiceImpl implements HistoryService {
    
    @Override
    public void saveHistory(HistoryDto historyDto) {

    }

    @Override
    public HistoryDto getHistory(Long id) {
        return new HistoryDto();
    }

    @Override
    public List<HistoryDto> getHistoriesByDate(LocalDateTime startTime, LocalDateTime endTime) {
        return new ArrayList<>();
    }
}
