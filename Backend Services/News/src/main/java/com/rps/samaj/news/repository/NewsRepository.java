package com.rps.samaj.news.repository;

import com.rps.samaj.news.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    @Query("""
            SELECT n FROM News n
            WHERE n.active = true
              AND (:categoryId IS NULL OR n.category.id = :categoryId)
              AND (:q IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(n.summary) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY n.pinned DESC, n.publishedAt DESC
            """)
    Page<News> search(
            @Param("categoryId") Long categoryId,
            @Param("q") String q,
            Pageable pageable
    );

    @Query("""
            SELECT n FROM News n
            WHERE n.active = true
              AND n.pinned = true
            ORDER BY n.publishedAt DESC
            """)
    List<News> findTopByPinned(Pageable pageable);

    @Query("""
            SELECT n FROM News n
            WHERE n.active = true
              AND n.id <> :excludeId
              AND n.category.id = :categoryId
            ORDER BY n.publishedAt DESC
            """)
    List<News> findRecommendations(
            @Param("excludeId") Long excludeId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    long countByPinnedTrue();

    @Query("SELECT COALESCE(SUM(n.views), 0) FROM News n")
    long totalViews();

    @Query("SELECT MAX(n.publishedAt) FROM News n WHERE n.active = true")
    LocalDateTime lastPublishedAt();
}

