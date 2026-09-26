package com.gus.finsight.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email:seu_email_aqui@gmail.com}")
    private String senderEmail;

    @Value("${finsight.email.dev-mode:false}")
    private boolean devMode;

    public void sendVerificationCode(String toEmail, String code) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(15_000);
        RestTemplate restTemplate = new RestTemplate(factory);

        Map<String, Object> body = Map.of(
            "sender", Map.of("name", "FinSight", "email", senderEmail),
            "to", List.of(Map.of("email", toEmail)),
            "subject", "FinSight - Código de Verificação",
            "textContent", "Bem-vindo ao FinSight! Seu código de verificação é: " + code
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForObject(BREVO_URL, entity, String.class);
        } catch (Exception e) {
            if (devMode) {
                logger.warn("[MODO DEV] Falha ao enviar e-mail para {}. Código: {}", toEmail, code);
                return;
            }
            logger.error("Falha ao enviar e-mail de verificação para {}", toEmail, e);
            throw new RuntimeException("Não foi possível enviar o e-mail de verificação.", e);
        }
    }
}