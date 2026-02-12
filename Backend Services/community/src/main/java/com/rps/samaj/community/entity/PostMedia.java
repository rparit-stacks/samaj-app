package com.rps.samaj.community.entity;

import com.rps.samaj.community.dto.CommunityDtos;
import jakarta.persistence.*;

@Entity
@Table(name = "post_media")
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 1024)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CommunityDtos.MediaType type;

    @Column(nullable = false)
    private int sortOrder = 0;

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public CommunityDtos.MediaType getType() {
        return type;
    }

    public void setType(CommunityDtos.MediaType type) {
        this.type = type;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}

