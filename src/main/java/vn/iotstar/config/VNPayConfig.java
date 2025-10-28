package vn.iotstar.config;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class VNPayConfig {
    
    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnp_PayUrl;
    
    @Value("${vnpay.return-url:http://localhost:8080/payment/vnpay-return}")
    private String vnp_ReturnUrl;
    
    @Value("${vnpay.tmn-code:YOUR_TMN_CODE}")
    private String vnp_TmnCode;
    
    @Value("${vnpay.hash-secret:YOUR_HASH_SECRET}")
    private String secretKey;
    
    @Value("${vnpay.version:2.1.0}")
    private String vnp_Version;
    
    @Value("${vnpay.command:pay}")
    private String vnp_Command;
    
    @Value("${vnpay.order-type:other}")
    private String orderType;
    
    /**
     * Generate HMAC SHA512 hash
     */
    public String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
    
    /**
     * Generate random number
     */
    public String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
