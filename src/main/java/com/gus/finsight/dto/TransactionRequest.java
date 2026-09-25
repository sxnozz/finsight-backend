package com.gus.finsight.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequest {

    private String description;
    private BigDecimal amount;
    private String type; 
    private String category;
    private LocalDate date;

    protected TransactionRequest() {
    }

    public TransactionRequest(String description, BigDecimal amount, String type, String category, LocalDate date) {
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
    }

    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
}