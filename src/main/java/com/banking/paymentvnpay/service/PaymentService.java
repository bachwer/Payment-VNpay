package com.banking.paymentvnpay.service;


import com.banking.paymentvnpay.config.VNPayConfig;
import com.banking.paymentvnpay.entity.Transaction;
import com.banking.paymentvnpay.repository.TransactionRepository;
import com.banking.paymentvnpay.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final VNPayConfig vnPayConfig;
    private final TransactionRepository transactionRepository;



    public String createVnPayUrl(String orderCode, long amount, HttpServletRequest request) {
        String vnpVersion = "2.1.0";
        String vnpCommand = "pay";
        String orderType = "other";
        long amountInVnpayFormat = amount * 100; // VNPay yêu cầu nhân 100
        String bankCode = "";

        String vnpIpAddr = VNPayUtil.getIpAddress(request);
        String vnpTmnCode = vnPayConfig.getTmnCode();

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", vnpVersion);
        vnpParams.put("vnp_Command", vnpCommand);
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amountInVnpayFormat));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderCode);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hàng: " + orderCode);
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", vnpIpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime localDateTime = LocalDateTime.now();
        String vnpCreateDate = localDateTime.format(formatter);
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        // Lưu đơn hàng vào DB trạng thái PENDING trước khi gọi VNPay
        Transaction tx = new Transaction();
        tx.setOrderCode(orderCode);
        tx.setAmount(BigDecimal.valueOf(amount));
        tx.setStatus("PENDING");
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);

        // Build data to hash and query string
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = vnpParams.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            // Build hash data
            hashData.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
            // Build query
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII)).append('=').append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
            if (itr.hasNext()) {
                hashData.append('&');
                query.append('&');
            }
        }

        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnpSecureHash);

        return vnPayConfig.getPayUrl() + "?" + query.toString();

    }


    public int processReturn(Map<String, String> queryParams) {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        if ("00".equals(vnp_ResponseCode)) {
            return 1; // Thành công
        }
        return 0;
    }


    public Map<String, String> processIPN(Map<String, String> queryParams) {
        Map<String, String> result = new HashMap<>();
        String vnp_SecureHash = queryParams.get("vnp_SecureHash");
        queryParams.remove("vnp_SecureHashType");
        queryParams.remove("vnp_SecureHash");

        // Check chữ ký số (Security)
        // (Trong thực tế cần viết hàm băm lại toàn bộ queryParams để so sánh với vnp_SecureHash)

        String orderCode = queryParams.get("vnp_TxnRef");
        String responseCode = queryParams.get("vnp_ResponseCode");

        Transaction tx = transactionRepository.findByOrderCode(orderCode).orElse(null);
        if (tx == null) {
            result.put("RspCode", "01");
            result.put("Message", "Order not found");
            return result;
        }

        // Idempotency: Nếu đơn đã thanh toán rồi thì không update lại nữa
        if ("SUCCESS".equals(tx.getStatus())) {
            result.put("RspCode", "02");
            result.put("Message", "Order already confirmed");
            return result;
        }

        if ("00".equals(responseCode)) {
            tx.setStatus("SUCCESS");
            tx.setResponseCode("00");
            tx.setVnpTransactionNo(queryParams.get("vnp_TransactionNo"));
            tx.setBankCode(queryParams.get("vnp_BankCode"));
            transactionRepository.save(tx);
        } else {
            tx.setStatus("FAILED");
            tx.setResponseCode(responseCode);
            transactionRepository.save(tx);
        }

        result.put("RspCode", "00");
        result.put("Message", "Confirm Success");
        return result;
    }


}
