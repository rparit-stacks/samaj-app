package com.rps.samaj.community.service;

import com.rps.samaj.community.dto.CommunityDtos;
import com.rps.samaj.community.entity.Post;
import com.rps.samaj.community.entity.PostComment;
import com.rps.samaj.community.entity.PostMedia;
import com.rps.samaj.community.entity.PostReport;
import com.rps.samaj.community.entity.Tag;
import com.rps.samaj.community.entity.UserPostInteraction;
import com.rps.samaj.community.repository.PostCommentRepository;
import com.rps.samaj.community.repository.PostReportRepository;
import com.rps.samaj.community.repository.PostRepository;
import com.rps.samaj.community.repository.TagRepository;
import com.rps.samaj.community.repository.UserPostInteractionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommunityService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final UserPostInteractionRepository interactionRepository;
    private final PostReportRepository postReportRepository;
    private final PostCommentRepository commentRepository;

    public CommunityService(PostRepository postRepository,
                            TagRepository tagRepository,
                            UserPostInteractionRepository interactionRepository,
                            PostReportRepository postReportRepository,
                            PostCommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.interactionRepository = interactionRepository;
        this.postReportRepository = postReportRepository;
        this.commentRepository = commentRepository;
    }

    /* ── Posts CRUD ───────────────────────────────────────────── */

    public CommunityDtos.PostResponse createPost(UUID authorUserId, CommunityDtos.CreatePostRequest request) {
        Post post = new Post();
        post.setAuthorUserId(authorUserId);
        post.setContent(Optional.ofNullable(request.content()).orElse("").trim());
        post.setLocation(request.location());
        post.setEmojiCodes(safeList(request.emojiCodes()));
        post.setMentionedUserIds(safeList(request.mentionedUserIds()));

        // Tags
        Set<Tag> tags = resolveTags(request.tags());
        post.setTags(tags);

        // Media
        List<PostMedia> media = buildMediaEntities(post, request.media());
        post.setMedia(media);

        Post saved = postRepository.save(post);
        return toPostResponse(saved, authorUserId, Map.of());
    }

    public CommunityDtos.PostResponse updatePost(Long id, UUID authorUserId, CommunityDtos.UpdatePostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        if (!post.getAuthorUserId().equals(authorUserId)) {
            throw new IllegalStateException("You can only edit your own posts");
        }

        if (request.content() != null) {
            post.setContent(request.content().trim());
        }
        if (request.location() != null) {
            post.setLocation(request.location());
        }
        if (request.emojiCodes() != null) {
            post.setEmojiCodes(safeList(request.emojiCodes()));
        }
        if (request.mentionedUserIds() != null) {
            post.setMentionedUserIds(safeList(request.mentionedUserIds()));
        }
        if (request.tags() != null) {
            post.setTags(resolveTags(request.tags()));
        }
        if (request.media() != null) {
            post.getMedia().clear();
            post.getMedia().addAll(buildMediaEntities(post, request.media()));
        }

        Post saved = postRepository.save(post);
        Map<Long, UserPostInteraction> interactions = getInteractionsForUser(authorUserId, List.of(saved.getId()));
        return toPostResponse(saved, authorUserId, interactions);
    }

    public void deletePost(Long id, UUID authorUserId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        if (!post.getAuthorUserId().equals(authorUserId)) {
            throw new IllegalStateException("You can only delete your own posts");
        }
        postRepository.delete(post);
    }

    /* ── Feed ────────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public Page<CommunityDtos.PostResponse> getFeed(UUID currentUserId,
                                                    String tagSlug,
                                                    UUID authorId,
                                                    boolean savedOnly,
                                                    int page,
                                                    int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));

        if (savedOnly) {
            // Saved posts for current user
            if (currentUserId == null) {
                return Page.empty(pageable);
            }
            Page<UserPostInteraction> interactionsPage =
                    interactionRepository.findByUserIdAndSavedIsTrue(currentUserId, pageable);
            List<Post> posts = interactionsPage.getContent().stream()
                    .map(UserPostInteraction::getPost)
                    .toList();
            List<Long> postIds = posts.stream().map(Post::getId).toList();
            Map<Long, UserPostInteraction> interactions =
                    getInteractionsForUser(currentUserId, postIds);

            List<CommunityDtos.PostResponse> dtos = posts.stream()
                    .map(p -> toPostResponse(p, currentUserId, interactions))
                    .toList();

            return new PageImpl<>(dtos, pageable, interactionsPage.getTotalElements());
        }

        Page<Post> pageResult;
        if (tagSlug != null && !tagSlug.isBlank()) {
            pageResult = postRepository.findByTags_SlugIgnoreCaseOrderByCreatedAtDesc(tagSlug, pageable);
        } else if (authorId != null) {
            pageResult = postRepository.searchByAuthor(authorId, pageable);
        } else {
            pageResult = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        List<Post> posts = pageResult.getContent();
        List<Long> postIds = posts.stream().map(Post::getId).toList();

        Map<Long, UserPostInteraction> interactions =
                currentUserId != null ? getInteractionsForUser(currentUserId, postIds) : Map.of();

        List<CommunityDtos.PostResponse> dtos = posts.stream()
                .map(p -> toPostResponse(p, currentUserId, interactions))
                .toList();

        return new PageImpl<>(dtos, pageable, pageResult.getTotalElements());
    }

    /* ── Reactions & views ───────────────────────────────────── */

    public CommunityDtos.PostResponse toggleLike(Long postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        UserPostInteraction interaction = interactionRepository
                .findByUserIdAndPost_Id(userId, postId)
                .orElseGet(() -> {
                    UserPostInteraction i = new UserPostInteraction();
                    i.setUserId(userId);
                    i.setPost(post);
                    return i;
                });

        boolean currentlyLiked = interaction.isLiked();
        interaction.setLiked(!currentlyLiked);
        interactionRepository.save(interaction);

        long likeDelta = interaction.isLiked() ? 1 : -1;
        post.setLikeCount(Math.max(0, post.getLikeCount() + likeDelta));
        postRepository.save(post);

        Map<Long, UserPostInteraction> interactions =
                getInteractionsForUser(userId, List.of(postId));
        return toPostResponse(post, userId, interactions);
    }

    public CommunityDtos.PostResponse toggleSave(Long postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        UserPostInteraction interaction = interactionRepository
                .findByUserIdAndPost_Id(userId, postId)
                .orElseGet(() -> {
                    UserPostInteraction i = new UserPostInteraction();
                    i.setUserId(userId);
                    i.setPost(post);
                    return i;
                });

        boolean currentlySaved = interaction.isSaved();
        interaction.setSaved(!currentlySaved);
        interactionRepository.save(interaction);

        long saveDelta = interaction.isSaved() ? 1 : -1;
        post.setSaveCount(Math.max(0, post.getSaveCount() + saveDelta));
        postRepository.save(post);

        Map<Long, UserPostInteraction> interactions =
                getInteractionsForUser(userId, List.of(postId));
        return toPostResponse(post, userId, interactions);
    }

    public void trackView(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }

    public void incrementShare(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        post.setShareCount(post.getShareCount() + 1);
        postRepository.save(post);
    }

    /* ── Comments ───────────────────────────────────────────── */

    public CommunityDtos.CommentResponse addComment(Long postId, UUID authorUserId,
                                                    CommunityDtos.CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        String content = Optional.ofNullable(request.content())
                .map(String::trim)
                .orElse("");
        if (content.isBlank()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setAuthorUserId(authorUserId);
        comment.setContent(content);
        PostComment saved = commentRepository.save(comment);

        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return new CommunityDtos.CommentResponse(
                saved.getId(),
                post.getId(),
                saved.getAuthorUserId(),
                saved.getContent(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<CommunityDtos.CommentResponse> getComments(Long postId, int page, int size) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<PostComment> comments = commentRepository.findByPostOrderByCreatedAtAsc(post, pageable);

        List<CommunityDtos.CommentResponse> dtos = comments.getContent().stream()
                .map(c -> new CommunityDtos.CommentResponse(
                        c.getId(),
                        post.getId(),
                        c.getAuthorUserId(),
                        c.getContent(),
                        c.getCreatedAt()
                ))
                .toList();

        return new PageImpl<>(dtos, pageable, comments.getTotalElements());
    }

    /* ── Tags & analytics ───────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<CommunityDtos.TagWithCountResponse> getTopTags(int limit) {
        int size = (limit <= 0 || limit > 50) ? 20 : limit;
        var pageable = PageRequest.of(0, size);
        List<Object[]> rows = tagRepository.findTagsWithPostCounts(pageable);
        return rows.stream()
                .map(r -> new CommunityDtos.TagWithCountResponse(
                        (Long) r[0],
                        (String) r[1],
                        (String) r[2],
                        (Long) r[3]
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public CommunityDtos.AnalyticsResponse getAnalytics(UUID userId) {
        long totalPosts = postRepository.count();
        long totalLikesGiven = interactionRepository.countByUserIdAndLikedIsTrue(userId);
        long totalLikesReceived = interactionRepository.countByPost_AuthorUserIdAndLikedIsTrue(userId);
        long totalSaves = interactionRepository.countByUserIdAndSavedIsTrue(userId);

        long totalViews = postRepository.findAll().stream()
                .mapToLong(Post::getViewCount)
                .sum();

        return new CommunityDtos.AnalyticsResponse(
                totalPosts,
                totalLikesGiven,
                totalLikesReceived,
                totalSaves,
                totalViews
        );
    }

    /* ── Reports ────────────────────────────────────────────── */

    public void reportPost(Long postId, UUID reporterUserId, CommunityDtos.ReportPostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        PostReport report = new PostReport();
        report.setPost(post);
        report.setReporterUserId(reporterUserId);
        report.setReason(Optional.ofNullable(request.reason()).orElse("OTHER"));
        report.setDetails(request.details());
        postReportRepository.save(report);
    }

    /* ── Helpers ────────────────────────────────────────────── */

    private Map<Long, UserPostInteraction> getInteractionsForUser(UUID userId, List<Long> postIds) {
        if (userId == null || postIds == null || postIds.isEmpty()) return Map.of();
        return interactionRepository.findByUserIdAndPost_IdIn(userId, postIds).stream()
                .collect(Collectors.toMap(i -> i.getPost().getId(), i -> i));
    }

    private List<String> safeList(List<String> list) {
        if (list == null) return new ArrayList<>();
        return list.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private Set<Tag> resolveTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return new HashSet<>();

        Set<Tag> tags = new HashSet<>();
        for (String raw : tagNames) {
            if (raw == null || raw.isBlank()) continue;
            String name = raw.trim();
            String slug = slugify(name);

            Tag tag = tagRepository.findBySlugIgnoreCase(slug)
                    .orElseGet(() -> {
                        Tag t = new Tag();
                        t.setName(name);
                        t.setSlug(slug);
                        return tagRepository.save(t);
                    });
            tags.add(tag);
        }
        return tags;
    }

    private List<PostMedia> buildMediaEntities(Post post, List<CommunityDtos.CreatePostMediaRequest> mediaRequests) {
        if (mediaRequests == null || mediaRequests.isEmpty()) return new ArrayList<>();

        List<PostMedia> mediaList = new ArrayList<>();
        int index = 0;
        for (CommunityDtos.CreatePostMediaRequest m : mediaRequests) {
            if (m == null || m.url() == null || m.url().isBlank() || m.type() == null) continue;
            PostMedia media = new PostMedia();
            media.setPost(post);
            media.setUrl(m.url());
            media.setType(m.type());
            media.setSortOrder(m.sortOrder() != null ? m.sortOrder() : index++);
            mediaList.add(media);
        }
        return mediaList;
    }

    private CommunityDtos.PostResponse toPostResponse(Post post,
                                                      UUID currentUserId,
                                                      Map<Long, UserPostInteraction> interactions) {
        UserPostInteraction interaction = interactions.getOrDefault(post.getId(), null);
        boolean liked = interaction != null && interaction.isLiked();
        boolean saved = interaction != null && interaction.isSaved();

        List<CommunityDtos.PostMediaDto> mediaDtos = post.getMedia().stream()
                .map(m -> new CommunityDtos.PostMediaDto(
                        m.getId(),
                        m.getUrl(),
                        m.getType(),
                        m.getSortOrder()
                ))
                .toList();

        List<CommunityDtos.PostTagDto> tagDtos = post.getTags().stream()
                .map(t -> new CommunityDtos.PostTagDto(
                        t.getId(),
                        t.getName(),
                        t.getSlug()
                ))
                .toList();

        return new CommunityDtos.PostResponse(
                post.getId(),
                post.getAuthorUserId(),
                post.getContent(),
                post.getLocation(),
                List.copyOf(post.getEmojiCodes()),
                List.copyOf(post.getMentionedUserIds()),
                tagDtos,
                mediaDtos,
                post.getLikeCount(),
                post.getCommentCount(),
                post.getSaveCount(),
                post.getShareCount(),
                post.getViewCount(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                liked,
                saved
        );
    }

    private String slugify(String input) {
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("[^\\w-]", "").toLowerCase(Locale.ROOT);
        return slug.length() > 64 ? slug.substring(0, 64) : slug;
    }
}

