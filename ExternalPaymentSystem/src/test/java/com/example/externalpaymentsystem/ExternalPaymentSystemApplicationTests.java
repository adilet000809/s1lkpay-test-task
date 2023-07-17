package com.example.externalpaymentsystem;

import com.example.externalpaymentsystem.controller.PaymentController;
import com.example.externalpaymentsystem.dto.TopUpRequest;
import com.example.externalpaymentsystem.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class ExternalPaymentSystemApplicationTests {

    @Mock
    private PaymentService paymentService;

    private PaymentController paymentController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        paymentController = new PaymentController(paymentService);
    }

    @Test
    public void testTopUpBalance_Success() {
        String accountNumber = "KZ1";
        BigDecimal amount = BigDecimal.valueOf(1000.0);

        TopUpRequest topUpRequest = new TopUpRequest(accountNumber, amount);

        ResponseEntity<String> response = paymentController.pay(topUpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paymentService, times(1)).pay(topUpRequest);
    }

    @Test
    public void testTopUpBalance_Exception() {
        String accountNumber = "KZ1";
        BigDecimal amount = BigDecimal.valueOf(1000.0);

        TopUpRequest topUpRequest = new TopUpRequest(accountNumber, amount);

        doThrow(new RuntimeException("Payment failed"))
                .when(paymentService).pay(topUpRequest);

        ResponseEntity<String> response = paymentController.pay(topUpRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(paymentService, times(1)).pay(topUpRequest);
    }

}
