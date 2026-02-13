package com.lanc.converter.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.lanc.converter.config.IntegrationProperties;

public interface ProviderGateway {
    JsonNode invoke(String providerName, JsonNode providerRequest, IntegrationProperties.ProviderConfig providerConfig);
}
