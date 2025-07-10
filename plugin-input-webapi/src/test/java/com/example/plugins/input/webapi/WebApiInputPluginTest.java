package com.example.plugins.input.webapi;

import com.example.plugins.input.webapi.dtos.WebApiInputConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;
class WebApiInputPluginTest {
    @Test
    void testRead_successfulCall() {
        // Arrange
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        RestTemplate mockRestTemplate = mock(RestTemplate.class);

        WebApiInputConfig config = new WebApiInputConfig();
        config.url = "https://example.com/api";
        config.method = "POST";
        config.body = "{\"query\":\"test\"}";
        config.headers = Map.of("Authorization", "Bearer token");

        when(mockMapper.convertValue(any(), eq(WebApiInputConfig.class)))
                .thenReturn(config);

        ResponseEntity<String> mockResponse = new ResponseEntity<>("OK_RESPONSE", HttpStatus.OK);
        when(mockRestTemplate.exchange(
                eq(config.url),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        WebApiInputPlugin plugin = new WebApiInputPlugin(mockMapper, mockRestTemplate);
        plugin.configure(Map.of("url", config.url, "method", config.method, "body", config.body, "headers", config.headers));

        // Act
        String result = plugin.read();

        // Assert
        assertEquals("OK_RESPONSE", result);
        verify(mockMapper).convertValue(any(), eq(WebApiInputConfig.class));
        verify(mockRestTemplate).exchange(eq(config.url), eq(HttpMethod.POST), any(), eq(String.class));
    }

    @Test
    void testRead_apiThrowsException() {
        // Arrange
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        RestTemplate mockRestTemplate = mock(RestTemplate.class);

        WebApiInputConfig config = new WebApiInputConfig();
        config.url = "https://bad.api";
        config.method = "GET";
        config.body = null;
        config.headers = Map.of();

        when(mockMapper.convertValue(any(), eq(WebApiInputConfig.class)))
                .thenReturn(config);

        when(mockRestTemplate.exchange(
                eq(config.url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("API unreachable"));

        WebApiInputPlugin plugin = new WebApiInputPlugin(mockMapper, mockRestTemplate);
        plugin.configure(Map.of("url", config.url, "method", config.method, "headers", config.headers));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, plugin::read);
        assertTrue(ex.getMessage().contains("Error al consumir Web API"));
    }

    @Test
    void testSupports() {
        WebApiInputPlugin plugin = new WebApiInputPlugin(new ObjectMapper(), new RestTemplate());
        assertTrue(plugin.supports("plugin-input-webapi"));
        assertFalse(plugin.supports("other"));
    }
}