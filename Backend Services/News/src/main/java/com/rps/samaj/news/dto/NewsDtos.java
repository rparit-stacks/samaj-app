package com.rps.samaj.news.dto;

import java.time.LocalDateTime;

public class NewsDtos {

    public record NewsCategoryResponse(
            Long id,
            String name,
            String slug
    ) {
    }

    public record CreateCategoryRequest(
            String name
    ) {
    }

    public record UpdateCategoryRequest(
            String name
    ) {
    }

    public record NewsResponse(
            Long id,
            String title,
            String summary,
            String content,
            Long categoryId,
            String categoryName,
            String imageUrl,
            boolean pinned,
            LocalDateTime publishedAt,
            long views
    ) {
    }

    public record CreateNewsRequest(
            String title,
            String summary,
            String content,
            Long categoryId,
            String imageUrl,
            LocalDateTime publishedAt,
            Boolean pinned
    ) {
    }

    public record UpdateNewsRequest(
            String title,
            String summary,
            String content,
            Long categoryId,
            String imageUrl,
            LocalDateTime publishedAt,
            Boolean pinned,
            Boolean active
    ) {
    }

    public record NewsStatsResponse(
            long total,
            long pinned,
            long totalViews,
            LocalDateTime lastPublishedAt
    ) {
    }
}

