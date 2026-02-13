package com.lanc.converter.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanc.converter.config.IntegrationProperties;
import com.lanc.converter.domain.PayloadFormat;
import com.lanc.converter.exception.IntegrationException;
import com.lanc.converter.mapping.PayloadFormatTransformer;
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

    private final RestTemplate restTemplate;
    private final PayloadFormatTransformer formatTransformer;
    private final ObjectMapper objectMapper;

    public HttpProviderGateway(RestTemplate restTemplate,
                               PayloadFormatTransformer formatTransformer,
                               ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.formatTransformer = formatTransformer;
        this.objectMapper = objectMapper;
    }

    @Override
    @Retryable(retryFor = RestClientException.class, maxAttempts = 3, backoff = @Backoff(delay = 300, multiplier = 2))
    public JsonNode invoke(String providerName, JsonNode providerRequest, IntegrationProperties.ProviderConfig providerConfig) {
        String url = providerConfig.getBaseUrl() + providerConfig.getPath();

        HttpHeaders headers = new HttpHeaders();
        String serializedRequest;
        if (providerConfig.getRequestFormat() == PayloadFormat.XML) {
            headers.setContentType(MediaType.APPLICATION_XML);
            serializedRequest = formatTransformer.toXmlString(providerRequest, providerConfig.getRequestRootElement());
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
            serializedRequest = formatTransformer.toJsonString(providerRequest);
        }

        HttpEntity<String> entity = new HttpEntity<>(serializedRequest, headers);

        try {
            log.info("Calling provider={} url={} requestFormat={} responseFormat={}", providerName, url,
                    providerConfig.getRequestFormat(), providerConfig.getResponseFormat());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getBody() == null || response.getBody().isBlank()) {
                throw new IntegrationException(HttpStatus.BAD_GATEWAY, "Provider returned empty response");
            }

            if (providerConfig.getResponseFormat() == PayloadFormat.XML) {
                return formatTransformer.xmlToJsonNode(response.getBody());
            }
            return objectMapper.readTree(response.getBody());
        } catch (IntegrationException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.error("Provider call failed for provider={}", providerName, ex);
            throw ex;
        } catch (Exception ex) {
            throw new IntegrationException(HttpStatus.BAD_GATEWAY, "Unable to parse provider response", ex);
        }
    }
}
