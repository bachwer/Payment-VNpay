package com.banking.paymentvnpay.controller;

import com.banking.paymentvnpay.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 1. API Tạo URL thanh toán
    @GetMapping("/create")
    public ResponseEntity<String> createPayment(@RequestParam String orderCode, @RequestParam long amount, HttpServletRequest request) {
        String paymentUrl = paymentService.createVnPayUrl(orderCode, amount, request);
        return ResponseEntity.ok(paymentUrl);
    }

    // 2. API ReturnURL (User được redirect về đây)
    @GetMapping("/vnpay-return")
    public ResponseEntity<String> paymentReturn(@RequestParam Map<String, String> queryParams) {
        int paymentStatus = paymentService.processReturn(queryParams);
        if (paymentStatus == 1) {
            return ResponseEntity.ok("Thanh toán thành công! Bạn có thể đóng cửa sổ này.");
        } else {
            return ResponseEntity.badRequest().body("Thanh toán thất bại hoặc bị hủy.");
        }
    }

    // 3. API IPN URL (Server VNPay gọi ngầm vào đây)
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> paymentIPN(@RequestParam Map<String, String> queryParams) {
        Map<String, String> response = paymentService.processIPN(queryParams);
        return ResponseEntity.ok(response);
    }
}