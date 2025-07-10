package com.example.plugins.output.jsonfile;

import com.example.plugins.OutputPlugin;
import com.example.plugins.output.jsonfile.dtos.FileOutputConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JsonFileOutputPlugin implements OutputPlugin<JsonNode> {
    private final ObjectMapper objectMapper;
    private FileOutputConfig config;

    @Override
    public void configure(Map<String, Object> configMap) {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.config = objectMapper.convertValue(configMap, FileOutputConfig.class);
    }

    @Override
    public void write(JsonNode data) {
        try {
            objectMapper.writeValue(new File(config.getFilePath()), data);
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo JSON en archivo: " + config.getFilePath(), e);
        }
    }

    @Override
    public boolean supports(String name) {
        return "plugin-output-jsonfile".equalsIgnoreCase(name);
    }
}