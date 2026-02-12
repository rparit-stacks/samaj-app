package com.rps.samaj.userservice.repository;

import com.rps.samaj.userservice.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    @Query("""
            select p from UserProfile p
            where lower(coalesce(p.fullName, '')) like concat('%', :q, '%')
               or lower(coalesce(p.city, '')) like concat('%', :q, '%')
               or lower(coalesce(p.profession, '')) like concat('%', :q, '%')
            """)
    Page<UserProfile> searchByNameOrCityOrProfession(@Param("q") String q, Pageable pageable);
}


