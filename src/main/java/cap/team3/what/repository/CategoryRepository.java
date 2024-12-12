package cap.team3.what.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cap.team3.what.model.Category;
import cap.team3.what.model.User;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByUserAndName(User user, String name);
    List<Category> findByUser(User user);
    void deleteByUserAndName(User user, String name);
}
