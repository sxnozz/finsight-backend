package com.gus.finsight.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gus.finsight.entity.AiReport;
import com.gus.finsight.entity.User;

public interface AiReportRepository extends JpaRepository<AiReport, Long> {
    
    List<AiReport> findByUserOrderByCreatedAtDesc(User user);
    
    void deleteByUser(User user);
}