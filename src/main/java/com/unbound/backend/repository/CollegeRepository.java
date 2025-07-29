package com.unbound.backend.repository;

import com.unbound.backend.entity.College;
import com.unbound.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollegeRepository extends JpaRepository<College, Integer> {
    Optional<College> findByUser(User user);
} 