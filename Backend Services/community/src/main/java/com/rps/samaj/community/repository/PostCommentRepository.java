package com.rps.samaj.community.repository;

import com.rps.samaj.community.entity.Post;
import com.rps.samaj.community.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    Page<PostComment> findByPostOrderByCreatedAtAsc(Post post, Pageable pageable);
}

