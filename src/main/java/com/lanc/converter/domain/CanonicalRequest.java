package com.lanc.converter.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record CanonicalRequest(InputFormat format, JsonNode payload) {
}
