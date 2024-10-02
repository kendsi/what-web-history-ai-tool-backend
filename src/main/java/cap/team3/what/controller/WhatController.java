package cap.team3.what.controller;

import cap.team3.what.dto.HistoryDto;
import cap.team3.what.service.HistoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/api/history")
    public ResponseEntity<List<HistoryDto>> getHistoryByDate(@RequestParam int days) {
        List<HistoryDto> histories = historyService.getHistoriesByTime(LocalDateTime.now().minusDays(days), LocalDateTime.now());
        return new ResponseEntity<>(histories, HttpStatus.OK);
    }

    @PostMapping("/api/history")
    public ResponseEntity<HistoryDto> saveHistory(@RequestBody HistoryDto historyDto) {
        HistoryDto savedHistory = historyService.saveHistory(historyDto);
        return new ResponseEntity<>(savedHistory, HttpStatus.OK);
    }
    
    @PutMapping("/api/history")
    public ResponseEntity<HistoryDto> generateKeywords(@RequestParam Long id, @RequestParam int spentTime) {
        return new ResponseEntity<>(historyService.updateHistory(id, spentTime), HttpStatus.OK);
    }
    
    @GetMapping("/api/history/keyword")
    public ResponseEntity<List<HistoryDto>> getHistoryByDateAndKeyword(@RequestParam int days, @RequestParam String keyword) {
        List<HistoryDto> histories = historyService.getHistoriesByTime(LocalDateTime.now().minusDays(days), LocalDateTime.now(), keyword);
        return new ResponseEntity<>(histories, HttpStatus.OK);
    }

    @GetMapping("/api/statistics/{keyword}/frequency")
    public int getKeywordFrequency(@PathVariable String keyword) {
        return historyService.getKeywordFrequency(keyword);
    }

    @GetMapping("/api/statistics/{keyword}/spent_time")
    public int getTotalSpentTime(@PathVariable String keyword) {
        return historyService.getTotalSpentTime(keyword);
    }
}
