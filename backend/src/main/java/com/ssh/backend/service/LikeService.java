package com.ssh.backend.service;

import com.ssh.backend.entity.Like;
import com.ssh.backend.entity.Post;
import com.ssh.backend.entity.User;
import com.ssh.backend.exception.BadRequestException;
import com.ssh.backend.repository.LikeRepository;
import com.ssh.backend.repository.PostRepository;
import com.ssh.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    public boolean toggleLike(Long postId) {
        User currentUser = authenticationService.getCurrentUser(); //인증된 유저 확인

        //삭제되어있지않은 포스트를 찾고 예외처리
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new BadRequestException("Post not found"));

        //좋아요가 되어있는지 확인
        boolean alreadyLiked = likeRepository.existsByUserAndPost(currentUser, post);

        //좋아요가 되어있으면
        if (alreadyLiked) {
            //좋아요 취소
            likeRepository.deleteByUserAndPost(currentUser, post);
            return false;
        } else {
            //안되어있으면 좋아요 표시
            Like like = Like.builder()
                    .user(currentUser)
                    .post(post)
                    .build();
            likeRepository.save(like);
            return true;
        }
    }

//좋아요 개수 확인
    @Transactional(readOnly = true)
    public Long getLikeCount(Long postId) {
        return likeRepository.countByPostId(postId);}

    //내가 좋아요 표시한것 확인
    @Transactional(readOnly = true)
    public boolean isLikedByCurrentUser(Long postId) {
        User currentUser = authenticationService.getCurrentUser();

        //삭제되어있지않은 포스트를 찾고 예외처리
        Post post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new BadRequestException("Post not found"));

        return likeRepository.existsByUserAndPost(currentUser, post);
    }
}