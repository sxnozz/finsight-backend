package com.gus.finsight.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.banking.BankingResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponseTransaction;
import com.webcohesion.ofx4j.domain.data.common.TransactionList;
import com.webcohesion.ofx4j.io.AggregateUnmarshaller;

import com.gus.finsight.entity.Statement;
import com.gus.finsight.entity.Transaction;
import com.gus.finsight.entity.User;
import com.gus.finsight.repository.StatementRepository;
import com.gus.finsight.repository.TransactionRepository;

@Service
public class StatementService {

    private final StatementRepository statementRepository;
    private final TransactionRepository transactionRepository;

    public StatementService(StatementRepository statementRepository, TransactionRepository transactionRepository) {
        this.statementRepository = statementRepository;
        this.transactionRepository = transactionRepository;
    }

   public void processStatementFile(MultipartFile file) {
        try {
            User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            

            String fileName = file.getOriginalFilename();

            Statement statement = new Statement(fileName, loggedUser);
            statement = statementRepository.save(statement);

            List<Transaction> transactionsToSave;

            if (fileName != null && fileName.toLowerCase().endsWith(".csv")) {
                transactionsToSave = processCsv(file, loggedUser, statement);
            } else {
                transactionsToSave = processOfx(file, loggedUser, statement);
            }

            transactionRepository.saveAll(transactionsToSave);

            System.out.println("SUCESSO! O extrato " + fileName + " processou e salvou " + transactionsToSave.size() + " transações.");

        } catch (Exception e) {

            throw new RuntimeException("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    //  LEITOR DE CSV
    private List<Transaction> processCsv(MultipartFile file, User loggedUser, Statement statement) throws Exception {
        List<Transaction> transactions = new ArrayList<>();
        
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext();
            if (header == null) throw new RuntimeException("O arquivo CSV está vazio.");

            int dateIdx = -1, amountIdx = -1, descIdx = -1;

            for (int i = 0; i < header.length; i++) {
                String col = header[i].toUpperCase().trim();
                if (col.contains("DATA") || col.contains("DATE")) dateIdx = i;
                else if (col.contains("VALOR") || col.contains("AMOUNT")) amountIdx = i;
                else if (col.contains("DESCRIÇÃO") || col.contains("DESCRICAO") || col.contains("HISTÓRICO") || col.contains("HISTORICO")) descIdx = i;
            }

            if (dateIdx == -1 || amountIdx == -1 || descIdx == -1) {
                throw new RuntimeException("Erro: O CSV precisa ter as colunas 'Data', 'Valor' e 'Descrição'.");
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String[] line;
            
            while ((line = csvReader.readNext()) != null) {
                if (line.length <= Math.max(dateIdx, Math.max(amountIdx, descIdx))) continue;

                String dateStr = line[dateIdx];
                String amountStr = line[amountIdx];
                String descStr = line[descIdx];

                LocalDate date = LocalDate.parse(dateStr, formatter);
                BigDecimal amount = new BigDecimal(amountStr);
                String type = amount.compareTo(BigDecimal.ZERO) >= 0 ? "INCOME" : "EXPENSE";

                Transaction myTransaction = new Transaction(
                    descStr, amount, type, categorizeTransaction(descStr), date, loggedUser, statement
                );

                transactions.add(myTransaction);
            }
        }
        return transactions;
    }

    // LEITOR DE OFX 
    private List<Transaction> processOfx(MultipartFile file, User loggedUser, Statement statement) throws Exception {
        List<Transaction> transactions = new ArrayList<>();
        AggregateUnmarshaller<ResponseEnvelope> unmarshaller = new AggregateUnmarshaller<>(ResponseEnvelope.class);
        ResponseEnvelope envelope = unmarshaller.unmarshal(file.getInputStream());

        BankingResponseMessageSet bankSet = (BankingResponseMessageSet) envelope.getMessageSet(com.webcohesion.ofx4j.domain.data.MessageSetType.banking);

        if (bankSet != null) {
            for (BankStatementResponseTransaction response : bankSet.getStatementResponses()) {
                TransactionList transactionList = response.getMessage().getTransactionList();

                if (transactionList != null) {
                    for (com.webcohesion.ofx4j.domain.data.common.Transaction ofxTx : transactionList.getTransactions()) {
                        String description = ofxTx.getMemo();
                        if (description == null || description.isEmpty()) {
                            description = ofxTx.getName();
                        }
                        
                        description = new String(description.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                        BigDecimal amount = BigDecimal.valueOf(ofxTx.getAmount());
                        String type = amount.compareTo(BigDecimal.ZERO) >= 0 ? "INCOME" : "EXPENSE";
                        LocalDate date = ofxTx.getDatePosted().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                        Transaction myTransaction = new Transaction(
                            description, amount, type, categorizeTransaction(description), date, loggedUser, statement
                        );
                        transactions.add(myTransaction);
                    }
                }
            }
        }
        return transactions;
    }

    // CATEGORIZADOR
    private String categorizeTransaction(String description) {
        if (description == null) return "Outros";
        String descUpper = description.toUpperCase();

        if (descUpper.contains("IFOOD") || descUpper.contains("MERCADO") || descUpper.contains("SUPERMERCADO")) return "Alimentação";
        else if (descUpper.contains("AMAZON") || descUpper.contains("NETFLIX") || descUpper.contains("SPOTIFY")) return "Assinaturas";
        else if (descUpper.contains("UBER") || descUpper.contains("99APP") || descUpper.contains("POSTO")) return "Transporte";
        else if (descUpper.contains("PAGAMENTO DE FATURA")) return "Cartão de Crédito";
        else if (descUpper.contains("RDB") || descUpper.contains("RENDIMENTO") || descUpper.contains("APLICAÇÃO")) return "Investimentos";
        else if (descUpper.contains("PIX")) return "Transferências"; 

        return "Outros";
    }
}