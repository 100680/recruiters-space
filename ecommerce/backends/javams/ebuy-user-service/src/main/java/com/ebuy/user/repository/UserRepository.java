package com.ebuy.user.repository;

import com.ebuy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.isDeleted = false")
    Optional<User> findByEmailIgnoreCaseAndIsDeletedFalse(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.isDeleted = false")
    Optional<User> findByUserIdAndIsDeletedFalse(@Param("userId") Long userId);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.isDeleted = false")
    boolean existsByEmailIgnoreCaseAndIsDeletedFalse(@Param("email") String email);
}