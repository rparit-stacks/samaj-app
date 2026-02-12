package com.rps.samaj.community.repository;

import com.rps.samaj.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findByTags_SlugIgnoreCaseOrderByCreatedAtDesc(String slug, Pageable pageable);

    @Query("""
            select p from Post p
            where (:authorId is null or p.authorUserId = :authorId)
            order by p.createdAt desc
            """)
    Page<Post> searchByAuthor(
            @Param("authorId") java.util.UUID authorId,
            Pageable pageable
    );
}

