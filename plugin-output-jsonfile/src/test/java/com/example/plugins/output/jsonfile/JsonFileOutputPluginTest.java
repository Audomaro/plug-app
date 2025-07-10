package com.example.plugins.output.jsonfile;

import com.example.plugins.output.jsonfile.dtos.FileOutputConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonFileOutputPluginTest {

    @Test
    void testConfigure_setsConfigCorrectly() {
        // Arrange
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        FileOutputConfig config = new FileOutputConfig();
        config.setFilePath("output.json");

        when(mockMapper.convertValue(any(), eq(FileOutputConfig.class))).thenReturn(config);

        JsonFileOutputPlugin plugin = new JsonFileOutputPlugin(mockMapper);

        // Act
        plugin.configure(Map.of("filePath", "output.json"));

        // Assert
        verify(mockMapper).enable(SerializationFeature.INDENT_OUTPUT);
        verify(mockMapper).convertValue(any(), eq(FileOutputConfig.class));
    }

    @Test
    void testWrite_writesJsonSuccessfully() throws Exception {
        // Arrange
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        JsonNode mockData = mock(JsonNode.class);
        FileOutputConfig config = new FileOutputConfig();
        config.setFilePath("result.json");

        when(mockMapper.convertValue(any(), eq(FileOutputConfig.class))).thenReturn(config);

        JsonFileOutputPlugin plugin = new JsonFileOutputPlugin(mockMapper);
        plugin.configure(Map.of("filePath", config.getFilePath()));

        // Act
        plugin.write(mockData);

        // Assert
        verify(mockMapper).writeValue(new File("result.json"), mockData);
    }

    @Test
    void testWrite_throwsRuntimeExceptionOnFailure() throws Exception {
        // Arrange
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        JsonNode mockData = mock(JsonNode.class);
        FileOutputConfig config = new FileOutputConfig();
        config.setFilePath("fail.json");

        when(mockMapper.convertValue(any(), eq(FileOutputConfig.class))).thenReturn(config);
        doThrow(new IOException("Disk full")).when(mockMapper).writeValue(any(File.class), eq(mockData));

        JsonFileOutputPlugin plugin = new JsonFileOutputPlugin(mockMapper);
        plugin.configure(Map.of("filePath", config.getFilePath()));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> plugin.write(mockData));
        assertTrue(ex.getMessage().contains("Error escribiendo JSON en archivo"));
    }

    @Test
    void testSupports() {
        JsonFileOutputPlugin plugin = new JsonFileOutputPlugin(new ObjectMapper());
        assertTrue(plugin.supports("plugin-output-jsonfile"));
        assertFalse(plugin.supports("otro-plugin"));
    }
}