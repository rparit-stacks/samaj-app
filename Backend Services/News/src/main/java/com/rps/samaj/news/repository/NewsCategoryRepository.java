package com.rps.samaj.news.repository;

import com.rps.samaj.news.entity.NewsCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsCategoryRepository extends JpaRepository<NewsCategory, Long> {

    Optional<NewsCategory> findBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);
}

