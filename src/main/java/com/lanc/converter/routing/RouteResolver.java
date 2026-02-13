package com.lanc.converter.routing;

import com.fasterxml.jackson.databind.JsonNode;
import com.lanc.converter.config.IntegrationProperties;
import com.lanc.converter.domain.RouteDecision;
import com.lanc.converter.exception.IntegrationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RouteResolver {

    private final IntegrationProperties properties;

    public RouteResolver(IntegrationProperties properties) {
        this.properties = properties;
    }

    public RouteDecision resolve(JsonNode canonicalPayload) {
        return properties.getRouting().stream()
                .filter(rule -> rule.getMatch().entrySet().stream().allMatch(match -> {
                    JsonNode candidate = canonicalPayload.at(match.getKey());
                    return !candidate.isMissingNode() && match.getValue().equalsIgnoreCase(candidate.asText());
                }))
                .findFirst()
                .map(rule -> new RouteDecision(rule.getProvider(), rule.getOperation()))
                .orElseThrow(() -> new IntegrationException(HttpStatus.BAD_REQUEST,
                        "No route found for request. Check routing configuration."));
    }
}
