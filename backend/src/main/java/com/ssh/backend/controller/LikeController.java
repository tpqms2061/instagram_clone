package com.ssh.backend.controller;

import com.ssh.backend.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{postId}")
    private ResponseEntity<Boolean> toggleLike(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.toggleLike(postId));
    }

    @GetMapping("/count/{postId}")
    public ResponseEntity<Long> countLike(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.getLikeCount(postId));
    }

    @GetMapping("/is-liked/{postId}")
    public ResponseEntity<Boolean> isLikedByMe(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.isLikedByCurrentUser(postId));
    }



}