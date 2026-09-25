package com.gus.finsight.repository;

import com.gus.finsight.entity.Statement;
import com.gus.finsight.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface StatementRepository extends JpaRepository <Statement, Long> {

    long countByUser(User user);

    void deleteByUser(com.gus.finsight.entity.User user);

    List<Statement> findByUser(User user);
    
}
