package com.hangout.core.post_api.dto;

import java.util.List;

public record PostsList<T>(List<T> posts, Integer currentPage, Integer totalPages) {

}
