package com.lanc.converter.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.lanc.converter.exception.IntegrationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PayloadFormatTransformer {

    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    public PayloadFormatTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.xmlMapper = new XmlMapper();
    }

    public String toJsonString(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception ex) {
            throw new IntegrationException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize JSON payload", ex);
        }
    }

    public String toXmlString(JsonNode node, String rootElement) {
        try {
            String xml = xmlMapper.writeValueAsString(node);
            return xml.replaceFirst("<ObjectNode>", "<" + rootElement + ">")
                    .replaceFirst("</ObjectNode>", "</" + rootElement + ">");
        } catch (Exception ex) {
            throw new IntegrationException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize XML payload", ex);
        }
    }

    public JsonNode xmlToJsonNode(String xmlPayload) {
        try {
            return xmlMapper.readTree(xmlPayload.getBytes());
        } catch (Exception ex) {
            throw new IntegrationException(HttpStatus.BAD_GATEWAY, "Failed to parse XML response from provider", ex);
        }
    }
}
