package com.example.plugins;

import org.springframework.plugin.core.Plugin;

import java.util.Map;

public interface InputPlugin extends Plugin<String> {
    void configure(Map<String, Object> config);
    String read();
}