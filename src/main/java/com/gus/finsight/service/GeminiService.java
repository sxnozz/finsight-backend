package com.gus.finsight.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent";    
    
    @SuppressWarnings("unchecked") 
    public String getFinancialInsight(String prompt) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(15_000);    
        RestTemplate restTemplate = new RestTemplate(factory);

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<?, ?> response = restTemplate.postForObject(API_URL, entity, Map.class);
            
            if (response != null && response.containsKey("candidates")) {
                List<?> candidates = (List<?>) response.get("candidates");
                Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
                Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
                List<?> parts = (List<?>) content.get("parts");
                Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);

                return (String) firstPart.get("text");
            }
            
            return "Não foi possível extrair a resposta da IA.";
            
        } catch (HttpStatusCodeException e) {
            System.out.println(" [Gemini AI] Requisição falhou. Código HTTP: " + e.getStatusCode());
            return "Nossos servidores de Inteligência Artificial estão com alta demanda no momento. Por favor, tente novamente em alguns segundos!";
            
        } catch (Exception e) {
            System.out.println(" [Gemini AI] Erro interno: " + e.getMessage());
            return "Ocorreu um erro de conexão com a Inteligência Artificial. Tente novamente.";
        }
    }
}