package cap.team3.what.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cap.team3.what.model.History;
import cap.team3.what.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    
    List<History> findByUrl(String url);

    // 사용자의 기록을 spentTime으로 정렬하여 조회
   @Query("SELECT h FROM History h WHERE h.user = :user ORDER BY h.spentTime DESC")
   List<History> findOrderBySpentTime(@Param("user") User user);


   // 사용자의 기록을 visitCount으로 정렬하여 조회
   @Query("SELECT h FROM History h WHERE h.user = :user ORDER BY h.visitCount DESC")
   List<History> findOrderByVisitCount(@Param("user") User user);

   // 사용자의 기록을 visitTime으로 정렬하여 조회
   @Query("SELECT h FROM History h WHERE h.user = :user ORDER BY h.visitTime DESC")
   List<History> findOrderByVisitTime(@Param("user") User user);


    // 사용자와 방문 시간 사이의 기록 조회
   @Query("SELECT h FROM History h WHERE h.user = :user AND h.visitTime BETWEEN :startTime AND :endTime")
   List<History> findByVisitTimeBetween(@Param("user") User user,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

   // 사용자와 방문 시간 사이의 기록을 spentTime으로 정렬하여 조회
   @Query("SELECT h FROM History h WHERE h.user = :user AND h.visitTime BETWEEN :startTime AND :endTime ORDER BY h.spentTime DESC")
   List<History> findByVisitTimeBetweenOrderBySpentTime(@Param("user") User user,
                                                               @Param("startTime") LocalDateTime startTime,
                                                               @Param("endTime") LocalDateTime endTime);

   // 사용자와 방문 시간 사이의 기록을 visitCount로 정렬하여 조회
   @Query("SELECT h FROM History h WHERE h.user = :user AND h.visitTime BETWEEN :startTime AND :endTime ORDER BY h.visitCount DESC")
   List<History> findByVisitTimeBetweenOrderByVisitCount(@Param("user") User user,
                                                               @Param("startTime") LocalDateTime startTime,
                                                               @Param("endTime") LocalDateTime endTime);

   // 사용자와 방문 시간 사이의 기록을 visitTime으로 정렬하여 조회
   @Query("SELECT h FROM History h WHERE h.user = :user AND h.visitTime BETWEEN :startTime AND :endTime ORDER BY h.visitTime DESC")
   List<History> findByVisitTimeBetweenOrderByVisitTime(@Param("user") User user,
                                                               @Param("startTime") LocalDateTime startTime,
                                                               @Param("endTime") LocalDateTime endTime);

   // 사용자와 키워드 및 방문 시간 조건으로 조회
   @Query("SELECT h FROM History h JOIN h.keywords k WHERE h.user = :user AND h.visitTime BETWEEN :startTime AND :endTime AND k.keyword = :keyword")
   List<History> findByVisitTimeBetweenAndKeyword(@Param("user") User user,
                                                         @Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime,
                                                         @Param("keyword") String keyword);

   // 사용자와 키워드 목록 및 방문 시간 조건으로 조회
   @Query("SELECT h FROM History h JOIN h.keywords k WHERE h.user = :user AND h.visitTime BETWEEN :startTime AND :endTime AND k.keyword IN :keywords GROUP BY h HAVING COUNT(DISTINCT k.keyword) = :keywordCount")
   List<History> findByVisitTimeBetweenAndKeywords(@Param("user") User user,
                                                         @Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime,
                                                         @Param("keywords") List<String> keywords,
                                                         @Param("keywordCount") Long keywordCount);

   // 특정 사용자, 방문 시간 범위, 키워드를 기반으로 spentTime 기준 내림차순으로 조회
   @Query("SELECT h FROM History h JOIN h.keywords k " +
   "WHERE h.user = :user AND h.visitTime BETWEEN :startTime AND :endTime " +
   "AND k.keyword IN :keywords " +
   "GROUP BY h " +
   "HAVING COUNT(DISTINCT k.keyword) = :keywordCount " +
   "ORDER BY h.spentTime DESC")
   List<History> findByVisitTimeBetweenAndKeywordsOrderBySpentTime(@Param("user") User user,
                                                                        @Param("startTime") LocalDateTime startTime,
                                                                        @Param("endTime") LocalDateTime endTime,
                                                                        @Param("keywords") List<String> keywords,
                                                                        @Param("keywordCount") Long keywordCount);

   // 특정 사용자, 방문 시간 범위, 키워드를 기반으로 visitCount 기준 내림차순으로 조회
   @Query("SELECT h FROM History h JOIN h.keywords k " +
   "WHERE h.user = :user AND h.visitTime BETWEEN :startTime AND :endTime " +
   "AND k.keyword IN :keywords " +
   "GROUP BY h " +
   "HAVING COUNT(DISTINCT k.keyword) = :keywordCount " +
   "ORDER BY h.visitCount DESC")
   List<History> findByVisitTimeBetweenAndKeywordsOrderByVisitCount(@Param("user") User user,
                                                                           @Param("startTime") LocalDateTime startTime,
                                                                           @Param("endTime") LocalDateTime endTime,
                                                                           @Param("keywords") List<String> keywords,
                                                                           @Param("keywordCount") Long keywordCount);

   // 특정 사용자, 방문 시간 범위, 키워드를 기반으로 visitTime 기준 내림차순으로 조회
   @Query("SELECT h FROM History h JOIN h.keywords k " +
   "WHERE h.user = :user AND h.visitTime BETWEEN :startTime AND :endTime " +
   "AND k.keyword IN :keywords " +
   "GROUP BY h " +
   "HAVING COUNT(DISTINCT k.keyword) = :keywordCount " +
   "ORDER BY h.visitTime DESC")
   List<History> findByVisitTimeBetweenAndKeywordsOrderByVisitTime(@Param("user") User user,
                                                                        @Param("startTime") LocalDateTime startTime,
                                                                        @Param("endTime") LocalDateTime endTime,
                                                                        @Param("keywords") List<String> keywords,
                                                                        @Param("keywordCount") Long keywordCount);

   @Query("SELECT h FROM History h JOIN h.keywords k WHERE k.keyword = :keyword")
   List<History> findByKeyword(@Param("keyword") String keyword);

   Optional<History> findByUserAndUrl(User user, String url);
}
