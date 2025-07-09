package com.example.plugins.output.jsonfile;

import com.example.plugins.OutputPlugin;
import com.example.plugins.output.jsonfile.dtos.FileOutputConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component
public class JsonFileOutputPlugin implements OutputPlugin<JsonNode> {
    private FileOutputConfig config;

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void configure(Map<String, Object> configMap) {
        this.config = new ObjectMapper().convertValue(configMap, FileOutputConfig.class);
    }

    @Override
    public void write(JsonNode data) {
        try {
            mapper.writeValue(new File(config.getFilePath()), data);
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo JSON en archivo: " + config.getFilePath(), e);
        }
    }

    @Override
    public boolean supports(String name) {
        return "plugin-output-jsonfile".equalsIgnoreCase(name);
    }
}