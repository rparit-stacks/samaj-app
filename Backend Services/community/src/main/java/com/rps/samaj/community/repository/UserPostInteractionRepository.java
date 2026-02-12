package com.rps.samaj.community.repository;

import com.rps.samaj.community.entity.UserPostInteraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPostInteractionRepository extends JpaRepository<UserPostInteraction, Long> {

    Optional<UserPostInteraction> findByUserIdAndPost_Id(UUID userId, Long postId);

    List<UserPostInteraction> findByUserIdAndPost_IdIn(UUID userId, Collection<Long> postIds);

    Page<UserPostInteraction> findByUserIdAndSavedIsTrue(UUID userId, Pageable pageable);

    long countByUserIdAndLikedIsTrue(UUID userId);

    long countByPost_AuthorUserIdAndLikedIsTrue(UUID authorUserId);

    long countByUserIdAndSavedIsTrue(UUID userId);
}

