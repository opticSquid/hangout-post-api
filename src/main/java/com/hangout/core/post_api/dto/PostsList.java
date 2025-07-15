package com.hangout.core.post_api.dto;

import java.util.List;

public record PostsList(List<GetNearbyPostsProjection> posts, Integer currentPage, Integer totalPages) {

}
