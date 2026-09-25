package com.gus.finsight.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.gus.finsight.dto.TransactionResponse;
import com.gus.finsight.entity.Transaction;
import com.gus.finsight.entity.User;
import com.gus.finsight.repository.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void deleteTransaction(Long id) {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));
                
        if (!transaction.getUser().getId().equals(loggedUser.getId())) {
            throw new RuntimeException("Acesso negado: Esta transação pertence a outro usuário");
        }
        
        transactionRepository.delete(transaction);
    }

    public TransactionResponse updateTransactionCategory(Long id, String newCategory) {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));
                
        if (!transaction.getUser().getId().equals(loggedUser.getId())) {
            throw new RuntimeException("Acesso negado");
        }
        
        transaction.setCategory(newCategory);
        transaction = transactionRepository.save(transaction);
        
        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getDate()
        );
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteByUser(com.gus.finsight.entity.User user) {
        transactionRepository.deleteByUser(user);
    }
}