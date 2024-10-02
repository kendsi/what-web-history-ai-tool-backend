package cap.team3.what.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cap.team3.what.model.History;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    Optional<List<History>> findByVisitTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    Optional<List<History>> findByVisitTimeBetweenAndKeyword(LocalDateTime startTime, LocalDateTime endTime, String keyword);
    Optional<List<History>> findByKeyword(String keyword);
}
