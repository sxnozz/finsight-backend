package com.gus.finsight.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gus.finsight.entity.Statement;
import com.gus.finsight.entity.Transaction;
import com.gus.finsight.entity.User;
import com.gus.finsight.repository.StatementRepository;
import com.gus.finsight.repository.TransactionRepository;
import com.gus.finsight.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Value("${finsight.demo.email:demo@finsight.com}")
    private String demoEmail;

    @Value("${finsight.demo.password:demo123}")
    private String demoPassword;

    @Bean
    public CommandLineRunner seedData(UserRepository userRepository, 
                                      TransactionRepository transactionRepository, 
                                      StatementRepository statementRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            
            User demoUser = userRepository.findByEmail(demoEmail).orElseGet(() -> {
                User newUser = new User();
                newUser.setName("Visitante (Recrutador)");
                newUser.setEmail(demoEmail);
                newUser.setPassword(passwordEncoder.encode(demoPassword));
                newUser.setVerified(true);
                return userRepository.save(newUser);
            });

            if (transactionRepository.findByUser(demoUser).isEmpty()) {
                
                Statement demoStatement = new Statement("Extrato_Demo_Nubank.csv", demoUser);
                statementRepository.save(demoStatement);

                LocalDate hoje = LocalDate.now();
                
                Transaction t1 = new Transaction("Salário Desenvolvedor Jr", new BigDecimal("4500.00"), "INCOME", "Salário", hoje.minusDays(15), demoUser, demoStatement);
                Transaction t2 = new Transaction("Aluguel", new BigDecimal("-1200.00"), "EXPENSE", "Moradia", hoje.minusDays(12), demoUser, demoStatement);
                Transaction t3 = new Transaction("Supermercado Zaffari", new BigDecimal("-650.50"), "EXPENSE", "Alimentação", hoje.minusDays(10), demoUser, demoStatement);
                Transaction t4 = new Transaction("Uber", new BigDecimal("-35.90"), "EXPENSE", "Transporte", hoje.minusDays(5), demoUser, demoStatement);
                Transaction t5 = new Transaction("iFood - Pizza", new BigDecimal("-89.90"), "EXPENSE", "Alimentação", hoje.minusDays(2), demoUser, demoStatement);

                transactionRepository.saveAll(List.of(t1, t2, t3, t4, t5));
            }
        };
    }
}