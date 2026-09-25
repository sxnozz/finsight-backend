package com.gus.finsight.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

  
    @Value("${finsight.email.dev-mode:false}")
    private boolean devMode;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("FinSight - Código de Verificação");
            message.setText("Bem-vindo ao FinSight! Seu código de verificação é: " + code + "\n\nUse este código para ativar sua conta.");
            mailSender.send(message);

        } catch (Exception e) {
            if (devMode) {
                logger.warn("[MODO DEV] Falha ao enviar e-mail para {}. Código de verificação: {}", toEmail, code);
                return;
            }

            logger.error("Falha ao enviar e-mail de verificação para {}", toEmail, e);
            throw new RuntimeException("Não foi possível enviar o e-mail de verificação.", e);
        }
    }
}