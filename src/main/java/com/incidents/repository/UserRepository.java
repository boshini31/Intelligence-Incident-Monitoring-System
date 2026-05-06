package com.incidents.repository;

import com.incidents.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for User entity.
 * Spring automatically generates all CRUD SQL at runtime.
 *
 * Interview point: What is JpaRepository?
 * - It's a Spring interface that provides findAll, findById, save, delete etc.
 * - We don't write any SQL - Spring generates queries from method names
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
