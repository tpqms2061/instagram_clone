//package com.ssh.backend.service;
//
//import com.ssh.backend.entity.Like;
//import com.ssh.backend.entity.Post;
//import com.ssh.backend.entity.User;
//import com.ssh.backend.exception.BadRequestException;
//import com.ssh.backend.repository.LikeRepository;
//import com.ssh.backend.repository.PostRepository;
//import com.ssh.backend.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class LikeService {
//
//    private final LikeRepository likeRepository;
//    private final PostRepository postRepository;
//    private final UserRepository userRepository;
//    private final AuthenticationService authenticationService;
//
//    public boolean toggleLike(Long postId) {
//        User currentUser = authenticationService.getCurrentUser();
//
//        Post post = postRepository.findByIdAndNotDeleted(postId)
//                .orElseThrow(() -> new BadRequestException("Post not found"));
//
//        boolean alreadyLiked = likeRepository.existByUserAndPost(currentUser, post);
//
//        if (alreadyLiked) {
//            likeRepository.deletedByUserAndPost(currentUser, post);
//            return false;
//        } else {
//            Like like = Like.builder()
//                    .user(currentUser)
//                    .post(post)
//                    .build();
//            likeRepository.save(like);
//            return true;
//        }
//    }
//}