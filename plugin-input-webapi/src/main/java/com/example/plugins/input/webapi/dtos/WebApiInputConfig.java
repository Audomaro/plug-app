package com.example.plugins.input.webapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Data()
@ToString()
@AllArgsConstructor()
@NoArgsConstructor()
public class WebApiInputConfig {
    public String url;
    public String method = "GET";
    public Map<String, String> headers = new HashMap<>();
    public String body;
}