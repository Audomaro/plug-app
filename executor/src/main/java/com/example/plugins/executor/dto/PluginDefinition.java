package com.example.plugins.executor.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class PluginDefinition {
    private String name;
    private Map<String, Object> config;
}
