package cap.team3.what.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import cap.team3.what.exception.CategoryNotFoundException;
import cap.team3.what.model.Category;
import cap.team3.what.model.User;
import cap.team3.what.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Override
    public void createDefaultCategories() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
        List<String> defaultCategories = List.of("게임", "학습", "엔터테인먼트", "뉴스", "쇼핑", "기타");
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
        category.setName(newName);
        categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(String name) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByEmail(email);
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
