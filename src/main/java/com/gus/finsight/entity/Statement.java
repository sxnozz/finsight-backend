package com.gus.finsight.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity 
@Table(name="statements")
public class Statement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_statement") 
    private Long id;
    @Column(name="file_name", nullable = false)
    private String fileName;

    @Column(name="uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name="period_start")
    private LocalDate periodStart;

    @Column(name="period_end")
    private LocalDate periodEnd;

    @ManyToOne 
    @JoinColumn(name="id_user", nullable = false)
    private User user;

    protected Statement() {
    }

    public Statement(String fileName, User user) {
        this.fileName = fileName;
        this.user = user;
    }

    @PrePersist
    private void setUploadedAt() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public User getUser() { return user; }
}