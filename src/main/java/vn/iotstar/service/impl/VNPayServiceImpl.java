package vn.iotstar.service.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.iotstar.config.VNPayConfig;
import vn.iotstar.service.VNPayService;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {
    
    private final VNPayConfig vnPayConfig;
    
    @Override
    public String createPaymentUrl(BigDecimal amount, String orderInfo, String orderId, HttpServletRequest request) {
        try {
            log.info("=== CREATING VNPAY URL ===");
            log.info("Amount: {}, OrderInfo: {}, OrderId: {}", amount, orderInfo, orderId);
            
            // Convert amount to VND (no decimal)
            long amountInVND = amount.multiply(new BigDecimal("100")).longValue();
            log.info("Amount in VND: {}", amountInVND);
            
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnPayConfig.getVnp_Version());
            vnp_Params.put("vnp_Command", vnPayConfig.getVnp_Command());
            vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
            vnp_Params.put("vnp_Amount", String.valueOf(amountInVND));
            vnp_Params.put("vnp_CurrCode", "VND");
            
            vnp_Params.put("vnp_TxnRef", orderId);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", vnPayConfig.getOrderType());
            
            String locate = request.getParameter("language");
            if (locate != null && !locate.isEmpty()) {
                vnp_Params.put("vnp_Locale", locate);
            } else {
                vnp_Params.put("vnp_Locale", "vn");
            }
            
            vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
            vnp_Params.put("vnp_IpAddr", getIpAddress(request));
            
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            
            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
            
            // Build hash data
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            String queryUrl = query.toString();
            String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
            
            log.info("Final VNPay Payment URL: {}", paymentUrl);
            log.info("VNPay Payment URL created for order: {}", orderId);
            return paymentUrl;
            
        } catch (UnsupportedEncodingException e) {
            log.error("Error creating VNPay payment URL", e);
            return null;
        }
    }
    
    @Override
    public boolean validateReturnData(Map<String, String> params) {
        try {
            String vnp_SecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            params.remove("vnp_SecureHash");
            
            // Build hash data
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
            
            String signValue = vnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
            return signValue.equals(vnp_SecureHash);
            
        } catch (UnsupportedEncodingException e) {
            log.error("Error validating VNPay return data", e);
            return false;
        }
    }
    
    @Override
    public String getPaymentStatus(Map<String, String> params) {
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        
        // Response code 00 means success
        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            return "SUCCESS";
        } else {
            return "FAILED";
        }
    }
    
    /**
     * Get client IP address
     */
    private String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        return ipAdress;
    }
}
