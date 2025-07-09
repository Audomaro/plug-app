package com.example.plugins.inputwebapi;

import java.util.HashMap;
import java.util.Map;

public class WebApiInputConfig {
    public String url;
    public String method = "GET";
    public Map<String, String> headers = new HashMap<>();
    public String body;
}