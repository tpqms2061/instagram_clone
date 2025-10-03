package com.ssh.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssh.backend.entity.Post;
import jdk.jfr.Frequency;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PostResponse {

    private Long id;
    private String content;
    private String username;
    private String imageUrl;

    private UserDto user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long likeCount;
    private  boolean isLiked;
    private Long commentCount;

   /* @JsonProperty("isOwner") //백엔드와 겹쳐서 삭제
    private boolean isOwner;*/

    public static PostResponse fromEntity(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .user(UserDto.fromEntity(post.getUser()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

}
