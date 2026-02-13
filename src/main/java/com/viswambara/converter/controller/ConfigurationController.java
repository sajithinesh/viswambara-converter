package com.viswambara.converter.controller;

import com.viswambara.converter.config.IntegrationProperties;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/configuration")
public class ConfigurationController {

    private final IntegrationProperties integrationProperties;

    public ConfigurationController(IntegrationProperties integrationProperties) {
        this.integrationProperties = integrationProperties;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void replaceConfiguration(@Valid @RequestBody IntegrationProperties request) {
        integrationProperties.setRouting(request.getRouting());
        integrationProperties.setProviders(request.getProviders());
        integrationProperties.setMappings(request.getMappings());
    }

    @GetMapping
    public Map<String, Object> currentConfiguration() {
        return Map.of(
                "routing", integrationProperties.getRouting(),
                "providers", integrationProperties.getProviders(),
                "mappings", integrationProperties.getMappings()
        );
    }
}
