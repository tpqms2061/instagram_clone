package com.ssh.backend.repository;

import com.ssh.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String Email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
