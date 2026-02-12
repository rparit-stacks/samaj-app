package com.rps.samaj.community.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private UUID authorUserId;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(length = 255)
    private String location;

    @ElementCollection
    @CollectionTable(name = "post_emojis", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "emoji_code", length = 32)
    private List<String> emojiCodes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "post_mentions", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "mentioned_user_id", length = 64)
    private List<String> mentionedUserIds = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<PostMedia> media = new ArrayList<>();

    @Column(nullable = false)
    private long likeCount = 0L;

    @Column(nullable = false)
    private long commentCount = 0L;

    @Column(nullable = false)
    private long saveCount = 0L;

    @Column(nullable = false)
    private long shareCount = 0L;

    @Column(nullable = false)
    private long viewCount = 0L;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(UUID authorUserId) {
        this.authorUserId = authorUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getEmojiCodes() {
        return emojiCodes;
    }

    public void setEmojiCodes(List<String> emojiCodes) {
        this.emojiCodes = emojiCodes != null ? emojiCodes : new ArrayList<>();
    }

    public List<String> getMentionedUserIds() {
        return mentionedUserIds;
    }

    public void setMentionedUserIds(List<String> mentionedUserIds) {
        this.mentionedUserIds = mentionedUserIds != null ? mentionedUserIds : new ArrayList<>();
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags != null ? tags : new HashSet<>();
    }

    public List<PostMedia> getMedia() {
        return media;
    }

    public void setMedia(List<PostMedia> media) {
        this.media = media != null ? media : new ArrayList<>();
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public long getSaveCount() {
        return saveCount;
    }

    public void setSaveCount(long saveCount) {
        this.saveCount = saveCount;
    }

    public long getShareCount() {
        return shareCount;
    }

    public void setShareCount(long shareCount) {
        this.shareCount = shareCount;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

