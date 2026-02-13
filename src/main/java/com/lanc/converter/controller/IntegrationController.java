package com.lanc.converter.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lanc.converter.service.OrchestrationService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationController {

    private final OrchestrationService orchestrationService;

    public IntegrationController(OrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping(value = "/process", produces = MediaType.APPLICATION_JSON_VALUE)
    public ObjectNode process(@RequestBody @NotBlank String body,
                              @RequestHeader(name = "Content-Type", required = false) String contentType) {
        return orchestrationService.process(body, contentType);
    }
}
