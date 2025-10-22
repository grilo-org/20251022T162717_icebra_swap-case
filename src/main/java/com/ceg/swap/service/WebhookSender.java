package com.ceg.swap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookSender {

    private static final Logger log = LoggerFactory.getLogger(WebhookSender.class);
    private final RestTemplate restTemplate = new RestTemplate();

    public void send(String url, Object payload) {
        try {
            restTemplate.postForEntity(url, payload, String.class);
            log.info("Webhook enviado com sucesso para {}", url);
        } catch (Exception e) {
            log.error("Falha ao enviar webhook para {}: {}", url, e.getMessage());
            throw new RuntimeException("Erro ao enviar webhook: " + e.getMessage(), e);
        }
    }
}
