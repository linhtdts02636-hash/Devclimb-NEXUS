package com.devclimb.community;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/by-slug/{slug}")
    public CommunityResponse getBySlug(@PathVariable String slug) {
        return communityService.getBySlug(slug);
    }

    @GetMapping("/{id}")
    public CommunityResponse getById(@PathVariable Integer id) {
        return communityService.getById(id);
    }
}
