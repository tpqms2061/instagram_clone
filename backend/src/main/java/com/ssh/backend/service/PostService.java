package com.ssh.backend.service;

import com.ssh.backend.dto.PostRequest;
import com.ssh.backend.dto.PostResponse;
import com.ssh.backend.entity.Post;
import com.ssh.backend.entity.User;
import com.ssh.backend.exception.ResourceNotFoundException;
import com.ssh.backend.exception.UnauthorizedException;
import com.ssh.backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final AuthenticationService authenticationService;

    public PostResponse createPost(PostRequest request) {
        User currentUser = authenticationService.getCurrentUser();
        Post post = Post.builder()
                .content(request.getContent())
                .user(currentUser)
                .deleted(false)
                .build();

        post = postRepository.save(post);
        return PostResponse.fromEntity(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        authenticationService.getCurrentUser();
        Page<Post> posts = postRepository.findAllActive(pageable);
        return posts.map(PostResponse::fromEntity);
    }

    public PostResponse updatePost(Long postId, PostRequest request) {

        //인증절차 성공 후 글씨기 가능
       User currentUser =  authenticationService.getCurrentUser();

        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update this post");
        }
        post.setContent(request.getContent());

        post = postRepository.save(post);
        return PostResponse.fromEntity(post);
    }
}