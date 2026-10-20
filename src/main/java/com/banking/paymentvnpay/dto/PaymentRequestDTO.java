package com.banking.paymentvnpay.dto;


import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {
    private String orderCode;
    private BigDecimal amount;
    private String orderInfo;
}