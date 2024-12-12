package cap.team3.what.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cap.team3.what.exception.CategoryNotFoundException;
import cap.team3.what.exception.DuplicateCategoryException;
import cap.team3.what.model.Category;
import cap.team3.what.model.History;
import cap.team3.what.model.User;
import cap.team3.what.repository.CategoryRepository;
import cap.team3.what.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final HistoryRepository historyRepository;
    private final UserService userService;

    @Override
    public void createDefaultCategories(User user) {
        List<String> defaultCategories = List.of("게임", "학습", "엔터테인먼트", "뉴스", "쇼핑", "스포츠", "기타");
        for (String categoryName : defaultCategories) {
            Category category = new Category(user, categoryName);
            categoryRepository.save(category);
        }
    }

    @Override
    public Category findByName(String name){
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
        Category category = categoryRepository.findByUserAndName(user, name)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + name));
        return category;
    }

    @Override
    public void addCategory(String name) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
        Category category = new Category(user, name);
        categoryRepository.save(category);
    }

    @Override
    public void updateCategory(String originalName, String newName) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);

        Category category = categoryRepository.findByUserAndName(user, originalName)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + originalName));

        categoryRepository.findByUserAndName(user, newName).ifPresent((existingCategory) -> {
            throw new DuplicateCategoryException("Category with name '" + existingCategory.getName() + "' already exists.");
        });

        category.setName(newName);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(String name) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);

        Category categoryToDelete = categoryRepository.findByUserAndName(user, name)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + name));

        Category etcCategory = categoryRepository.findByUserAndName(user, "기타")
            .orElse(null);

        if (etcCategory == null) {
            addCategory("기타");
            etcCategory = categoryRepository.findByUserAndName(user, "기타")
            .orElseThrow(() -> new CategoryNotFoundException("Category not found: 기타"));
        }
        
        historyRepository.updateCategoryToEtc(categoryToDelete.getId(), etcCategory.getId());
        categoryRepository.deleteByUserAndName(user, name);
    }

    @Override
    public List<String> getAllCategories() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
        return categoryRepository.findByUser(user)
                             .stream()
                             .map(Category::getName)
                             .toList();
    }
}
