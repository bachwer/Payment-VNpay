package com.banking.paymentvnpay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {
    private String code;
    private String message;
    private String paymentUrl;
}