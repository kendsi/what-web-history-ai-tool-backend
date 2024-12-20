package cap.team3.what.service;

import java.util.List;

import cap.team3.what.model.Category;
import cap.team3.what.model.User;

public interface CategoryService {
    public void createDefaultCategories(User user);
    public Category findByName(String name);
    public void addCategory(String name);
    public void updateCategory(String originalName, String newName);
    public void deleteCategory(String name);
    public List<String> getAllCategories();
}
