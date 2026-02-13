package com.lanc.converter.config;

import com.lanc.converter.domain.PayloadFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "integration")
public class IntegrationProperties {

    @Valid
    @NotEmpty
    private List<RouteRule> routing;

    @Valid
    @NotEmpty
    private Map<String, ProviderConfig> providers;

    @Valid
    @NotEmpty
    private Map<String, MappingConfig> mappings;

    public List<RouteRule> getRouting() {
        return routing;
    }

    public void setRouting(List<RouteRule> routing) {
        this.routing = routing;
    }

    public Map<String, ProviderConfig> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, ProviderConfig> providers) {
        this.providers = providers;
    }

    public Map<String, MappingConfig> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, MappingConfig> mappings) {
        this.mappings = mappings;
    }

    public static class RouteRule {
        @NotBlank
        private String provider;
        @NotBlank
        private String operation;
        @NotEmpty
        private Map<String, String> match;

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
            this.match = match;
        }
    }

    public static class ProviderConfig {
        @NotBlank
        private String baseUrl;
        @NotBlank
        private String path;
        private Duration timeout = Duration.ofSeconds(5);
        private PayloadFormat requestFormat = PayloadFormat.JSON;
        private PayloadFormat responseFormat = PayloadFormat.JSON;
        private String requestRootElement = "request";

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

        public PayloadFormat getRequestFormat() {
            return requestFormat;
        }

        public void setRequestFormat(PayloadFormat requestFormat) {
            this.requestFormat = requestFormat;
        }

        public PayloadFormat getResponseFormat() {
            return responseFormat;
        }

        public void setResponseFormat(PayloadFormat responseFormat) {
            this.responseFormat = responseFormat;
        }

        public String getRequestRootElement() {
            return requestRootElement;
        }

        public void setRequestRootElement(String requestRootElement) {
            this.requestRootElement = requestRootElement;
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
