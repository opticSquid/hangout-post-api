package com.hangout.core.post_api.controller;

import java.util.Optional;
import java.util.UUID;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hangout.core.post_api.dto.GetNearbyPostsProjection;
import com.hangout.core.post_api.dto.GetParticularPostProjection;
import com.hangout.core.post_api.dto.GetPostsDTO;
import com.hangout.core.post_api.dto.PostCreationResponse;
import com.hangout.core.post_api.dto.PostsList;
import com.hangout.core.post_api.services.PostService;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/post")
public class PostController {
        private final PostService postService;

        @WithSpan(kind = SpanKind.SERVER, value = "create-post with description controller")
        @PostMapping(path = "/full", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<PostCreationResponse> createPostWithMediasAndText(
                        @RequestHeader(name = "Authorization") String authToken,
                        @RequestPart(value = "postDescription") String postDescription,
                        @RequestPart(value = "state") String state,
                        @RequestPart(value = "city") String city,
                        @RequestPart(value = "lat") String lat,
                        @RequestPart(value = "lon") String lon,
                        @RequestPart(value = "file") MultipartFile file) throws FileUploadException {
                return new ResponseEntity<>(
                                this.postService.create(authToken, file, Optional.of(postDescription), state, city,
                                                Double.parseDouble(lat), Double.parseDouble(lon)),
                                HttpStatus.CREATED);
        }

        @WithSpan(kind = SpanKind.SERVER, value = "create-post without description")
        @PostMapping(path = "/short", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<PostCreationResponse> createPostWithMedias(
                        @RequestHeader(name = "Authorization") String authToken,
                        @RequestPart(value = "state") String state,
                        @RequestPart(value = "city") String city,
                        @RequestPart(value = "lat") String lat,
                        @RequestPart(value = "lon") String lon,
                        @RequestPart(value = "file") MultipartFile file)
                        throws FileUploadException {
                return new ResponseEntity<>(
                                this.postService.create(authToken, file, Optional.empty(), state, city,
                                                Double.parseDouble(lat),
                                                Double.parseDouble(lon)),
                                HttpStatus.CREATED);
        }

        @WithSpan(kind = SpanKind.SERVER, value = "get-all-posts controller")
        @GetMapping("/near-me")
        public PostsList<GetNearbyPostsProjection> getNearByPosts(
                        @RequestParam Double lat, @RequestParam Double lon,
                        @RequestParam Double minSearchRadius,
                        @RequestParam Double maxSearchRadius,
                        @RequestParam Optional<Integer> pageNumber) {
                GetPostsDTO getPostsDTO = new GetPostsDTO(
                                lat, lon, minSearchRadius, maxSearchRadius,
                                pageNumber.isPresent() ? pageNumber.get() : Integer.valueOf(1));
                return this.postService.findNearByPosts(getPostsDTO);
        }

        @WithSpan(kind = SpanKind.SERVER, value = "get-particular-post controller")
        @GetMapping("/{postId}")
        public GetParticularPostProjection getAParticularPost(@PathVariable UUID postId) {
                return this.postService.getParticularPost(postId);
        }

        @WithSpan(kind = SpanKind.SERVER, value = "get my posts controller")
        @GetMapping("/my-posts")
        public PostsList<GetParticularPostProjection> getMyPosts(
                        @RequestHeader(name = "Authorization") String authToken,
                        @RequestParam Optional<Integer> pageNumber) {
                return this.postService.getMyPosts(authToken,
                                pageNumber.isPresent() ? pageNumber.get() : Integer.valueOf(1));
        }
}
