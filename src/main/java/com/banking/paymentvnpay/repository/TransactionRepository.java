package com.banking.paymentvnpay.repository;

import com.banking.paymentvnpay.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByOrderCode(String orderCode);
}
