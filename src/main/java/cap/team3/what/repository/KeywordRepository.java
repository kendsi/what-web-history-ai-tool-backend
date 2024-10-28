package cap.team3.what.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cap.team3.what.model.Keyword;

import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Optional<Keyword> findByKeyword(String keyword);
    @Query("SELECT COUNT(h) FROM History h JOIN h.keywords k WHERE k = :keyword")
    long countByKeywordsContains(@Param("keyword") Keyword keyword);
}
