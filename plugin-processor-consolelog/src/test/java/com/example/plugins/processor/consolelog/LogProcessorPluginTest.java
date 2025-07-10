package com.example.plugins.processor.consolelog;

import com.example.plugins.processor.consolelog.LogProcessorPlugin;
import com.example.plugins.processor.consolelog.dtos.LogProcessorConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogProcessorPluginTest {

    @Test
    void testProcess_PrintsJsonNode_WithConfiguredPrefix() {
        ObjectMapper mapper = mock(ObjectMapper.class);
        LogProcessorConfig config = new LogProcessorConfig();
        config.setLogPrefix("[PREFIX]");
        when(mapper.convertValue(any(), eq(LogProcessorConfig.class))).thenReturn(config);

        LogProcessorPlugin plugin = new LogProcessorPlugin(mapper);
        plugin.configure(Map.of("logPrefix", "[PREFIX]"));

        ObjectNode node = new ObjectMapper().createObjectNode();
        node.put("field", "value");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Object result = plugin.process(node);

        String printed = out.toString().trim();
        assertTrue(printed.contains("[PREFIX]"));
        assertTrue(printed.contains("\"field\":\"value\""));
        assertSame(node, result);

        System.setOut(System.out);
    }

    @Test
    void testProcess_PrintsNonJsonNode_WithConfiguredPrefix() {
        ObjectMapper mapper = mock(ObjectMapper.class);
        LogProcessorConfig config = new LogProcessorConfig();
        config.setLogPrefix(">>");
        when(mapper.convertValue(any(), eq(LogProcessorConfig.class))).thenReturn(config);

        LogProcessorPlugin plugin = new LogProcessorPlugin(mapper);
        plugin.configure(Map.of("logPrefix", ">>"));

        String input = "test string";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Object result = plugin.process(input);

        String printed = out.toString().trim();
        assertTrue(printed.contains(">>"));
        assertTrue(printed.contains(input));
        assertSame(input, result);

        System.setOut(System.out);
    }

    @Test
    void testProcess_PrintsNullData_WithDefaultPrefix() {
        ObjectMapper mapper = mock(ObjectMapper.class);
        LogProcessorConfig config = new LogProcessorConfig(); // logPrefix = null
        when(mapper.convertValue(any(), eq(LogProcessorConfig.class))).thenReturn(config);

        LogProcessorPlugin plugin = new LogProcessorPlugin(mapper);
        plugin.configure(Map.of()); // No prefix set

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Object result = plugin.process(null);

        String printed = out.toString().trim();
        assertTrue(printed.contains("LogProcessorPlugin: null"));
        assertNull(result);

        System.setOut(System.out);
    }

    @Test
    void testProcess_ConfigIsNull_UsesDefaultPrefix() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);
        LogProcessorPlugin plugin = new LogProcessorPlugin(mapper);

        // Forzar config a null con reflexión
        var configField = LogProcessorPlugin.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(plugin, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        String input = "input data";
        Object result = plugin.process(input);

        String printed = out.toString().trim();
        assertTrue(printed.contains("LogProcessorPlugin: input data"));
        assertSame(input, result);

        System.setOut(System.out);
    }
}
