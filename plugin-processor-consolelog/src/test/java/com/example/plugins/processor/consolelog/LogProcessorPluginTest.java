package com.example.plugins.processor.consolelog;

import com.example.plugins.processor.consolelog.dtos.LogProcessorConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogProcessorPluginTest {

    @Test
    void testGetPrefix_ReturnsConfiguredOrDefault() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);
        LogProcessorPlugin plugin = new LogProcessorPlugin(mapper);

        // Config null -> default prefix
        Field configField = LogProcessorPlugin.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(plugin, null);
        assertEquals("LogProcessorPlugin:", plugin.getPrefix());

        // Config with null prefix -> default prefix
        LogProcessorConfig config = new LogProcessorConfig();
        configField.set(plugin, config);
        assertEquals("LogProcessorPlugin:", plugin.getPrefix());

        // Config with custom prefix
        config.setLogPrefix("[MY PREFIX]");
        assertEquals("[MY PREFIX]", plugin.getPrefix());
    }

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
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        Object result = plugin.process(node);

        System.setOut(originalOut);

        String printed = out.toString().trim();
        assertTrue(printed.contains("[PREFIX]"));
        assertTrue(printed.contains("\"field\":\"value\""));
        assertSame(node, result);
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
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        Object result = plugin.process(input);

        System.setOut(originalOut);

        String printed = out.toString().trim();
        assertTrue(printed.contains(">>"));
        assertTrue(printed.contains(input));
        assertSame(input, result);
    }

    @Test
    void testProcess_PrintsNullData_WithDefaultPrefix() {
        ObjectMapper mapper = mock(ObjectMapper.class);
        LogProcessorConfig config = new LogProcessorConfig(); // logPrefix = null
        when(mapper.convertValue(any(), eq(LogProcessorConfig.class))).thenReturn(config);

        LogProcessorPlugin plugin = new LogProcessorPlugin(mapper);
        plugin.configure(Map.of()); // No prefix set

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        Object result = plugin.process(null);

        System.setOut(originalOut);

        String printed = out.toString().trim();
        assertTrue(printed.contains("LogProcessorPlugin: null"));
        assertNull(result);
    }

    @Test
    void testProcess_ConfigIsNull_UsesDefaultPrefix() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);
        LogProcessorPlugin plugin = new LogProcessorPlugin(mapper);

        // Forzar config a null con reflexión
        Field configField = LogProcessorPlugin.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(plugin, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        String input = "input data";
        Object result = plugin.process(input);

        System.setOut(originalOut);

        String printed = out.toString().trim();
        assertTrue(printed.contains("LogProcessorPlugin: input data"));
        assertSame(input, result);
    }

    @Test
    void testSupports() {
        LogProcessorPlugin plugin = new LogProcessorPlugin(new ObjectMapper());
        assertTrue(plugin.supports("plugin-processor-consolelog"));
        assertFalse(plugin.supports("otro-plugin"));
    }
}
