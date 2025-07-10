package com.example.plugins.processor.consolelog;

import com.example.plugins.ProcessorPlugin;
import com.example.plugins.processor.consolelog.dtos.LogProcessorConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LogProcessorPlugin implements ProcessorPlugin<Object, Object> {
    private final ObjectMapper objectMapper;
    private LogProcessorConfig config;

    @Override
    public void configure(Map<String, Object> config) {
        this.config = objectMapper.convertValue(config, LogProcessorConfig.class);
    }

    @Override
    public Object process(Object data) {
        // Obtener output según tipo y valor de data
        String output = (data instanceof JsonNode)
                ? data.toString()
                : (data != null ? data.toString() : "null");

        // Obtener prefijo seguro
        String prefix = (config != null && config.getLogPrefix() != null)
                ? config.getLogPrefix()
                : "LogProcessorPlugin:";

        System.out.println(prefix + " " + output);
        return data;
    }

    @Override
    public boolean supports(String name) {
        return "plugin-processor-consolelog".equalsIgnoreCase(name);
    }
}