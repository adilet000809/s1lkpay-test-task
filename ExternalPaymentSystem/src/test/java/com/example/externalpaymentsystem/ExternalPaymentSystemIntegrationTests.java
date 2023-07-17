package com.example.externalpaymentsystem;

import com.example.externalpaymentsystem.dto.TopUpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExternalPaymentSystemIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testPayment_Success() {
        TopUpRequest topUpRequest = new TopUpRequest();
        topUpRequest.setAccountNumber("KZ1");
        topUpRequest.setAmount(BigDecimal.valueOf(1000.0));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TopUpRequest> requestEntity = new HttpEntity<>(topUpRequest, headers);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                "http://localhost:8082/api/external-payment-system/pay",
                HttpMethod.POST,
                requestEntity,
                Void.class
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testPayment_Fail() {
        TopUpRequest topUpRequest = new TopUpRequest();
        topUpRequest.setAccountNumber("NO ACCOUNT NUMBER");
        topUpRequest.setAmount(BigDecimal.valueOf(1000.0));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TopUpRequest> requestEntity = new HttpEntity<>(topUpRequest, headers);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                "http://localhost:8082/api/external-payment-system/pay",
                HttpMethod.POST,
                requestEntity,
                Void.class
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

}
