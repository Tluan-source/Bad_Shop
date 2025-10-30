package vn.iotstar.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.ShippingProvider;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.ShippingProviderRepository;
import vn.iotstar.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Khởi tạo dữ liệu shipping providers và gán cho shipper
 * NOTE: Component này chạy khi start app và có thể trigger từ API /api/admin/shipping/init-data
 */
@Component
@RequiredArgsConstructor
public class ShippingDataInitializer implements CommandLineRunner {

    private final ShippingProviderRepository shippingProviderRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("=== INITIALIZING SHIPPING DATA ===");
        
        // 1. Tạo shipping providers nếu chưa có
        initializeShippingProviders();
        
        // 2. Gán shipping provider cho các shipper chưa có
        assignShippingProvidersToShippers();
        
        // 3. Cập nhật shipping provider cho orders cũ
        updateOrdersWithShippingProvider();
        
        System.out.println("=== SHIPPING DATA INITIALIZATION COMPLETED ===");
    }

    private void initializeShippingProviders() {
        long count = shippingProviderRepository.count();
        
        if (count == 0) {
            System.out.println("Creating default shipping providers...");
            
            // Tạo nhà vận chuyển mặc định
            createShippingProvider("Giao hàng nhanh", "Giao hàng trong 24h", new BigDecimal("30000"));
            createShippingProvider("Giao hàng tiết kiệm", "Giao hàng trong 3-5 ngày", new BigDecimal("20000"));
            createShippingProvider("Giao hàng hỏa tốc", "Giao hàng trong 2-4 giờ", new BigDecimal("50000"));
            
            System.out.println("✓ Created 3 default shipping providers");
        } else {
            System.out.println("✓ Shipping providers already exist: " + count + " provider(s)");
        }
    }

    private void createShippingProvider(String name, String description, BigDecimal shippingFee) {
        ShippingProvider provider = new ShippingProvider();
        provider.setId("SP-" + UUID.randomUUID().toString().substring(0, 8));
        provider.setName(name);
        provider.setDescription(description);
        provider.setShippingFee(shippingFee);
        provider.setIsActive(true);
        shippingProviderRepository.save(provider);
        System.out.println("  - Created: " + name + " (" + shippingFee + " VND)");
    }

    private void assignShippingProvidersToShippers() {
        // Lấy tất cả user có role SHIPPER
        List<User> shippers = userRepository.findByRole(User.UserRole.SHIPPER);
        
        if (shippers.isEmpty()) {
            System.out.println("✓ No shippers found to assign");
            return;
        }
        
        // Lấy shipping provider đầu tiên (mặc định)
        ShippingProvider defaultProvider = shippingProviderRepository.findAll().stream()
                .filter(ShippingProvider::getIsActive)
                .findFirst()
                .orElse(null);
        
        if (defaultProvider == null) {
            System.out.println("✗ No active shipping provider found!");
            return;
        }
        
        int assigned = 0;
        for (User shipper : shippers) {
            if (shipper.getShippingProvider() == null) {
                shipper.setShippingProvider(defaultProvider);
                userRepository.save(shipper);
                assigned++;
                System.out.println("  - Assigned " + defaultProvider.getName() + " to shipper: " + shipper.getEmail());
            }
        }
        
        System.out.println("✓ Assigned shipping provider to " + assigned + " shipper(s)");
    }

    private void updateOrdersWithShippingProvider() {
        // Lấy shipping provider đầu tiên (mặc định)
        ShippingProvider defaultProvider = shippingProviderRepository.findAll().stream()
                .filter(ShippingProvider::getIsActive)
                .findFirst()
                .orElse(null);
        
        if (defaultProvider == null) {
            System.out.println("✗ No active shipping provider found!");
            return;
        }
        
        // Lấy tất cả orders chưa có shipping provider
        List<Order> orders = orderRepository.findAll();
        int updated = 0;
        
        for (Order order : orders) {
            if (order.getShippingProvider() == null) {
                order.setShippingProvider(defaultProvider);
                
                // Nếu chưa có shipping fee, set từ provider
                if (order.getShippingFee() == null || order.getShippingFee().compareTo(BigDecimal.ZERO) == 0) {
                    order.setShippingFee(defaultProvider.getShippingFee());
                }
                
                orderRepository.save(order);
                updated++;
            }
        }
        
        System.out.println("✓ Updated " + updated + " order(s) with shipping provider");
    }
}