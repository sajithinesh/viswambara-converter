package com.viswambara.converter.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.viswambara.converter.config.IntegrationProperties;
import com.viswambara.converter.exception.IntegrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpProviderGateway implements ProviderGateway {

    private static final Logger log = LoggerFactory.getLogger(HttpProviderGateway.class);

    private final IntegrationProperties properties;
    private final RestTemplate restTemplate;

    public HttpProviderGateway(IntegrationProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    @Retryable(retryFor = RestClientException.class, maxAttempts = 3, backoff = @Backoff(delay = 300, multiplier = 2))
    public JsonNode invoke(String providerName, JsonNode providerRequest) {
        IntegrationProperties.ProviderConfig providerConfig = properties.getProviders().get(providerName);
        if (providerConfig == null) {
            throw new IntegrationException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown provider config: " + providerName);
        }

        String url = providerConfig.getBaseUrl() + providerConfig.getPath();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JsonNode> entity = new HttpEntity<>(providerRequest, headers);

        try {
            log.info("Calling provider={} url={}", providerName, url);
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
            if (response.getBody() == null) {
                throw new IntegrationException(HttpStatus.BAD_GATEWAY, "Provider returned empty response");
            }
            return response.getBody();
        } catch (RestClientException ex) {
            log.error("Provider call failed for provider={}", providerName, ex);
            throw ex;
        }
    }
}
