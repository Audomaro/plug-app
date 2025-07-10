package com.example.plugins.input.jsonfile;

import com.example.plugins.input.jsonfile.dtos.JsonFileInputConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JsonFileInputPluginTest {
    @Test
    void testConfigureAndRead_Success() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        JsonNode mockJsonNode = mock(JsonNode.class);

        JsonFileInputConfig mockConfig = new JsonFileInputConfig();
        mockConfig.setFilePath("test.json");

        when(mockMapper.convertValue(any(), eq(JsonFileInputConfig.class))).thenReturn(mockConfig);
        when(mockMapper.readTree(any(File.class))).thenReturn(mockJsonNode);

        JsonFileInputPlugin plugin = new JsonFileInputPlugin(mockMapper);
        plugin.configure(Map.of("filePath", "test.json"));
        JsonNode result = plugin.read();

        assertEquals(mockJsonNode, result);

        verify(mockMapper).convertValue(any(), eq(JsonFileInputConfig.class));
        verify(mockMapper).readTree(new File("test.json"));
    }

    @Test
    void testRead_ThrowsException() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        JsonFileInputConfig config = new JsonFileInputConfig();
        config.setFilePath("bad.json");

        when(mockMapper.convertValue(any(), eq(JsonFileInputConfig.class))).thenReturn(config);
        when(mockMapper.readTree(any(File.class))).thenThrow(new RuntimeException("File not found"));

        JsonFileInputPlugin plugin = new JsonFileInputPlugin(mockMapper);
        plugin.configure(Map.of("filePath", "bad.json"));

        RuntimeException ex = assertThrows(RuntimeException.class, plugin::read);
        assertTrue(ex.getMessage().contains("Error reading JSON file"));
    }

    @Test
    void testSupports() {
        JsonFileInputPlugin plugin = new JsonFileInputPlugin(new ObjectMapper());
        assertTrue(plugin.supports("plugin-input-jsonfile"));
        assertTrue(plugin.supports("PLUGIN-INPUT-JSONFILE"));
        assertFalse(plugin.supports("other-plugin"));
    }
}