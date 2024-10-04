package cap.team3.what.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cap.team3.what.model.History;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    List<History> findByVisitTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    List<History> findByVisitTimeBetweenAndKeywordsContaining(LocalDateTime startTime, LocalDateTime endTime, String keyword);
    List<History> findByKeywordsContaining(String keyword);
}
