package com.viswambara.converter.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.viswambara.converter.domain.CanonicalRequest;
import com.viswambara.converter.domain.InputFormat;
import com.viswambara.converter.exception.IntegrationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Iterator;

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
                    : normalizeXmlNode(xmlMapper.readTree(rawBody.getBytes()));
            return new CanonicalRequest(format, jsonNode);
        } catch (Exception ex) {
            throw new IntegrationException(HttpStatus.BAD_REQUEST, "Invalid " + format + " payload", ex);
        }
    }

    private JsonNode normalizeXmlNode(JsonNode xmlNode) {
        if (!(xmlNode instanceof ObjectNode objectNode)) {
            return xmlNode;
        }

        Iterator<String> fields = objectNode.fieldNames();
        if (!fields.hasNext()) {
            return objectNode;
        }

        String first = fields.next();
        if (fields.hasNext()) {
            return objectNode;
        }

        JsonNode child = objectNode.get(first);
        return child == null ? objectNode : child;
    }
}
