# Viswambara Converter (Dynamic Config Integration Orchestrator)

Spring Boot backend that accepts JSON or XML input, auto-detects payload format, resolves provider/operation from runtime configuration, transforms payloads, invokes provider APIs, converts provider responses (JSON/XML) to canonical JSON, and always returns standardized JSON.

## Runtime model

- `POST /api/v1/integrations/process` accepts only `payload + content-type` from client.
- Provider routing and request/response mappings are runtime-configurable.
- Outbound provider request format is configurable per provider (`JSON` or `XML`).
- Inbound provider response is auto-detected and normalized to JSON before response mapping.

## Endpoints

- `POST /api/v1/integrations/process` - process integration request.
- `PUT /api/v1/configuration` - replace full runtime configuration (routing/providers/mappings) without restart.
- `GET /api/v1/configuration` - view currently active configuration.

## Configuration principles

No hardcoded provider or operation logic in Java code.

To onboard new provider/operation:
1. Add route rules.
2. Add provider endpoint + request format metadata.
3. Add request/response mapping templates.
4. Push via `PUT /api/v1/configuration`.

No code change is required.

## Run

```bash
mvn spring-boot:run
```
