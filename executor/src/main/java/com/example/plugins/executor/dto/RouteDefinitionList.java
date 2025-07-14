package com.example.plugins.executor.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RouteDefinitionList {
    private List<RouteConfig> routes;
}