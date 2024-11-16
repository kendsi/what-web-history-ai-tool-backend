package cap.team3.what.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cap.team3.what.dto.VectorMetaData;
import cap.team3.what.service.VectorStoreService;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestController {
    
    private final VectorStoreService vectorStoreService;

    @PostMapping("/vector/save")
    public ResponseEntity<?> saveVectorDB(@RequestBody VectorMetaData vectorMetaData) {
        vectorStoreService.addDocument(vectorMetaData);

        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @GetMapping("/vector/search")
    public ResponseEntity<List<VectorMetaData>> getDocuments(@RequestParam String query) {
        return new ResponseEntity<>(vectorStoreService.searchSimilarDocuments(query, 5, "test@example.com"), HttpStatus.OK);
    }
    
}
