package cap.team3.what.controller;

import cap.team3.what.dto.HistoryDto;
import cap.team3.what.service.HistoryService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {

    private final HistoryService historyService;

    @GetMapping("/api/test/history/{id}")
    public ResponseEntity<HistoryDto> getHistoryById(@PathVariable Long id) {

        HistoryDto history = historyService.getHistoryById(id);

        return new ResponseEntity<>(history, HttpStatus.OK);
    }
    
    @GetMapping("/api/test/history")
    public ResponseEntity<List<HistoryDto>> getHistoryByTime(
            @RequestParam(name = "startTime") LocalDateTime startTime,
            @RequestParam(name = "endTime") LocalDateTime endTime,
            @RequestParam(name = "orderBy", defaultValue = "visitTime") String orderBy) {
 

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("start time cannot be after end time");
        }

        List<HistoryDto> histories = historyService.getHistoriesByTime(startTime, endTime, orderBy);
        return new ResponseEntity<>(histories, HttpStatus.OK);
    }

    @PostMapping("/api/test/history")
    public ResponseEntity<HistoryDto> saveHistory(@RequestBody HistoryDto historyDto) {

        HistoryDto savedHistory = historyService.saveHistory(historyDto);
        return new ResponseEntity<>(savedHistory, HttpStatus.OK);
    }
    
    @PutMapping("/api/test/history")
    public ResponseEntity<HistoryDto> updateHistory(
                @RequestParam(name = "url") String url, 
                @RequestParam(name = "spentTime", defaultValue = "0") int spentTime) {

        if (url == null) {
            throw new IllegalArgumentException("URL is required");
        }

        return new ResponseEntity<>(historyService.updateHistory(url, spentTime), HttpStatus.OK);
    }

    @DeleteMapping("/api/test/history")
    public ResponseEntity<String> deleteHistory(@RequestParam(name = "url") String url) {

        if (url == null) {
            throw new IllegalArgumentException("URL is required");
        }

        historyService.deleteHistory(url);


        return new ResponseEntity<String>("History Successfully Deleted", HttpStatus.NO_CONTENT);
    }

    @GetMapping("/api/test/history/search")
    public ResponseEntity<List<HistoryDto>> searchHistory(
        @Parameter(description = "Start time for filtering history (optional)", required = false)
        @RequestParam(name = "startTime", required = false) LocalDateTime startTime,
        
        @Parameter(description = "End time for filtering history (optional)", required = false)
        @RequestParam(name = "endTime", required = false) LocalDateTime endTime,
        
        @RequestParam(name = "query") String query) {

        List<HistoryDto> result;

        if (startTime == null || endTime == null) {
            log.info("No visitTime criteria");
            result = historyService.searchHistory(query);
        }
        else {
            result = historyService.searchHistory(startTime, endTime, query);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    
    @GetMapping("/api/test/history/statistics/{keyword}/frequency")
    public int getKeywordFrequency(
                @RequestParam(name = "startTime") LocalDateTime startTime,
                @RequestParam(name = "endTime") LocalDateTime endTime,
                @PathVariable(name = "keyword") String keyword) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("start time cannot be after end time");
        }

        return historyService.getKeywordFrequency(startTime, endTime, keyword);
    }

    @GetMapping("/api/test/history/statistics/{keyword}/spent_time")
    public int getTotalSpentTime(
                @RequestParam(name = "startTime") LocalDateTime startTime,
                @RequestParam(name = "endTime") LocalDateTime endTime,
                @PathVariable(name = "keyword") String keyword) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("start time cannot be after end time");
        }

        return historyService.getTotalSpentTime(startTime, endTime, keyword);
    }
}
