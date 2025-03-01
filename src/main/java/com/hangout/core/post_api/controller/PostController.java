package com.hangout.core.post_api.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hangout.core.post_api.dto.GetParticularPostProjection;
import com.hangout.core.post_api.dto.GetPostsDTO;
import com.hangout.core.post_api.dto.PostCreationResponse;
import com.hangout.core.post_api.dto.PostsList;
import com.hangout.core.post_api.services.PostService;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/post")
public class PostController {
    private final PostService postService;

    @Observed(name = "create-post", contextualName = "controller", lowCardinalityKeyValues = { "postType",
            "MEDIA+TEXT" })
    @PostMapping(path = "/full", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostCreationResponse> createPostWithMediasAndText(
            @RequestHeader(name = "Authorization") String authToken,
            @RequestPart(value = "file") MultipartFile file,
            @RequestPart(value = "postDescription") String postDescription,
            @RequestPart(value = "state") String state,
            @RequestPart(value = "city") String city,
            @RequestPart(value = "lat") Double lat,
            @RequestPart(value = "lon") Double lon) throws FileUploadException {
        return new ResponseEntity<>(
                this.postService.create(authToken, file, Optional.of(postDescription), state, city, lat, lon),
                HttpStatus.CREATED);
    }

    @Observed(name = "create-post", contextualName = "controller", lowCardinalityKeyValues = { "postType", "MEDIA" })
    @PostMapping(path = "/short", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostCreationResponse> createPostWithMedias(
            @RequestHeader(name = "Authorization") String authToken,
            @RequestPart(value = "file") MultipartFile file,
            @RequestPart(value = "state") String state,
            @RequestPart(value = "city") String city,
            @RequestPart(value = "lat") Double lat,
            @RequestPart(value = "lon") Double lon) throws FileUploadException {
        return new ResponseEntity<>(this.postService.create(authToken, file, Optional.empty(), state, city, lat, lon),
                HttpStatus.CREATED);
    }

    @Observed(name = "get-all-posts", contextualName = "controller")
    @PostMapping("/near-me")
    public PostsList getNearByPosts(@RequestBody GetPostsDTO getPostParams) {
        return this.postService.findNearByPosts(getPostParams);
    }

    @Observed(name = "get-particular-post", contextualName = "controller")
    @GetMapping("/{postId}")
    public GetParticularPostProjection getAParticularPost(@PathVariable UUID postId) {
        return this.postService.getParticularPost(postId);
    }

    @Observed(name = "get my posts", contextualName = "controller")
    @GetMapping("/my-posts")
    public List<GetParticularPostProjection> getMyPosts(@RequestHeader(name = "Authorization") String authToken) {
        return this.postService.getMyPosts(authToken);
    }
}
