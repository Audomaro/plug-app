package com.example.plugins;

import org.springframework.plugin.core.Plugin;

import java.util.Map;

public interface OutputPlugin extends Plugin<String> {
    void configure(Map<String, Object> config);
    void write(String data);
}