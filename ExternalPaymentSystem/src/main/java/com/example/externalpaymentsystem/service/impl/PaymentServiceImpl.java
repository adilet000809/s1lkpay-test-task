package com.example.externalpaymentsystem.service.impl;

import com.example.externalpaymentsystem.dto.TopUpRequest;
import com.example.externalpaymentsystem.service.PaymentService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class PaymentServiceImpl implements PaymentService {

    private final RestTemplate restTemplate;
    private static final String paymentUrl = "http://localhost:8081/api/account/top-up";

    public PaymentServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void pay(TopUpRequest topUpRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TopUpRequest> paymentRequestEntity = new HttpEntity<>(topUpRequest, headers);

        restTemplate.postForObject(paymentUrl, paymentRequestEntity, Void.class);

    }

}
