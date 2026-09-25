package com.gus.finsight.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gus.finsight.dto.UserLoginRequest;
import com.gus.finsight.dto.UserRegisterRequest;
import com.gus.finsight.dto.LoginResponse;
import com.gus.finsight.entity.User;
import com.gus.finsight.repository.UserRepository;
import com.gus.finsight.service.UserService;
import com.gus.finsight.service.TokenService;
import com.gus.finsight.service.EmailService;
import com.gus.finsight.service.RateLimiterService;

import jakarta.servlet.http.HttpServletRequest;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserService userService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiterService rateLimiter;

    public AuthController(UserService userService, TokenService tokenService, UserRepository userRepository,
                           EmailService emailService, PasswordEncoder passwordEncoder, RateLimiterService rateLimiter) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request, HttpServletRequest httpRequest) {

        if (!rateLimiter.isAllowed("register:" + httpRequest.getRemoteAddr(), 5, 3600)) {
            return ResponseEntity.status(429).body(Map.of("error", "Muitas tentativas de cadastro. Tente novamente mais tarde."));
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Este e-mail já está cadastrado!"));
        }

        User savedUser = userService.registerUser(request);

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        savedUser.setVerificationCode(code);
        savedUser.setVerified(false);

        try {
            emailService.sendVerificationCode(savedUser.getEmail(), code);
        } catch (Exception e) {
            userRepository.delete(savedUser);
            return ResponseEntity.status(503).body(Map.of("error", "Não foi possível enviar o e-mail de verificação. Tente novamente em alguns instantes."));
        }

        userRepository.save(savedUser);

        return ResponseEntity.ok(Map.of("message", "Usuário criado com sucesso. Verifique seu e-mail!"));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");

        String rateLimitKey = "verify:" + (email != null ? email.toLowerCase() : "unknown");
        if (!rateLimiter.isAllowed(rateLimitKey, 8, 600)) {
            return ResponseEntity.status(429).body(Map.of("error", "Muitas tentativas. Tente novamente em alguns minutos."));
        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.isVerified()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Conta já está verificada"));
        }

        if (user.getVerificationCode() != null && user.getVerificationCode().equals(code)) {
            user.setVerified(true);
            user.setVerificationCode(null);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Conta ativada com sucesso!"));
        }

        return ResponseEntity.status(403).body(Map.of("error", "Código inválido"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {

        String rateLimitKey = "login:" + (request.getEmail() != null ? request.getEmail().toLowerCase() : "unknown");
        if (!rateLimiter.isAllowed(rateLimitKey, 8, 3600)) {
            return ResponseEntity.status(429).body(Map.of("error", "Muitas tentativas de login. Tente novamente em alguns minutos."));
        }

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty() || !passwordEncoder.matches(request.getPassword(), optionalUser.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Email ou senha incorretos!"));
        }

        User dbUser = optionalUser.get();

        if (!dbUser.isVerified()) {
            return ResponseEntity.status(403).body(Map.of("error", "Conta inativa. Verifique seu e-mail usando o código enviado."));
        }

        String token = tokenService.generateToken(dbUser);
        return ResponseEntity.ok(new LoginResponse(dbUser.getName(), token));
    }
}