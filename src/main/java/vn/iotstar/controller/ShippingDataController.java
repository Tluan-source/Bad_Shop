package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.iotstar.config.ShippingDataInitializer;

import java.util.HashMap;
import java.util.Map;

/**
 * API để trigger khởi tạo dữ liệu shipping
 */
@RestController
@RequestMapping("/api/admin/shipping")
@RequiredArgsConstructor
public class ShippingDataController {

    private final ShippingDataInitializer shippingDataInitializer;

    /**
     * Endpoint để re-initialize shipping data
     * Chỉ admin mới có thể gọi
     */
    @PostMapping("/init-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> initShippingData() {
        try {
            shippingDataInitializer.run();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Shipping data initialized successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
