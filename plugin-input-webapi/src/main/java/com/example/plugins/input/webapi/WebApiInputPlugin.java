package com.example.plugins.input.webapi;

import com.example.plugins.InputPlugin;
import com.example.plugins.input.webapi.dtos.WebApiInputConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class WebApiInputPlugin implements InputPlugin<String> {

    private WebApiInputConfig config;

    @Override
    public void configure(Map<String, Object> configMap) {
        this.config = new ObjectMapper().convertValue(configMap, WebApiInputConfig.class);
    }

    @Override
    public String read() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            config.headers.forEach(headers::add);
            HttpEntity<String> entity = new HttpEntity<>(config.body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    config.url,
                    HttpMethod.valueOf(config.method.toUpperCase()),
                    entity,
                    String.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Error al consumir Web API", e);
        }
    }

    @Override
    public boolean supports(String s) {
        return "plugin-input-webapi".equalsIgnoreCase(s);
    }
}