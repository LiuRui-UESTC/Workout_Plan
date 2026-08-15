package com.project.dev.code_sports_rui_and_fei.repository;

import com.project.dev.code_sports_rui_and_fei.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByPartnerCode(String partnerCode);
    boolean existsByUsername(String username);
}
