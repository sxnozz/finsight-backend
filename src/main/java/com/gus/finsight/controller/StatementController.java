package com.gus.finsight.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.gus.finsight.entity.Statement;
import com.gus.finsight.entity.User;
import com.gus.finsight.repository.StatementRepository;
import com.gus.finsight.repository.UserRepository;
import com.gus.finsight.service.StatementService;

@RestController
@RequestMapping("/api/statements")
public class StatementController {

    private static final Logger logger = LoggerFactory.getLogger(StatementController.class);

    @Value("${finsight.demo.email:demo@finsight.com}")
    private String demoEmail;

    private final StatementService statementService;
    private final UserRepository userRepository;
    private final StatementRepository statementRepository;

    public StatementController(StatementService statementService, UserRepository userRepository, StatementRepository statementRepository) {
        this.statementService = statementService;
        this.userRepository = userRepository;
        this.statementRepository = statementRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadStatement(@RequestParam("file") MultipartFile file) {

        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (loggedUser.getEmail().equalsIgnoreCase(demoEmail)) {
            return ResponseEntity.status(403).body(Map.of("error", "Upload bloqueado na conta demo."));
        }

        User dbUser = userRepository.findById(loggedUser.getId()).orElseThrow();

        
        int reserved = userRepository.decrementStatementTokens(dbUser.getId());
        if (reserved == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Limite de 3 extratos atingido."));
        }

        try {
            statementService.processStatementFile(file);
            return ResponseEntity.ok(Map.of("message", "Arquivo processado com sucesso!"));

        } catch (Exception e) {
            userRepository.incrementStatementTokens(dbUser.getId());
            logger.warn("Falha ao processar extrato enviado por {}: {}", dbUser.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Não foi possível processar o arquivo. Verifique se o formato e as colunas estão corretos."));
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllStatements() {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Statement> statements = statementRepository.findByUser(loggedUser);

        List<Map<String, Object>> response = statements.stream()
            .map(s -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", s.getId());
                map.put("fileName", s.getFileName());
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}