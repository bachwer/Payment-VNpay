package com.banking.paymentvnpay.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderCode;

    private String vnpTransactionNo;
    private BigDecimal amount;
    private String responseCode;
    private String bankCode;
    private String status;
    private LocalDateTime createdAt;
}