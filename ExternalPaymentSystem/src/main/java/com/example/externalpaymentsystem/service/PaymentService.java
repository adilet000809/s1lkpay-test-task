package com.example.externalpaymentsystem.service;

import com.example.externalpaymentsystem.dto.TopUpRequest;

public interface PaymentService {

    void pay(TopUpRequest topUpRequest);

}
