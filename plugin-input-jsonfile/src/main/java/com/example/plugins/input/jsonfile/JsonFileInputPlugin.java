package com.example.plugins.input.jsonfile;

import com.example.plugins.InputPlugin;
import com.example.plugins.input.jsonfile.dtos.JsonFileInputConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JsonFileInputPlugin implements InputPlugin<JsonNode> {
    private final ObjectMapper objectMapper;
    private JsonFileInputConfig config = new JsonFileInputConfig();

    @Override
    public void configure(Map<String, Object> configMap) {
        this.config = objectMapper.convertValue(configMap, JsonFileInputConfig.class);
    }

    @Override
    public JsonNode read() {
        try {
            File file = new File(config.getFilePath());
            return objectMapper.readTree(file);
        } catch (Exception e) {
            throw new RuntimeException("Error reading JSON file from " + config.getFilePath(), e);
        }
    }

    @Override
    public boolean supports(String name) {
        return "plugin-input-jsonfile".equalsIgnoreCase(name);
    }
}