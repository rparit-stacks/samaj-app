package com.rps.samaj.community.controller;

import com.rps.samaj.community.dto.CommunityDtos;
import com.rps.samaj.community.service.CommunityService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/community")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    private UUID currentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        return (UUID) authentication.getPrincipal();
    }

    /* ── Posts ──────────────────────────────────────────────── */

    @PostMapping("/posts")
    public ResponseEntity<CommunityDtos.PostResponse> createPost(
            @RequestBody CommunityDtos.CreatePostRequest request,
            Authentication authentication
    ) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(communityService.createPost(userId, request));
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<CommunityDtos.PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody CommunityDtos.UpdatePostRequest request,
            Authentication authentication
    ) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(communityService.updatePost(id, userId, request));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID userId = currentUserId(authentication);
        communityService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<CommunityDtos.PostResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) UUID authorId,
            @RequestParam(defaultValue = "false") boolean savedOnly,
            Authentication authentication
    ) {
        UUID userId = authentication != null ? (UUID) authentication.getPrincipal() : null;
        Page<CommunityDtos.PostResponse> feed = communityService.getFeed(
                userId,
                tag,
                authorId,
                savedOnly,
                page,
                size
        );
        return ResponseEntity.ok(feed);
    }

    /* ── Reactions & views ─────────────────────────────────── */

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<CommunityDtos.PostResponse> toggleLike(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(communityService.toggleLike(id, userId));
    }

    @PostMapping("/posts/{id}/save")
    public ResponseEntity<CommunityDtos.PostResponse> toggleSave(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(communityService.toggleSave(id, userId));
    }

    @PostMapping("/posts/{id}/view")
    public ResponseEntity<Void> trackView(@PathVariable Long id) {
        communityService.trackView(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{id}/share")
    public ResponseEntity<Void> trackShare(@PathVariable Long id) {
        communityService.incrementShare(id);
        return ResponseEntity.ok().build();
    }

    /* ── Comments ───────────────────────────────────────────── */

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<CommunityDtos.CommentResponse> addComment(
            @PathVariable Long id,
            @RequestBody CommunityDtos.CreateCommentRequest request,
            Authentication authentication
    ) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(communityService.addComment(id, userId, request));
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<Page<CommunityDtos.CommentResponse>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(communityService.getComments(id, page, size));
    }

    /* ── Tags & analytics ──────────────────────────────────── */

    @GetMapping("/tags")
    public ResponseEntity<List<CommunityDtos.TagWithCountResponse>> getTopTags(
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(communityService.getTopTags(limit));
    }

    @GetMapping("/me/analytics")
    public ResponseEntity<CommunityDtos.AnalyticsResponse> getMyAnalytics(
            Authentication authentication
    ) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(communityService.getAnalytics(userId));
    }

    /* ── Reports ───────────────────────────────────────────── */

    @PostMapping("/posts/{id}/report")
    public ResponseEntity<Void> reportPost(
            @PathVariable Long id,
            @RequestBody CommunityDtos.ReportPostRequest request,
            Authentication authentication
    ) {
        UUID userId = currentUserId(authentication);
        communityService.reportPost(id, userId, request);
        return ResponseEntity.ok().build();
    }
}

