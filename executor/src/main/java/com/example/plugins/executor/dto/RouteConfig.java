package com.example.plugins.executor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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