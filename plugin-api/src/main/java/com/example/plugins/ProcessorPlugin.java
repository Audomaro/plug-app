package com.example.plugins;

import org.springframework.plugin.core.Plugin;

import java.util.Map;

public interface ProcessorPlugin<I, O> extends Plugin<String> {
    default void configure(Map<String, Object> config) {

    }

    O process(I input);
}
