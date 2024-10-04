package cap.team3.what.controller;

import cap.team3.what.dto.HistoryDto;
import cap.team3.what.service.HistoryService;

import lombok.RequiredArgsConstructor;

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

@RestController
@RequiredArgsConstructor
public class WhatController {

    private final HistoryService historyService;

    @GetMapping("/api")
    public ResponseEntity<?> healthCheck() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
    

    @GetMapping("/api/history")
    public ResponseEntity<List<HistoryDto>> getHistoryByDate(@RequestParam(value = "days", defaultValue = "7") int days) {
        List<HistoryDto> histories = historyService.getHistoriesByTime(LocalDateTime.now().minusDays(days), LocalDateTime.now());
        return new ResponseEntity<>(histories, HttpStatus.OK);
    }

    @PostMapping("/api/history")
    public ResponseEntity<HistoryDto> saveHistory(@RequestBody HistoryDto historyDto) {
        HistoryDto savedHistory = historyService.saveHistory(historyDto);
        return new ResponseEntity<>(savedHistory, HttpStatus.OK);
    }
    
    @PutMapping("/api/history/{id}")
    public ResponseEntity<HistoryDto> updateHistory(@PathVariable Long id, @RequestParam(value = "spentTime", defaultValue = "0") int spentTime) {
        return new ResponseEntity<>(historyService.updateHistory(id, spentTime), HttpStatus.OK);
    }

    @PutMapping("/api/history/{id}/keyword")
    public ResponseEntity<List<String>> extractKeywords(@PathVariable Long id) {
        List<String> keywords = historyService.extractKeywords(id);

        return new ResponseEntity<>(keywords, HttpStatus.OK);
    }

    @DeleteMapping("/api/history/{id}")
    public ResponseEntity<String> deleteHistory(@PathVariable Long id) {
        historyService.deleteHistory(id);
        return new ResponseEntity<String>("History Successfully Deleted", HttpStatus.NO_CONTENT);
    }
    
    @GetMapping("/api/history/{keyword}")
    public ResponseEntity<List<HistoryDto>> getHistoryByDateAndKeyword(@RequestParam(value = "days", defaultValue = "7") int days, @PathVariable String keyword) {
        List<HistoryDto> histories = historyService.getHistoriesByTime(LocalDateTime.now().minusDays(days), LocalDateTime.now(), keyword);
        return new ResponseEntity<>(histories, HttpStatus.OK);
    }

    @GetMapping("/api/history/statistics/{keyword}/frequency")
    public int getKeywordFrequency(@RequestParam(value = "days", defaultValue = "7") int days, @PathVariable String keyword) {
        return historyService.getKeywordFrequency(LocalDateTime.now().minusDays(days), LocalDateTime.now(), keyword);
    }

    @GetMapping("/api/history/statistics/{keyword}/spent_time")
    public int getTotalSpentTime(@RequestParam(value = "days", defaultValue = "7") int days, @PathVariable String keyword) {
        return historyService.getTotalSpentTime(LocalDateTime.now().minusDays(days), LocalDateTime.now(), keyword);
    }
}
