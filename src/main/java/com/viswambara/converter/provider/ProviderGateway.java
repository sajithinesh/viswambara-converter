package com.viswambara.converter.provider;

import com.fasterxml.jackson.databind.JsonNode;

public interface ProviderGateway {
    JsonNode invoke(String providerName, JsonNode providerRequest);
}
