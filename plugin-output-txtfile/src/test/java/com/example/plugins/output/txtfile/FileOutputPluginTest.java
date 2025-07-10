package com.example.plugins.output.txtfile;

import com.example.plugins.output.txtfile.dtos.FileOutputConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileOutputPluginTest {

    private File tempFile;
    private ObjectMapper mockMapper;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = File.createTempFile("output", ".txt");
        tempFile.deleteOnExit();
        mockMapper = mock(ObjectMapper.class);
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testWrite_writesToFile() throws Exception {
        // Arrange
        FileOutputConfig config = new FileOutputConfig();
        config.setFilePath(tempFile.getAbsolutePath());

        when(mockMapper.convertValue(any(), eq(FileOutputConfig.class))).thenReturn(config);

        FileOutputPlugin plugin = new FileOutputPlugin(mockMapper);
        plugin.configure(Map.of("filePath", tempFile.getAbsolutePath()));

        // Act
        plugin.write("Hola mundo");

        // Assert: leer archivo para verificar contenido
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String line = reader.readLine();
            assertEquals("Hola mundo", line);
        }
    }

    @Test
    void testWrite_appendsToFile() throws Exception {
        // Arrange
        FileOutputConfig config = new FileOutputConfig();
        config.setFilePath(tempFile.getAbsolutePath());

        when(mockMapper.convertValue(any(), eq(FileOutputConfig.class))).thenReturn(config);

        FileOutputPlugin plugin = new FileOutputPlugin(mockMapper);
        plugin.configure(Map.of("filePath", tempFile.getAbsolutePath()));

        // Act
        plugin.write("Primera línea");
        plugin.write("Segunda línea");

        // Assert
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            assertEquals("Primera línea", reader.readLine());
            assertEquals("Segunda línea", reader.readLine());
        }
    }

    @Test
    void testWrite_throwsExceptionIfPathInvalid() {
        // Arrange
        FileOutputConfig config = new FileOutputConfig();
        config.setFilePath("/ruta/invalida/output.txt"); // inválido en sistemas Unix

        when(mockMapper.convertValue(any(), eq(FileOutputConfig.class))).thenReturn(config);

        FileOutputPlugin plugin = new FileOutputPlugin(mockMapper);
        plugin.configure(Map.of("filePath", config.getFilePath()));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> plugin.write("data"));
        assertTrue(ex.getMessage().contains("No se pudo escribir al archivo"));
    }

    @Test
    void testSupports() {
        FileOutputPlugin plugin = new FileOutputPlugin(new ObjectMapper());
        assertTrue(plugin.supports("plugin-output-txtfile"));
        assertFalse(plugin.supports("otro-plugin"));
    }
}