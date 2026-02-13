# LANC Converter (Config-Driven Integration Orchestrator)

Production-ready Spring Boot backend that accepts JSON/XML, resolves provider + operation by configuration, transforms payloads with template mappings, invokes provider APIs, and always returns standardized JSON.

## What this layer does

- Detects incoming payload format from `Content-Type` header or payload structure.
- Parses incoming JSON or XML into a common canonical JSON model.
- Routes to provider/operation from config only (no hardcoding).
- Transforms canonical payload using configurable templates.
- Converts provider request payload to XML when provider expects XML.
- Converts XML provider responses back into JSON and maps to standard JSON output.

## Package Structure

```text
com.lanc.converter
├── config
├── controller
├── domain
├── exception
├── mapping
├── provider
├── routing
└── service
```

## Configuration-Driven Contract

All behavior is controlled in `application.yml` + mapping templates under `src/main/resources/mappings`.

- `integration.routing`: route criteria using JSON pointers.
- `integration.providers`: endpoint, timeout, request format, response format, request XML root element.
- `integration.mappings`: request/response template per `provider:operation`.

Adding new provider/operation or changing request/response formats requires only configuration/template changes.

## Example flow

1. Receive input (`JSON` or `XML`).
2. Detect format.
3. Parse to canonical model.
4. Resolve provider+operation from config.
5. Transform canonical -> provider payload.
6. Convert provider request to XML if configured.
7. Call provider API.
8. Convert XML provider response to JSON if configured.
9. Transform provider response -> standard JSON.
10. Return JSON.

## Run

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test
```
