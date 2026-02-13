package com.lanc.converter.mapping;

import com.lanc.converter.domain.InputFormat;
import com.lanc.converter.exception.IntegrationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FormatDetectionService {

    public InputFormat detect(String contentType, String payload) {
        if (StringUtils.hasText(contentType)) {
            String normalized = contentType.toLowerCase();
            if (normalized.contains("json")) {
                return InputFormat.JSON;
            }
            if (normalized.contains("xml") || normalized.contains("text/plain")) {
                return InputFormat.XML;
            }
        }

        String trimmed = payload == null ? "" : payload.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return InputFormat.JSON;
        }
        if (trimmed.startsWith("<")) {
            return InputFormat.XML;
        }

        throw new IntegrationException(HttpStatus.BAD_REQUEST, "Unable to detect payload format. Supported: JSON/XML");
    }
}
