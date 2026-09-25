package com.gus.finsight.controller;

import com.gus.finsight.dto.TransactionResponse;
import com.gus.finsight.entity.User;
import com.gus.finsight.repository.TransactionRepository;
import com.gus.finsight.repository.UserRepository;
import com.gus.finsight.service.GeminiService;
import com.gus.finsight.service.TransactionService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Value("${finsight.demo.email:demo@finsight.com}")
    private String demoEmail;

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final TransactionRepository transactionRepository;
    private final com.gus.finsight.repository.AiReportRepository aiReportRepository;
    private final com.gus.finsight.repository.StatementRepository statementRepository;

    public TransactionController(TransactionService transactionService,
                                 UserRepository userRepository,
                                 GeminiService geminiService,
                                 TransactionRepository transactionRepository,
                                 com.gus.finsight.repository.AiReportRepository aiReportRepository,
                                 com.gus.finsight.repository.StatementRepository statementRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
        this.transactionRepository = transactionRepository;
        this.aiReportRepository = aiReportRepository;
        this.statementRepository = statementRepository;
    }

    @GetMapping("/insights")
    public ResponseEntity<Map<String, String>> getInsights(@RequestParam(required = false) Long statementId) {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User dbUser = userRepository.findById(loggedUser.getId()).orElseThrow();

        if (dbUser.getEmail().equalsIgnoreCase(demoEmail)) {
            return ResponseEntity.status(403).body(Map.of("insight", "A Inteligência Artificial é exclusiva para contas cadastradas. Crie sua conta gratuita e valide seu e-mail para testar este recurso!"));
        }

        
        int reserved = userRepository.decrementAiTokens(dbUser.getId());
        if (reserved == 0) {
            return ResponseEntity.status(403).body(Map.of("insight", "Você não tem tokens de IA suficientes."));
        }

        boolean tokenConsumed = false;
        try {
            List<com.gus.finsight.entity.Transaction> transactions;
            String fileName;

            if (statementId != null) {
                com.gus.finsight.entity.Statement statement = statementRepository.findById(statementId)
                    .orElseThrow(() -> new RuntimeException("Extrato não encontrado"));

                if (!statement.getUser().getId().equals(dbUser.getId())) {
                    return ResponseEntity.status(403).body(Map.of("error", "Acesso negado"));
                }
                transactions = transactionRepository.findByStatement(statement);
                fileName = statement.getFileName();
            } else {
                transactions = transactionRepository.findByUser(dbUser);

                List<com.gus.finsight.entity.Statement> userStatements = statementRepository.findByUser(dbUser);

                if (userStatements.size() > 1) {
                    String fileNamesConcat = userStatements.stream()
                        .map(com.gus.finsight.entity.Statement::getFileName)
                        .collect(Collectors.joining(", "));
                    fileName = "Múltiplos Extratos (" + fileNamesConcat + ")";
                } else if (userStatements.size() == 1) {
                    fileName = userStatements.get(0).getFileName();
                } else {
                    fileName = "Sem Arquivos Cadastrados";
                }
            }

            if (transactions == null || transactions.isEmpty()) {
                return ResponseEntity.ok(Map.of("insight", "Não há transações cadastradas neste extrato. Faça o upload primeiro."));
            }

            StringBuilder prompt = new StringBuilder(
                "Você é um consultor financeiro. Analise estas transações. REGRAS OBRIGATÓRIAS:\n" +
                "1. Responda em no máximo 4 tópicos curtos.\n" +
                "2. NUNCA use formatação Markdown (é estritamente proibido usar asteriscos '*' para listas ou negritos).\n" +
                "3. Comece cada tópico diretamente com um emoji, seguido do título.\n" +
                "4. Seja direto, não escreva introduções ou conclusões.\n\nTransações:\n"
            );

            int limit = Math.min(transactions.size(), 40);
            for (int i = 0; i < limit; i++) {
                com.gus.finsight.entity.Transaction t = transactions.get(i);
                prompt.append("- ").append(t.getDescription())
                      .append(" (").append(t.getCategory()).append("): ")
                      .append(t.getType().equals("INCOME") ? "+" : "")
                      .append(t.getAmount()).append("\n");
            }

            String insight = geminiService.getFinancialInsight(prompt.toString());

            if (!insight.contains("alta demanda") && !insight.contains("Erro")) {
                tokenConsumed = true;

                String finalInsight = "📄 Extrato Analisado: " + fileName + "\n\n" + insight;

                com.gus.finsight.entity.AiReport report = new com.gus.finsight.entity.AiReport();
                report.setInsightText(finalInsight);
                report.setUser(dbUser);
                aiReportRepository.save(report);

                return ResponseEntity.ok(Map.of("insight", finalInsight));
            }

            return ResponseEntity.ok(Map.of("insight", insight));

        } finally {
           
            if (!tokenConsumed) {
                userRepository.incrementAiTokens(dbUser.getId());
            }
        }
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(@RequestParam(required = false) Long statementId) {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<com.gus.finsight.entity.Transaction> transactions;

        if (statementId != null) {
            com.gus.finsight.entity.Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new RuntimeException("Extrato não encontrado"));

            if (!statement.getUser().getId().equals(loggedUser.getId())) {
                return ResponseEntity.status(403).build();
            }
            transactions = transactionRepository.findByStatement(statement);
        } else {
            transactions = transactionRepository.findByUser(loggedUser);
        }

        List<TransactionResponse> response = transactions.stream().map(t -> new TransactionResponse(
            t.getId(),
            t.getDescription(),
            t.getAmount(),
            t.getType(),
            t.getCategory(),
            t.getDate()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/insights/history")
    public ResponseEntity<List<com.gus.finsight.entity.AiReport>> getInsightHistory() {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<com.gus.finsight.entity.AiReport> history = aiReportRepository.findByUserOrderByCreatedAtDesc(loggedUser);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User dbUser = userRepository.findById(loggedUser.getId()).orElseThrow();

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("name", dbUser.getName() != null ? dbUser.getName() : "Usuário");
        response.put("email", dbUser.getEmail());

        response.put("aiTokens", dbUser.getAiTokens());
        response.put("statementTokens", dbUser.getStatementTokens());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (loggedUser.getEmail().equalsIgnoreCase(demoEmail)) {
            return ResponseEntity.status(403).body(Map.of("error", "Ação não permitida na conta de demonstração."));
        }

        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/category")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (loggedUser.getEmail().equalsIgnoreCase(demoEmail)) {
            return ResponseEntity.status(403).body(Map.of("error", "Ação não permitida na conta de demonstração."));
        }

        String newCategory = body.get("category");
        TransactionResponse updated = transactionService.updateTransactionCategory(id, newCategory);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/account")
    @Transactional
    public ResponseEntity<String> deleteAccount() {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (loggedUser.getEmail().equalsIgnoreCase(demoEmail)) {
            return ResponseEntity.status(403).body("A conta de demonstração não pode ser excluída.");
        }

        aiReportRepository.deleteByUser(loggedUser);
        transactionService.deleteByUser(loggedUser);
        statementRepository.deleteByUser(loggedUser);
        userRepository.delete(loggedUser);

        return ResponseEntity.ok("Conta excluída com sucesso");
    }
}