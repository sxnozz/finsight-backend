package com.gus.finsight.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gus.finsight.entity.Transaction;
import com.gus.finsight.entity.User;

public interface TransactionRepository extends JpaRepository <Transaction, Long> {
    

    List<Transaction> findAllByUser(User user);


    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate startDate, LocalDate endDate);

    void deleteByUser(com.gus.finsight.entity.User user);

    List<com.gus.finsight.entity.Transaction> findByUser(com.gus.finsight.entity.User user);

    List<Transaction> findByStatement(com.gus.finsight.entity.Statement statement);
}
