package com.rps.samaj.news.controller;

import com.rps.samaj.news.dto.NewsDtos;
import com.rps.samaj.news.service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /* Public - list, detail, search, recommendations */

    @GetMapping
    public ResponseEntity<Page<NewsDtos.NewsResponse>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, name = "q") String query
    ) {
        return ResponseEntity.ok(newsService.listNews(page, size, categoryId, query));
    }

    @GetMapping("/pinned")
    public ResponseEntity<List<NewsDtos.NewsResponse>> pinned(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(newsService.listPinned(limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsDtos.NewsResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getById(id));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> trackView(@PathVariable Long id) {
        newsService.trackView(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<List<NewsDtos.NewsResponse>> recommendations(
            @PathVariable Long id,
            @RequestParam(defaultValue = "4") int limit
    ) {
        return ResponseEntity.ok(newsService.recommendations(id, limit));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<NewsDtos.NewsResponse>> search(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(newsService.listNews(page, size, categoryId, query));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<NewsDtos.NewsResponse>> byCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(newsService.listNews(page, size, categoryId, null));
    }

    /* Public - categories */

    @GetMapping("/categories")
    public ResponseEntity<List<NewsDtos.NewsCategoryResponse>> categories() {
        return ResponseEntity.ok(newsService.getCategories());
    }

    /* Admin-style endpoints (no admin panel integration yet) */

    @PostMapping
    public ResponseEntity<NewsDtos.NewsResponse> create(@RequestBody NewsDtos.CreateNewsRequest request) {
        return ResponseEntity.ok(newsService.createNews(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NewsDtos.NewsResponse> update(
            @PathVariable Long id,
            @RequestBody NewsDtos.UpdateNewsRequest request
    ) {
        return ResponseEntity.ok(newsService.updateNews(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pin")
    public ResponseEntity<NewsDtos.NewsResponse> pin(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.pin(id));
    }

    @DeleteMapping("/{id}/pin")
    public ResponseEntity<NewsDtos.NewsResponse> unpin(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.unpin(id));
    }

    @PostMapping("/categories")
    public ResponseEntity<NewsDtos.NewsCategoryResponse> createCategory(
            @RequestBody NewsDtos.CreateCategoryRequest request
    ) {
        return ResponseEntity.ok(newsService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<NewsDtos.NewsCategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody NewsDtos.UpdateCategoryRequest request
    ) {
        return ResponseEntity.ok(newsService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        newsService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<NewsDtos.NewsStatsResponse> stats() {
        return ResponseEntity.ok(newsService.stats());
    }
}

