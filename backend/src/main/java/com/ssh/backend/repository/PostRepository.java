package com.ssh.backend.repository;

import com.ssh.backend.entity.Post;
import com.ssh.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM Post p WHERE p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findAllActive(Pageable pageable);

    //삭제되지않은 user로 조회
    @EntityGraph(attributePaths = {"user"})
    //조회개수가 많아지면 관련된 녀석들을 반복적으로 수행하는데 N+1 의문제인데
//    entityGraph 를 사용하면 처음 조회할때 묶어서 가져옴
    @Query("SELECT p FROM Post p WHERE p.user = :user AND p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findByUserAndNotDeleted(@Param("user") User user, Pageable pageable);
      //삭제되지않은 userid로 조회

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.deleted = false ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

  //삭제되지않은 postid로 조회
  @Query("SELECT p FROM Post p WHERE p.id = :id AND p.deleted = false")
  Optional<Post> findByIdAndNotDeleted(@Param("id") Long id);

    //삭제되지않은 포스트개수
    @Query("SELECT COUNT(p) FROM Post p WHERE p.user = :user AND p.deleted = false")
    long countByUserAndNotDeleted(@Param("user") User user);

}