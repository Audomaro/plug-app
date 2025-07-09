package com.example.plugins.input.jsonfile;

import com.example.plugins.InputPlugin;
import com.example.plugins.input.jsonfile.dtos.JsonFileInputConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

@Component
public class JsonFileInputPlugin implements InputPlugin<JsonNode> {
    private JsonFileInputConfig config = new JsonFileInputConfig();

    @Override
    public void configure(Map<String, Object> configMap) {
        this.config = new ObjectMapper().convertValue(configMap, JsonFileInputConfig.class);
    }

    @Override
    public JsonNode read() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(config.getFilePath());
            return mapper.readTree(file);
        } catch (Exception e) {
            throw new RuntimeException("Error reading JSON file from " + config.getFilePath(), e);
        }
    }

    @Override
    public boolean supports(String name) {
        return "plugin-input-jsonfile".equalsIgnoreCase(name);
    }
}