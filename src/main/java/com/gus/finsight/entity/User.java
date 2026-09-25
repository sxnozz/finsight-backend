package com.gus.finsight.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity 
@Table(name = "users")
public class User {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_user")
    private Long id;

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="email", nullable = false, unique = true)
    private String email;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public User(String name, String email, String password) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public User() {
    }

    @PrePersist 
    private void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @JsonIgnore
    public String getPassword() {
        return password;
    }
   public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @jakarta.persistence.Column(name = "ai_tokens", nullable = false)
    private Integer aiTokens = 3;

    public Integer getAiTokens() {
        return aiTokens;
    }

    public void setAiTokens(Integer aiTokens) {
        this.aiTokens = aiTokens;
    }

    @jakarta.persistence.Column(name = "verification_code")
    private String verificationCode;

    @jakarta.persistence.Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    @jakarta.persistence.Column(name = "statement_tokens", nullable = false)
    private Integer statementTokens = 3;

    public Integer getStatementTokens() { return statementTokens; }
    public void setStatementTokens(Integer statementTokens) { this.statementTokens = statementTokens; }
}