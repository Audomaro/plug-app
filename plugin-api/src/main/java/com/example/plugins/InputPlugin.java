package com.example.plugins;

import org.springframework.plugin.core.Plugin;

import java.util.Map;

public interface InputPlugin<T> extends Plugin<String> {
    void configure(Map<String, Object> config);
    T read();
}