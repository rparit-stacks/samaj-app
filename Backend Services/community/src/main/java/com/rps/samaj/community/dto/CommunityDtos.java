package com.rps.samaj.community.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CommunityDtos {

    public enum MediaType {
        IMAGE,
        VIDEO
    }

    public record PostMediaDto(
            Long id,
            String url,
            MediaType type,
            int sortOrder
    ) {}

    public record PostTagDto(
            Long id,
            String name,
            String slug
    ) {}

    public record PostResponse(
            Long id,
            UUID authorUserId,
            String content,
            String location,
            List<String> emojiCodes,
            List<String> mentionedUserIds,
            List<PostTagDto> tags,
            List<PostMediaDto> media,
            long likeCount,
            long commentCount,
            long saveCount,
            long shareCount,
            long viewCount,
            Instant createdAt,
            Instant updatedAt,
            boolean likedByCurrentUser,
            boolean savedByCurrentUser
    ) {}

    public record CreatePostMediaRequest(
            String url,
            MediaType type,
            Integer sortOrder
    ) {}

    public record CreatePostRequest(
            String content,
            String location,
            List<String> emojiCodes,
            List<String> mentionedUserIds,
            List<String> tags,
            List<CreatePostMediaRequest> media
    ) {}

    public record UpdatePostRequest(
            String content,
            String location,
            List<String> emojiCodes,
            List<String> mentionedUserIds,
            List<String> tags,
            List<CreatePostMediaRequest> media
    ) {}

    public record TagWithCountResponse(
            Long id,
            String name,
            String slug,
            long postCount
    ) {}

    public record AnalyticsResponse(
            long totalPosts,
            long totalLikesGiven,
            long totalLikesReceived,
            long totalSaves,
            long totalViews
    ) {}

    public record ReportPostRequest(
            String reason,
            String details
    ) {}

    public record CommentResponse(
            Long id,
            Long postId,
            UUID authorUserId,
            String content,
            Instant createdAt
    ) {}

    public record CreateCommentRequest(
            String content
    ) {}
}

