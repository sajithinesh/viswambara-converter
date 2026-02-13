package com.viswambara.converter.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.viswambara.converter.exception.IntegrationException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

@Component
public class TemplateMappingEngine {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public TemplateMappingEngine(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    public ObjectNode applyTemplate(String templateLocation, JsonNode sourceNode) {
        try {
            JsonNode template = loadTemplate(templateLocation);
            ObjectNode output = objectMapper.createObjectNode();
            JsonNode fields = template.path("fields");
            if (!fields.isObject()) {
                throw new IllegalArgumentException("Template must contain object field: fields");
            }

            Iterator<Map.Entry<String, JsonNode>> iterator = fields.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> field = iterator.next();
                String outputPath = field.getKey();
                String sourcePath = field.getValue().asText();
                JsonNode value = sourceNode.at(sourcePath);
                setJsonPointer(output, outputPath, value.isMissingNode() ? null : value);
            }
            return output;
        } catch (IntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IntegrationException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to apply mapping template: " + templateLocation, ex);
        }
    }

    private JsonNode loadTemplate(String templateLocation) throws Exception {
        Resource resource = resourceLoader.getResource(templateLocation);
        if (!resource.exists()) {
            throw new IntegrationException(HttpStatus.INTERNAL_SERVER_ERROR, "Template not found: " + templateLocation);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readTree(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private void setJsonPointer(ObjectNode root, String pointer, JsonNode value) {
        if (!pointer.startsWith("/")) {
            throw new IllegalArgumentException("Output path must be JSON pointer: " + pointer);
        }
        String[] segments = pointer.substring(1).split("/");
        ObjectNode current = root;
        for (int i = 0; i < segments.length - 1; i++) {
            String segment = decodePointer(segments[i]);
            JsonNode existing = current.get(segment);
            if (!(existing instanceof ObjectNode)) {
                ObjectNode child = objectMapper.createObjectNode();
                current.set(segment, child);
                current = child;
            } else {
                current = (ObjectNode) existing;
            }
        }
        String leaf = decodePointer(segments[segments.length - 1]);
        current.set(leaf, value == null ? objectMapper.nullNode() : value);
    }

    private String decodePointer(String raw) {
        return raw.replace("~1", "/").replace("~0", "~");
    }
}
