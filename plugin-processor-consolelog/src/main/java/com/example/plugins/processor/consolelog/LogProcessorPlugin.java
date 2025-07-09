package com.example.plugins.processor.consolelog;

import com.example.plugins.ProcessorPlugin;
import com.example.plugins.processor.consolelog.dtos.LogProcessorConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LogProcessorPlugin implements ProcessorPlugin<Object, Object> {

    private LogProcessorConfig config;

    @Override
    public void configure(Map<String, Object> config) {
        this.config = new ObjectMapper().convertValue(config, LogProcessorConfig.class);
    }

    @Override
    public Object process(Object data) {
        String output;
        if (data instanceof JsonNode) {
            output = data.toString();
        } else {
            output = data != null ? data.toString() : "null";
        }
        String prefix = config != null && config.getLogPrefix() != null ? config.getLogPrefix() : "LogProcessorPlugin output:";
        System.out.println(prefix + " " + output);
        return data;
    }

    @Override
    public boolean supports(String name) {
        return "plugin-processor-consolelog".equalsIgnoreCase(name);
    }
}