package com.rps.samaj.userservice.controller;

import com.rps.samaj.userservice.dto.UserSearchDto;
import com.rps.samaj.userservice.entity.UserProfile;
import com.rps.samaj.userservice.repository.UserProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserSearchController {

    private final UserProfileRepository userProfileRepository;

    public UserSearchController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserSearchDto>> searchUsers(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        String q = query == null ? "" : query.trim().toLowerCase();

        Page<UserProfile> result;
        if (q.isEmpty()) {
            result = userProfileRepository.findAll(pageable);
        } else {
            result = userProfileRepository.searchByNameOrCityOrProfession(q, pageable);
        }

        List<UserSearchDto> content = result.getContent().stream()
                .map(p -> new UserSearchDto(
                        p.getUserId(),
                        p.getFullName(),
                        p.getCity(),
                        p.getProfession(),
                        p.getAvatarUrl()
                ))
                .toList();

        return ResponseEntity.ok(new PageImpl<>(content, pageable, result.getTotalElements()));
    }
}

