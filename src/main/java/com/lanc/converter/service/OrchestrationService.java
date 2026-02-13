package com.lanc.converter.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lanc.converter.config.IntegrationProperties;
import com.lanc.converter.domain.CanonicalRequest;
import com.lanc.converter.domain.InputFormat;
import com.lanc.converter.domain.RouteDecision;
import com.lanc.converter.exception.IntegrationException;
import com.lanc.converter.mapping.CanonicalParser;
import com.lanc.converter.mapping.FormatDetectionService;
import com.lanc.converter.mapping.TemplateMappingEngine;
import com.lanc.converter.provider.ProviderGateway;
import com.lanc.converter.routing.RouteResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationService.class);

    private final FormatDetectionService formatDetectionService;
    private final CanonicalParser canonicalParser;
    private final RouteResolver routeResolver;
    private final TemplateMappingEngine mappingEngine;
    private final ProviderGateway providerGateway;
    private final IntegrationProperties properties;

    public OrchestrationService(FormatDetectionService formatDetectionService,
                                CanonicalParser canonicalParser,
                                RouteResolver routeResolver,
                                TemplateMappingEngine mappingEngine,
                                ProviderGateway providerGateway,
                                IntegrationProperties properties) {
        this.formatDetectionService = formatDetectionService;
        this.canonicalParser = canonicalParser;
        this.routeResolver = routeResolver;
        this.mappingEngine = mappingEngine;
        this.providerGateway = providerGateway;
        this.properties = properties;
    }

    public ObjectNode process(String body, String contentType) {
        InputFormat inputFormat = formatDetectionService.detect(contentType, body);
        CanonicalRequest canonicalRequest = canonicalParser.parse(inputFormat, body);
        RouteDecision decision = routeResolver.resolve(canonicalRequest.payload());

        IntegrationProperties.MappingConfig mappingConfig = properties.getMappings().get(mappingKey(decision));
        if (mappingConfig == null) {
            throw new IntegrationException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Missing mapping config for " + mappingKey(decision));
        }

        IntegrationProperties.ProviderConfig providerConfig = properties.getProviders().get(decision.provider());
        if (providerConfig == null) {
            throw new IntegrationException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Missing provider config for " + decision.provider());
        }

        ObjectNode providerRequest = mappingEngine.applyTemplate(mappingConfig.getRequestTemplate(), canonicalRequest.payload());
        var providerResponse = providerGateway.invoke(decision.provider(), providerRequest, providerConfig);
        ObjectNode standardized = mappingEngine.applyTemplate(mappingConfig.getResponseTemplate(), providerResponse);

        standardized.put("provider", decision.provider());
        standardized.put("operation", decision.operation());
        standardized.put("inputFormat", inputFormat.name());
        log.info("Processed request provider={} operation={} inputFormat={}", decision.provider(), decision.operation(), inputFormat);
        return standardized;
    }

    private String mappingKey(RouteDecision decision) {
        return decision.provider() + ":" + decision.operation();
    }
}
