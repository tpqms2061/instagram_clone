package com.ssh.backend.controller;

import com.ssh.backend.dto.PostRequest;
import com.ssh.backend.dto.PostResponse;
import com.ssh.backend.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        PostResponse response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPost(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size
    ) {
        Pageable pageable = PageRequest.of(page,size);
        Page<PostResponse> posts = postService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostRequest request
    ) {
        PostResponse response = postService.updatePost(postId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId{")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

}