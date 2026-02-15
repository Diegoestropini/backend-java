package com.coop.financialclose;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FinancialCloseE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldRunEndToEndFlow() {
        String csv = "fecha,codigoSucursal,codigoProducto,monto,tipo\n" +
            "2026-02-14,SUC001,PROD001,100.50,INGRESO\n" +
            "2026-02-14,SUC404,PROD001,20.00,EGRESO\n";

        HttpHeaders uploadHeaders = new HttpHeaders();
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource resource = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "tx.csv";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        ResponseEntity<String> uploadResp = restTemplate.postForEntity("/api/upload", new HttpEntity<>(body, uploadHeaders), String.class);
        assertEquals(200, uploadResp.getStatusCode().value());
        assertNotNull(uploadResp.getBody());
        assertTrue(uploadResp.getBody().contains("\"processedRows\":2"));
        assertTrue(uploadResp.getBody().contains("\"acceptedRows\":1"));
        assertTrue(uploadResp.getBody().contains("\"rejectedRows\":1"));

        ResponseEntity<String> processResp = restTemplate.postForEntity("/api/process-close?date=2026-02-14", null, String.class);
        assertEquals(200, processResp.getStatusCode().value());
        assertNotNull(processResp.getBody());
        assertTrue(processResp.getBody().contains("\"rowsUpserted\":1"));

        ResponseEntity<String> summaryResp = restTemplate.getForEntity("/api/close-summary?date=2026-02-14", String.class);
        assertEquals(200, summaryResp.getStatusCode().value());
        assertNotNull(summaryResp.getBody());
        assertTrue(summaryResp.getBody().contains("\"grandIncome\":100.50"));
        assertTrue(summaryResp.getBody().contains("\"grandExpense\":0.00"));
        assertTrue(summaryResp.getBody().contains("\"grandNet\":100.50"));
        assertTrue(summaryResp.getBody().contains("\"grandTxCount\":1"));

        ResponseEntity<String> txResp = restTemplate.exchange(
            "/api/transactions?date=2026-02-14&page=0&size=10",
            HttpMethod.GET,
            null,
            String.class
        );
        assertEquals(200, txResp.getStatusCode().value());
        assertNotNull(txResp.getBody());
        assertTrue(txResp.getBody().contains("\"totalElements\":2"));
        assertTrue(txResp.getBody().contains("CONCILIADA"));
        assertTrue(txResp.getBody().contains("RECHAZADA"));
    }
}
