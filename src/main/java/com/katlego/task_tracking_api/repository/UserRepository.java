package com.katlego.task_tracking_api.repository;

import com.katlego.task_tracking_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("""
       SELECT u FROM User u
       JOIN FETCH u.role
       WHERE u.email = :email
       """)
    Optional<User> findByEmailWithRole(@Param("email") String email);
}