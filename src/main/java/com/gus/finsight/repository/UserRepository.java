package com.gus.finsight.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.gus.finsight.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.aiTokens = u.aiTokens - 1 WHERE u.id = :id AND u.aiTokens > 0")
    int decrementAiTokens(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.aiTokens = u.aiTokens + 1 WHERE u.id = :id")
    int incrementAiTokens(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.statementTokens = u.statementTokens - 1 WHERE u.id = :id AND u.statementTokens > 0")
    int decrementStatementTokens(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.statementTokens = u.statementTokens + 1 WHERE u.id = :id")
    int incrementStatementTokens(@Param("id") Long id);
}