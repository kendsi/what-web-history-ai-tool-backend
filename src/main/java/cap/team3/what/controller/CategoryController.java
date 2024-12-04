package cap.team3.what.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cap.team3.what.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;



@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;
    
    @PostMapping()
    public ResponseEntity<Void> addCategory(@RequestParam String categoryName) {
        categoryService.addCategory(categoryName);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @DeleteMapping()
    public ResponseEntity<Void> deleteCategory(@RequestParam String categoryName) {
        categoryService.deleteCategory(categoryName);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping()
    public ResponseEntity<Void> updateCategory(@RequestBody Map<String, String> categoryMap) {
        String originalName = categoryMap.get("originalName");
        String newName = categoryMap.get("newName");
        categoryService.updateCategory(originalName, newName);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
