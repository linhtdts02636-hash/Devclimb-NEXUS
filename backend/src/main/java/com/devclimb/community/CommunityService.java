package com.devclimb.community;

import com.devclimb.common.exception.ApiException;
import com.devclimb.common.exception.ErrorCode;
import com.devclimb.community.entity.Community;
import com.devclimb.community.repository.CommunityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;

    public CommunityService(CommunityRepository communityRepository) {
        this.communityRepository = communityRepository;
    }

    @Transactional(readOnly = true)
    public CommunityResponse getBySlug(String slug) {
        String requestedSlug = CommunitySlug.from(slug);

        if (requestedSlug.isEmpty()) {
            throw new ApiException(ErrorCode.COMMUNITY_NOT_FOUND);
        }

        List<Community> matches = communityRepository.findByActiveTrue().stream()
                .filter(community -> CommunitySlug.from(community.getName()).equals(requestedSlug))
                .toList();

        if (matches.isEmpty()) {
            throw new ApiException(ErrorCode.COMMUNITY_NOT_FOUND);
        }

        if (matches.size() > 1) {
            throw new ApiException(ErrorCode.COMMUNITY_SLUG_CONFLICT);
        }

        return CommunityResponse.from(matches.get(0));
    }

    @Transactional(readOnly = true)
    public CommunityResponse getById(Integer id) {
        return communityRepository.findById(id)
                .filter(Community::isActive)
                .map(CommunityResponse::from)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMUNITY_NOT_FOUND));
    }
}
