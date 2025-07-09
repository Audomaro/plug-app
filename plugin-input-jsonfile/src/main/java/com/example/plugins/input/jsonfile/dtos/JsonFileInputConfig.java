package com.example.plugins.input.jsonfile.dtos;

import lombok.Data;

@Data
public class JsonFileInputConfig {
    private String filePath = "input.json"; // valor por defecto
}