package com.rps.samaj.userservice.repository;

import com.rps.samaj.userservice.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, UUID> {

    List<FamilyMember> findByUserId(UUID userId);
}

