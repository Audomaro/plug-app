package com.example.plugins.output.txtfile;

import com.example.plugins.OutputPlugin;
import com.example.plugins.output.txtfile.dtos.FileOutputConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Component
public class FileOutputPlugin implements OutputPlugin<String> {

    private FileOutputConfig config;

    @Override
    public void configure(Map<String, Object> configMap) {
        this.config = new ObjectMapper().convertValue(configMap, FileOutputConfig.class);
    }

    @Override
    public void write(String data) {
        try (FileWriter writer = new FileWriter(config.getFilePath(), true)) {
            writer.write(data + System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo escribir al archivo: " + config.getFilePath(), e);
        }
    }

    @Override
    public boolean supports(String name) {
        return "plugin-output-txtfile".equalsIgnoreCase(name);
    }
}