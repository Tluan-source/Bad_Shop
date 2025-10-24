package vn.iotstar.service.vendor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;
import vn.iotstar.exception.UnauthorizedAccessException;
import vn.iotstar.exception.UnauthorizedStoreAccessException;
import vn.iotstar.exception.VendorNotFoundException;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.vendor.VendorSecurityService;

/**
 * Implementation of VendorSecurityService
 * @author Vendor Module
 * @since 2025-10-24
 */
@Service
public class VendorSecurityServiceImpl implements VendorSecurityService {
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void checkStoreOwnership(String storeId, String userId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new VendorNotFoundException("Không tìm thấy cửa hàng"));
            
        if (!store.getOwner().getId().equals(userId)) {
            throw new UnauthorizedStoreAccessException(
                "Bạn không có quyền truy cập cửa hàng này"
            );
        }
    }
    
    @Override
    public void checkProductOwnership(String productId, String storeId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            
        if (!product.getStore().getId().equals(storeId)) {
            throw new UnauthorizedAccessException(
                "Sản phẩm này không thuộc cửa hàng của bạn"
            );
        }
    }
    
    @Override
    public void checkOrderOwnership(String orderId, String storeId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            
        if (!order.getStore().getId().equals(storeId)) {
            throw new UnauthorizedAccessException(
                "Đơn hàng này không thuộc cửa hàng của bạn"
            );
        }
    }
    
    @Override
    public String getCurrentVendorStoreId(String userId) {
        Store store = storeRepository.findByOwnerId(userId)
            .stream()
            .findFirst()
            .orElseThrow(() -> new VendorNotFoundException(
                "Bạn chưa có cửa hàng. Vui lòng liên hệ admin để tạo cửa hàng."
            ));
        return store.getId();
    }
    
    @Override
    public boolean isVendor(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return user.getRole() == User.UserRole.VENDOR;
    }
}
