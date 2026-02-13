package com.viswambara.converter.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "integration")
public class IntegrationProperties {

    @Valid
    private List<RouteRule> routing = new ArrayList<>();

    @Valid
    private Map<String, ProviderConfig> providers = new LinkedHashMap<>();

    @Valid
    private Map<String, MappingConfig> mappings = new LinkedHashMap<>();

    public List<RouteRule> getRouting() {
        return routing;
    }

    public void setRouting(List<RouteRule> routing) {
        this.routing = routing == null ? new ArrayList<>() : new ArrayList<>(routing);
    }

    public Map<String, ProviderConfig> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, ProviderConfig> providers) {
        this.providers = providers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(providers);
    }

    public Map<String, MappingConfig> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, MappingConfig> mappings) {
        this.mappings = mappings == null ? new LinkedHashMap<>() : new LinkedHashMap<>(mappings);
    }

    public static class RouteRule {
        @NotBlank
        private String provider;
        @NotBlank
        private String operation;
        private Map<String, String> match = new LinkedHashMap<>();

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public Map<String, String> getMatch() {
            return match;
        }

        public void setMatch(Map<String, String> match) {
            this.match = match == null ? new LinkedHashMap<>() : new LinkedHashMap<>(match);
        }
    }

    public enum ProviderPayloadFormat {
        JSON,
        XML
    }

    public static class ProviderConfig {
        @NotBlank
        private String baseUrl;
        @NotBlank
        private String path;
        private Duration timeout = Duration.ofSeconds(5);
        private ProviderPayloadFormat requestFormat = ProviderPayloadFormat.JSON;
        private String xmlRootElement = "request";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public ProviderPayloadFormat getRequestFormat() {
            return requestFormat;
        }

        public void setRequestFormat(ProviderPayloadFormat requestFormat) {
            this.requestFormat = requestFormat;
        }

        public String getXmlRootElement() {
            return xmlRootElement;
        }

        public void setXmlRootElement(String xmlRootElement) {
            this.xmlRootElement = xmlRootElement;
        }
    }

    public static class MappingConfig {
        @NotBlank
        private String requestTemplate;
        @NotBlank
        private String responseTemplate;

        public String getRequestTemplate() {
            return requestTemplate;
        }

        public void setRequestTemplate(String requestTemplate) {
            this.requestTemplate = requestTemplate;
        }

        public String getResponseTemplate() {
            return responseTemplate;
        }

        public void setResponseTemplate(String responseTemplate) {
            this.responseTemplate = responseTemplate;
        }
    }
}
