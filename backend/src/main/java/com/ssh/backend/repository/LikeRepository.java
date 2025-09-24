//package com.ssh.backend.repository;
//
//import com.ssh.backend.entity.Like;
//import com.ssh.backend.entity.Post;
//import com.ssh.backend.entity.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface LikeRepository extends JpaRepository<Like, Long> {
//    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId")
//    Long countByPostId(@Param("postId") Long postId);
//
//    boolean existByUserAndPost(User user, Post post);
//
//    void deletedByUserAndPost(User user, Post post);
//}