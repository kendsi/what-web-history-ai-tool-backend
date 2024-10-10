package cap.team3.what.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cap.team3.what.model.History;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    List<History> findByVisitTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT h FROM History h JOIN h.keywords k WHERE h.visitTime BETWEEN :startTime AND :endTime AND k.keyword = :keyword")
    List<History> findByVisitTimeBetweenAndKeyword(@Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime,
                                                              @Param("keyword") String keyword);


    @Query("SELECT h FROM History h JOIN h.keywords k WHERE k.keyword = :keyword")
    List<History> findByKeyword(@Param("keyword") String keyword);


    Optional<History> findByUrl(@Param("url") String url);

    @Query("SELECT h FROM History h JOIN h.keywords k " +
           "WHERE h.visitTime BETWEEN :startTime AND :endTime " +
           "AND k.keyword IN :keywords " +
           "GROUP BY h " +
           "HAVING COUNT(DISTINCT k.keyword) = :keywordCount")
    List<History> findByVisitTimeBetweenAndKeywords(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime,
                                                       @Param("keywords") List<String> keywords,
                                                       @Param("keywordCount") Long keywordCount);
}
