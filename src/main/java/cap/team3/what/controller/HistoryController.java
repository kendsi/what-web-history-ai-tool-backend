package cap.team3.what.controller;

import cap.team3.what.dto.DetailedHistoryResponseDto;
import cap.team3.what.dto.HistoryRequestDto;
import cap.team3.what.dto.HistoryResponseDto;
import cap.team3.what.dto.SearchRequestDto;
import cap.team3.what.service.HistoryService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/api")
    public ResponseEntity<?> healthCheck() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/history/{id}")
    public ResponseEntity<DetailedHistoryResponseDto> getHistoryById(@PathVariable Long id) {

        DetailedHistoryResponseDto detailedHistory = historyService.getDetailedHistoryById(id);

        return new ResponseEntity<>(detailedHistory, HttpStatus.OK);
    }
    
    @GetMapping("/api/history")
    public ResponseEntity<Page<HistoryResponseDto>> getHistoryByTime(
        @Parameter(description = "Start time for filtering history (optional)", required = false)
        @RequestParam(name = "startTime", required = false) LocalDateTime startTime,
        
        @Parameter(description = "End time for filtering history (optional)", required = false)
        @RequestParam(name = "endTime", required = false) LocalDateTime endTime,

        @Parameter(description = "Domain for filtering history (optional)", required = false)
        @RequestParam(name = "domain", required = false, defaultValue = "") String domain,

        @Parameter(description = "Category for filtering history (optional)", required = false)
        @RequestParam(name = "category", required = false, defaultValue = "") String category,

        Pageable pageable) {

        
        if (startTime == null) {
            startTime = LocalDateTime.of(-4712, 1, 1, 0, 0);
        }
        if (endTime == null) {
            endTime = LocalDateTime.of(294275, 12, 31, 23, 59);
        }

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("start time cannot be after end time");
        }

        Page<HistoryResponseDto> histories = historyService.getHistoriesByTime(startTime, endTime, domain, category, pageable);

        return new ResponseEntity<>(histories, HttpStatus.OK);
    }

    @PostMapping("/api/history")
    public ResponseEntity<DetailedHistoryResponseDto> saveHistory(@RequestBody HistoryRequestDto historyRequestDto) {
        return new ResponseEntity<>(historyService.saveHistory(historyRequestDto), HttpStatus.OK);
    }
    
    @PutMapping("/api/history")
    public ResponseEntity<DetailedHistoryResponseDto> updateHistory(
                @RequestParam(name = "url") String url, 
                @RequestParam(name = "spentTime", defaultValue = "0") int spentTime,
                @RequestParam(name = "category", required = false) String category) {

        if (url == null) {
            throw new IllegalArgumentException("URL is required");
        }

        if (category.isEmpty()) {
            category = "";
        }

        return new ResponseEntity<>(historyService.updateHistory(url, spentTime, category), HttpStatus.OK);
    }

    @DeleteMapping("/api/history")
    public ResponseEntity<String> deleteHistory(@RequestParam(name = "url") String url) {

        if (url == null) {
            throw new IllegalArgumentException("URL is required");
        }

        historyService.deleteHistory(url);


        return new ResponseEntity<String>("History Successfully Deleted", HttpStatus.NO_CONTENT);
    }

    @PostMapping("/api/history/search")
    public ResponseEntity<List<HistoryResponseDto>> searchHistory(@RequestBody SearchRequestDto searchRequestDto) {

        LocalDateTime startTime = searchRequestDto.getStartTime();
        LocalDateTime endTime = searchRequestDto.getEndTime();
        if (startTime == null) {
            startTime = LocalDateTime.of(-4712, 1, 1, 0, 0);
            searchRequestDto.setStartTime(startTime);
        }
        if (endTime == null) {
            endTime = LocalDateTime.of(294275, 12, 31, 23, 59);
            searchRequestDto.setEndTime(endTime);
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("start time cannot be after end time");
        }
        
        List<HistoryResponseDto> result = historyService.searchHistory(searchRequestDto);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/api/history/statistics/{keyword}/spent_time")
    public ResponseEntity<Integer> getTotalSpentTime(
                @Parameter(description = "Start time for filtering history (optional)", required = false)
                @RequestParam(name = "startTime", required = false) LocalDateTime startTime,
                
                @Parameter(description = "End time for filtering history (optional)", required = false)
                @RequestParam(name = "endTime", required = false) LocalDateTime endTime,

                @PathVariable(name = "keyword") String keyword) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now().isBefore(startTime.plusDays(7)) ? LocalDateTime.now() : startTime.plusDays(7);
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("start time cannot be after end time");
        }

        return new ResponseEntity<>(historyService.getTotalSpentTime(startTime, endTime, keyword), HttpStatus.OK);
    }
    
    @GetMapping("/api/history/statistics/{keyword}/frequency")
    public ResponseEntity<Integer> getKeywordFrequency(
                @Parameter(description = "Start time for filtering history (optional)", required = false)
                @RequestParam(name = "startTime", required = false) LocalDateTime startTime,
                
                @Parameter(description = "End time for filtering history (optional)", required = false)
                @RequestParam(name = "endTime", required = false) LocalDateTime endTime,

                @PathVariable(name = "keyword") String keyword) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now().isBefore(startTime.plusDays(7)) ? LocalDateTime.now() : startTime.plusDays(7);
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("start time cannot be after end time");
        }
        return new ResponseEntity<>(historyService.getKeywordFrequency(startTime, endTime, keyword), HttpStatus.OK);
    }

    @GetMapping("/api/history/statistics/domain")
    public ResponseEntity<List<String>> getDomainFrequency(
                @Parameter(description = "Start time for filtering (optional)", required = false)
                @RequestParam(name = "startTime", required = false) LocalDateTime startTime,
                
                @Parameter(description = "End time for filtering (optional)", required = false)
                @RequestParam(name = "endTime", required = false) LocalDateTime endTime,

                @RequestParam(name = "k") int k) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now().isBefore(startTime.plusDays(7)) ? LocalDateTime.now() : startTime.plusDays(7);
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("start time cannot be after end time");
        }

        return new ResponseEntity<>(historyService.getDomainFrequency(startTime, endTime, k), HttpStatus.OK);
    }

    @GetMapping("/api/history/statistics/category")
    public ResponseEntity<List<String>> getCategoryFrequency(
                @Parameter(description = "Start time for filtering (optional)", required = false)
                @RequestParam(name = "startTime", required = false) LocalDateTime startTime,
                
                @Parameter(description = "End time for filtering (optional)", required = false)
                @RequestParam(name = "endTime", required = false) LocalDateTime endTime,

                @RequestParam(name = "k") int k) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now().isBefore(startTime.plusDays(7)) ? LocalDateTime.now() : startTime.plusDays(7);
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("start time cannot be after end time");
        }

        return new ResponseEntity<>(historyService.getCategoryFrequency(startTime, endTime, k), HttpStatus.OK);
    }
}
