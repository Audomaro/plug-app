package com.example.plugins.executor.dto;

import lombok.*;

import java.util.Map;

@Data()
@ToString()
@AllArgsConstructor()
@NoArgsConstructor()
public class RouteConfig {
    private String description;
    private PluginDefinition input;
    private PluginDefinition processor;
    private PluginDefinition output;
}