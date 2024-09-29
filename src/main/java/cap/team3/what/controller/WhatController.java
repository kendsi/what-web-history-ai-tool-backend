package cap.team3.what.controller;

import cap.team3.what.dto.HistoryDto;
import cap.team3.what.service.AIService;
import cap.team3.what.service.HistoryService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class WhatController {

    private final HistoryService historyService;
    private final AIService aiService;

    @PostMapping("/api/history")
    public ResponseEntity<HistoryDto> generateKeywords(@RequestBody HistoryDto historyDto) {
        historyDto.setKeywords(aiService.extractKeywords(historyDto.getContent()));
        
        return new ResponseEntity<>(historyService.saveHistory(historyDto), HttpStatus.OK);
    }
    

    // @GetMapping("/api/history")
    // public ResponseEntity<?> getHistoryByTime(@RequestBody HistoryDto historyDto) {
    //     return new ResponseEntity<>(null, null);
    // }
    
    // @GetMapping("/api/history")
    // public ResponseEntity<?> getHistoryByTimeAndKeyword(@RequestBody HistoryDto historyDto, @RequestParam int days) {
    //     return new ResponseEntity<>(null, null);
    // }

    @GetMapping("/api/statistics")
    public int getMethodName(@RequestParam String keyword) {
        return 0;
    }
    
    
}
