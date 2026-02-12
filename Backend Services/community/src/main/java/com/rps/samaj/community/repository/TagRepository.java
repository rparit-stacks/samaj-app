package com.rps.samaj.community.repository;

import com.rps.samaj.community.entity.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findBySlugIgnoreCase(String slug);

    Optional<Tag> findByNameIgnoreCase(String name);

    @Query("""
            select t from Tag t
            order by t.name asc
            """)
    List<Tag> findAllAlphabetical();

    @Query("""
            select t.id, t.name, t.slug, count(p)
            from Tag t
            left join t.posts p
            group by t.id, t.name, t.slug
            order by count(p) desc, t.name asc
            """)
    List<Object[]> findTagsWithPostCounts(Pageable pageable);
}


