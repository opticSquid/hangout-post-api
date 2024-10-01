package com.hangout.core.post.controller;

import java.util.List;

import io.micrometer.observation.annotation.Observed;
import org.springframework.web.bind.annotation.*;

import com.hangout.core.post.dto.PostDTO;
import com.hangout.core.post.entities.Post;
import com.hangout.core.post.services.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {
    private final PostService postService;

    @Observed(name = "create-post", contextualName = "controller")
    @PostMapping
    public String createPost(@ModelAttribute PostDTO post) {
        return postService.create(post);
    }

    @GetMapping("/all")
    public List<Post> getAllPosts() {
        return postService.findAll();
    }

    @GetMapping("/{postId}")
    public Post getAParticularPost(@PathVariable String postId) {
        return postService.getParticularPost(postId);
    }
}