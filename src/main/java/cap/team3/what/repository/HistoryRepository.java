package cap.team3.what.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT h FROM History h JOIN h.keywords k WHERE k.keyword = :keyword")
    List<History> findByKeyword(@Param("keyword") String keyword);
 
    Optional<History> findByUserAndUrl(User user, String url);

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
    Page<History> findByVisitTimeBetween(
        @Param("user") User user,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable);


    @Query("""
        SELECT h
        FROM History h
        LEFT JOIN h.category c
        WHERE h.user.id = :userId
        AND h.visitTime BETWEEN :startTime AND :endTime
        AND (:domain = '' OR h.url LIKE CONCAT('%', :domain, '%'))
        AND (:categoryName = '' OR c.name = :categoryName)
        """)
    Page<History> findByVisitTimeBetweenAndFilters(
        @Param("userId") Long userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("domain") String domain,
        @Param("categoryName") String categoryName,
        Pageable pageable);
                                                                    


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

    // 방문 시각 내에 domain 상위 k개
    @Query(value = """
        SELECT domain
        FROM (
            SELECT REGEXP_REPLACE(SPLIT_PART(url, '/', 3), '^(https?://|http://|//)?', '') AS domain
            FROM history h
            WHERE h.user_id = ?1
            AND h.visit_time BETWEEN ?2 AND ?3
        ) AS domain_extraction
        GROUP BY domain
        ORDER BY COUNT(*) DESC
        LIMIT ?4
    """, nativeQuery = true)
    List<String> findTopKDistinctDomains(Long userId,
                                        LocalDateTime startDate,
                                        LocalDateTime endDate,
                                        int k);

    @Query(value = """
        SELECT c.name
        FROM history h
        JOIN category c ON h.category_id = c.id
        WHERE h.user_id = ?1
        AND h.visit_time BETWEEN ?2 AND ?3
        GROUP BY c.name
        ORDER BY COUNT(h.id) DESC
        LIMIT ?4
    """, nativeQuery = true)
    List<String> findTopKCategories(Long userId,
                                    LocalDateTime startDate,
                                    LocalDateTime endDate,
                                    int k);

    @Modifying
    @Query("UPDATE History h SET h.category.id = :etcCategoryId WHERE h.category.id = :categoryId")
    void updateCategoryToEtc(@Param("categoryId") Long categoryId, @Param("etcCategoryId") Long etcCategoryId);
}
