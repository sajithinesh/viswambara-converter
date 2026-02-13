package com.viswambara.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.okhttp3.mockwebserver.MockResponse;
import com.squareup.okhttp3.mockwebserver.MockWebServer;
import com.viswambara.converter.service.OrchestrationService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrchestrationServiceTest {

    private static MockWebServer providerA;
    private static MockWebServer providerB;

    @Autowired
    private OrchestrationService orchestrationService;


    @BeforeAll
    static void setup() throws Exception {
        providerA = new MockWebServer();
        providerA.start();
        providerB = new MockWebServer();
        providerB.start();
    }

    @AfterAll
    static void tearDown() throws Exception {
        providerA.shutdown();
        providerB.shutdown();
    }

    @DynamicPropertySource
    static void register(DynamicPropertyRegistry registry) {
        registry.add("integration.providers.providerA.baseUrl", () -> providerA.url("/").toString().replaceAll("/$", ""));
        registry.add("integration.providers.providerB.baseUrl", () -> providerB.url("/").toString().replaceAll("/$", ""));
    }

    @Test
    void shouldProcessJsonRequest() throws Exception {
        providerA.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "result": {
                            "status": "SUCCESS",
                            "providerTxnId": "P-A-9001",
                            "message": "Order accepted"
                          }
                        }
                        """));

        String jsonInput = """
                {
                  "intent": "CREATE_ORDER",
                  "serviceCode": "ORD",
                  "orderId": "O-1",
                  "amount": 1200,
                  "customer": { "id": "C-1", "documentNo": "DOC-1" }
                }
                """;

        JsonNode result = orchestrationService.process(jsonInput, "application/json");

        assertThat(result.path("status").asText()).isEqualTo("SUCCESS");
        assertThat(result.path("provider").asText()).isEqualTo("providerA");
        assertThat(result.path("inputFormat").asText()).isEqualTo("JSON");

        var recorded = providerA.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getHeader("Content-Type")).contains("application/json");
        assertThat(recorded.getBody().readUtf8()).contains("\"order\"");
    }

    @Test
    void shouldProcessXmlRequestByPayloadDetection() throws Exception {
        providerB.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/xml")
                .setBody("""
                        <response>
                            <validationStatus>SUCCESS</validationStatus>
                            <referenceId>REF-22</referenceId>
                            <reason>Customer validated</reason>
                        </response>
                        """));

        String xmlInput = """
                <root>
                    <intent>VALIDATE_CUSTOMER</intent>
                    <serviceCode>CUST</serviceCode>
                    <customer>
                        <id>C-8</id>
                        <documentNo>XY123</documentNo>
                    </customer>
                </root>
                """;

        JsonNode result = orchestrationService.process(xmlInput, null);

        assertThat(result.path("status").asText()).isEqualTo("SUCCESS");
        assertThat(result.path("provider").asText()).isEqualTo("providerB");
        assertThat(result.path("operation").asText()).isEqualTo("validateCustomer");
        assertThat(result.path("inputFormat").asText()).isEqualTo("XML");

        var recorded = providerB.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recorded).isNotNull();
        assertThat(recorded.getHeader("Content-Type")).contains("application/xml");
        assertThat(recorded.getBody().readUtf8()).contains("<validateCustomerRequest>");
    }
}
