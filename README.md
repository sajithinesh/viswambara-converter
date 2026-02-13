# Viswambara Converter (Config-Driven Integration Orchestrator)

Production-ready Spring Boot backend that accepts JSON/XML, resolves provider and operation by configuration, transforms payloads with template mappings, invokes provider APIs, and always returns standardized JSON.

## Architecture

- `IntegrationController` receives payloads at `POST /api/v1/integrations/process`.
- `FormatDetectionService` detects JSON/XML from header or payload shape.
- `CanonicalParser` converts JSON/XML into a common `JsonNode` CDM.
- `RouteResolver` identifies `provider + operation` using configurable route rules.
- `TemplateMappingEngine` maps:
  - Canonical request -> provider request
  - provider response -> standardized response
- `HttpProviderGateway` calls provider APIs with retry and error propagation, and can send JSON or XML per provider configuration (`requestFormat`).
- `GlobalExceptionHandler` returns uniform JSON error responses.

## Package Structure

```text
com.viswambara.converter
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

All behavior is configured in `application.yml` + mapping templates under `src/main/resources/mappings`:

1. **Routing** (`integration.routing`) defines matching criteria using JSON pointers.
2. **Providers** (`integration.providers`) defines endpoint details.
3. **Mappings** (`integration.mappings`) binds `provider:operation` to request/response templates.
4. **Provider payload format** (`integration.providers.<name>.requestFormat`) controls outbound provider request serialization (`JSON`/`XML`) and optional `xmlRootElement`.

No code changes are required to add providers/operations or update mapping formats.

## Example Inputs and Outputs

### JSON input request

```json
{
  "intent": "CREATE_ORDER",
  "serviceCode": "ORD",
  "orderId": "O-1001",
  "amount": 2500,
  "customer": {
    "id": "C-100",
    "documentNo": "ABCD1234"
  }
}
```

### XML input request

```xml
<root>
  <intent>VALIDATE_CUSTOMER</intent>
  <serviceCode>CUST</serviceCode>
  <customer>
    <id>C-555</id>
    <documentNo>PAN9876</documentNo>
  </customer>
</root>
```

### Provider request (after transformation)

```json
{
  "validation": {
    "customerId": "C-555",
    "documentNo": "PAN9876"
  },
  "meta": {
    "requestType": "VALIDATE_CUSTOMER"
  }
}
```

### Standard JSON output response

```json
{
  "status": "SUCCESS",
  "transactionId": "TXN-7687",
  "message": "Customer validated",
  "provider": "providerB",
  "operation": "validateCustomer",
  "inputFormat": "XML"
}
```

## Running

```bash
mvn spring-boot:run
```

## Testing

```bash
mvn test
```
