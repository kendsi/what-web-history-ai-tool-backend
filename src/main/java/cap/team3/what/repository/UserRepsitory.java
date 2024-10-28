package cap.team3.what.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cap.team3.what.model.User;

import java.util.Optional;

@Repository
public interface UserRepsitory extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
