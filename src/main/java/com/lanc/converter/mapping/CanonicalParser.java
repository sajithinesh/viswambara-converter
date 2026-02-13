package com.lanc.converter.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.lanc.converter.domain.CanonicalRequest;
import com.lanc.converter.domain.InputFormat;
import com.lanc.converter.exception.IntegrationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CanonicalParser {

    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    public CanonicalParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.xmlMapper = new XmlMapper();
    }

    public CanonicalRequest parse(InputFormat format, String rawBody) {
        try {
            JsonNode jsonNode = format == InputFormat.JSON
                    ? objectMapper.readTree(rawBody)
                    : xmlMapper.readTree(rawBody.getBytes());
            return new CanonicalRequest(format, jsonNode);
        } catch (Exception ex) {
            throw new IntegrationException(HttpStatus.BAD_REQUEST, "Invalid " + format + " payload", ex);
        }
    }
}
