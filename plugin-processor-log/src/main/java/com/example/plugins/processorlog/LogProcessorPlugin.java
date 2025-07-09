package com.example.plugins.processorlog;

import com.example.plugins.ProcessorPlugin;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LogProcessorPlugin implements ProcessorPlugin {

    @Override
    public void configure(Map<String, Object> config) {
        // Este procesador no necesita configuración
    }

    @Override
    public String process(String input) {
        System.out.println(">> Procesador Log: " + input);
        return input;
    }

    @Override
    public boolean supports(String name) {
        return "logger".equalsIgnoreCase(name);
    }
}