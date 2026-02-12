package com.rps.samaj.news.service;

import com.rps.samaj.news.dto.NewsDtos;
import com.rps.samaj.news.entity.News;
import com.rps.samaj.news.entity.NewsCategory;
import com.rps.samaj.news.repository.NewsCategoryRepository;
import com.rps.samaj.news.repository.NewsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;
    private final NewsCategoryRepository categoryRepository;

    public NewsService(NewsRepository newsRepository, NewsCategoryRepository categoryRepository) {
        this.newsRepository = newsRepository;
        this.categoryRepository = categoryRepository;
    }

    /* Categories */

    public List<NewsDtos.NewsCategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    public NewsDtos.NewsCategoryResponse createCategory(NewsDtos.CreateCategoryRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }
        NewsCategory category = new NewsCategory();
        category.setName(request.name().trim());
        category.setSlug(generateSlug(request.name()));
        return toCategoryResponse(categoryRepository.save(category));
    }

    public NewsDtos.NewsCategoryResponse updateCategory(Long id, NewsDtos.UpdateCategoryRequest request) {
        NewsCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found"));
        if (request.name() != null && !request.name().isBlank()) {
            category.setName(request.name().trim());
            category.setSlug(generateSlug(request.name()));
        }
        return toCategoryResponse(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    /* News */

    @Transactional(readOnly = true)
    public Page<NewsDtos.NewsResponse> listNews(Integer page, Integer size, Long categoryId, String q) {
        Pageable pageable = PageRequest.of(page != null && page >= 0 ? page : 0,
                size != null && size > 0 ? size : 20);
        String query = (q == null || q.isBlank()) ? null : q.trim();
        Page<News> pageResult = newsRepository.search(categoryId, query, pageable);
        return pageResult.map(this::toNewsResponse);
    }

    @Transactional(readOnly = true)
    public List<NewsDtos.NewsResponse> listPinned(int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1));
        return newsRepository.findTopByPinned(pageable).stream()
                .map(this::toNewsResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NewsDtos.NewsResponse getById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("News not found"));
        if (!news.isActive()) {
            throw new NoSuchElementException("News not found");
        }
        return toNewsResponse(news);
    }

    public NewsDtos.NewsResponse createNews(NewsDtos.CreateNewsRequest request) {
        validateCreate(request);
        NewsCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NoSuchElementException("Category not found"));
        News news = new News();
        news.setTitle(request.title().trim());
        news.setSummary(request.summary().trim());
        news.setContent(request.content().trim());
        news.setCategory(category);
        news.setImageUrl(request.imageUrl());
        news.setPinned(Boolean.TRUE.equals(request.pinned()));
        news.setPublishedAt(request.publishedAt() != null ? request.publishedAt() : LocalDateTime.now());
        return toNewsResponse(newsRepository.save(news));
    }

    public NewsDtos.NewsResponse updateNews(Long id, NewsDtos.UpdateNewsRequest request) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("News not found"));
        if (request.title() != null && !request.title().isBlank()) {
            news.setTitle(request.title().trim());
        }
        if (request.summary() != null && !request.summary().isBlank()) {
            news.setSummary(request.summary().trim());
        }
        if (request.content() != null && !request.content().isBlank()) {
            news.setContent(request.content().trim());
        }
        if (request.categoryId() != null) {
            NewsCategory category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NoSuchElementException("Category not found"));
            news.setCategory(category);
        }
        if (request.imageUrl() != null) {
            news.setImageUrl(request.imageUrl());
        }
        if (request.publishedAt() != null) {
            news.setPublishedAt(request.publishedAt());
        }
        if (request.pinned() != null) {
            news.setPinned(request.pinned());
        }
        if (request.active() != null) {
            news.setActive(request.active());
        }
        return toNewsResponse(newsRepository.save(news));
    }

    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }

    public NewsDtos.NewsResponse pin(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("News not found"));
        news.setPinned(true);
        return toNewsResponse(newsRepository.save(news));
    }

    public NewsDtos.NewsResponse unpin(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("News not found"));
        news.setPinned(false);
        return toNewsResponse(newsRepository.save(news));
    }

    public void trackView(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("News not found"));
        news.incrementViews();
        newsRepository.save(news);
    }

    @Transactional(readOnly = true)
    public List<NewsDtos.NewsResponse> recommendations(Long id, int limit) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("News not found"));
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1));
        return newsRepository.findRecommendations(news.getId(), news.getCategory().getId(), pageable).stream()
                .map(this::toNewsResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NewsDtos.NewsStatsResponse stats() {
        long total = newsRepository.count();
        long pinned = newsRepository.countByPinnedTrue();
        long totalViews = newsRepository.totalViews();
        LocalDateTime lastPublishedAt = newsRepository.lastPublishedAt();
        return new NewsDtos.NewsStatsResponse(total, pinned, totalViews, lastPublishedAt);
    }

    private void validateCreate(NewsDtos.CreateNewsRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.summary() == null || request.summary().isBlank()) {
            throw new IllegalArgumentException("Summary is required");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("Content is required");
        }
        if (request.categoryId() == null) {
            throw new IllegalArgumentException("Category is required");
        }
    }

    private NewsDtos.NewsCategoryResponse toCategoryResponse(NewsCategory category) {
        return new NewsDtos.NewsCategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug()
        );
    }

    private NewsDtos.NewsResponse toNewsResponse(News news) {
        return new NewsDtos.NewsResponse(
                news.getId(),
                news.getTitle(),
                news.getSummary(),
                news.getContent(),
                news.getCategory().getId(),
                news.getCategory().getName(),
                news.getImageUrl(),
                news.isPinned(),
                news.getPublishedAt(),
                news.getViews()
        );
    }

    private String generateSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "category" : slug;
    }
}

