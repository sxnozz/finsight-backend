package com.gus.finsight.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name="transactions")
public class Transaction {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_transaction")
    private Long id;

    @Column(name="description", nullable = false)
    private String description;

    @Column(name="amount", nullable = false)
    private BigDecimal amount;

    @Column(name="type", nullable = false)
    private String type;

    @Column(name="category")
    private String category;

    @Column(name="transaction_date", nullable = false)
    private LocalDate date;

    @ManyToOne 
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne 
    @JoinColumn(name="statement_id", nullable = false)
    private Statement statement;

    protected Transaction() {
    }

    public Transaction(String description, BigDecimal amount, String type, String category, LocalDate date, User user, Statement statement) {
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.user = user;
        this.statement = statement;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
    public User getUser() { return user; }
    public Statement getStatement() { return statement; }

    public void setCategory(String category) {
        this.category = category;
    }
}