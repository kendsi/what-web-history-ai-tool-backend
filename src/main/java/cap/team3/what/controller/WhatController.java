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
    public ResponseEntity<List<HistoryDto>> getHistoryByTime(@RequestParam LocalDateTime startTime, @RequestParam LocalDateTime endTime, @RequestParam(defaultValue = "visitTime") String orderBy) {
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

    @PostMapping("/api/history")
    public ResponseEntity<HistoryDto> saveHistory(@RequestBody HistoryDto historyDto) {
        HistoryDto savedHistory = historyService.saveHistory(historyDto);
        return new ResponseEntity<>(savedHistory, HttpStatus.OK);
    }
    
    @PutMapping("/api/history")
    public ResponseEntity<HistoryDto> updateHistory(@RequestParam String url, @RequestParam(value = "spentTime", defaultValue = "0") int spentTime) {
        return new ResponseEntity<>(historyService.updateHistory(url, spentTime), HttpStatus.OK);
    }

    @PutMapping("/api/history/keyword")
    public ResponseEntity<List<String>> extractKeywords(@RequestParam String url) {
        List<String> keywords = historyService.extractKeywords(url);

        return new ResponseEntity<>(keywords, HttpStatus.OK);
    }

    @DeleteMapping("/api/history")
    public ResponseEntity<String> deleteHistory(@RequestParam String url) {
        historyService.deleteHistory(url);
        return new ResponseEntity<String>("History Successfully Deleted", HttpStatus.NO_CONTENT);
    }
    
    @GetMapping("/api/history/keyword")
    public ResponseEntity<List<HistoryDto>> getHistoryByTimeAndKeyword(@RequestParam LocalDateTime startTime, @RequestParam LocalDateTime endTime, @RequestParam(defaultValue = "visitTime") String orderBy, @RequestParam List<String> keywords) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }

        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("start time cannot be after end time");
        }

        List<HistoryDto> histories = historyService.getHistoriesByTime(startTime, endTime, orderBy, keywords);
        return new ResponseEntity<>(histories, HttpStatus.OK);
    }

    @GetMapping("/api/history/statistics/{keyword}/frequency")
    public int getKeywordFrequency(@RequestParam LocalDateTime startTime, @RequestParam LocalDateTime endTime, @PathVariable String keyword) {

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

    @GetMapping("/api/history/statistics/{keyword}/spent_time")
    public int getTotalSpentTime(@RequestParam LocalDateTime startTime, @RequestParam LocalDateTime endTime, @PathVariable String keyword) {

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
