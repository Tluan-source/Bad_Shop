package vn.iotstar.service.vendor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.vendor.VendorOrderDTO;
import vn.iotstar.dto.vendor.VendorOrderItemDTO;
import vn.iotstar.entity.*;
import vn.iotstar.exception.InsufficientStockException;
import vn.iotstar.exception.InvalidOrderStatusException;
import vn.iotstar.exception.ResourceNotFoundException;
import vn.iotstar.repository.*;
import vn.iotstar.service.vendor.VendorOrderService;
import vn.iotstar.service.vendor.VendorSecurityService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of VendorOrderService
 * Handles order processing with business logic
 * @author Vendor Module
 * @since 2025-10-24
 */
@Service
public class VendorOrderServiceImpl implements VendorOrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private vn.iotstar.repository.StyleValueRepository styleValueRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private VendorSecurityService securityService;
    
    @Override
    @Transactional(readOnly = true)
    public List<VendorOrderDTO> getMyOrders(String storeId) {
        List<Order> orders = orderRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
        return orders.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<VendorOrderDTO> getMyOrders(String storeId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStoreId(storeId, pageable);
        List<VendorOrderDTO> dtos = orders.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, orders.getTotalElements());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<VendorOrderDTO> getMyOrdersByStatus(String storeId, Order.OrderStatus status) {
        List<Order> orders = orderRepository.findByStoreIdAndStatusOrderByCreatedAtDesc(storeId, status);
        return orders.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorOrderDTO> getMyAssignedShipments(String storeId) {
        List<Order> orders = orderRepository.findByStoreIdAndShipmentIsNotNullOrderByCreatedAtDesc(storeId);
        return orders.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<VendorOrderDTO> getMyOrdersByStatus(String storeId, Order.OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStoreIdAndStatus(storeId, status, pageable);
        List<VendorOrderDTO> dtos = orders.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, orders.getTotalElements());
    }
    
    @Override
    @Transactional(readOnly = true)
    public VendorOrderDTO getOrderDetail(String orderId, String storeId) {
        securityService.checkOrderOwnership(orderId, storeId);
        
        Order order = orderRepository.findByIdWithItems(orderId);
        if (order == null) {
            throw ResourceNotFoundException.order(orderId);
        }
        
        return convertToDTO(order);
    }
    
    @Override
    @Transactional
    public VendorOrderDTO confirmOrder(String orderId, String storeId) {
        securityService.checkOrderOwnership(orderId, storeId);
        
        Order order = orderRepository.findByIdWithItems(orderId);
        if (order == null) {
            throw ResourceNotFoundException.order(orderId);
        }
        
        // Check current status
        if (order.getStatus() != Order.OrderStatus.NOT_PROCESSED) {
            throw new InvalidOrderStatusException(
                "Chỉ có thể xác nhận đơn hàng ở trạng thái 'Chờ xử lý'"
            );
        }
        
        // Check stock for all items
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(product.getName());
            }
        }
        
        // Deduct stock and increase sold
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
            product.setSold(product.getSold() + item.getQuantity());
            productRepository.save(product);
        }
        
        // Update order status
        order.setStatus(Order.OrderStatus.PROCESSING);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }
    
    @Override
    @Transactional
    public VendorOrderDTO prepareShipment(String orderId, String storeId, String shipperId) {
        securityService.checkOrderOwnership(orderId, storeId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> ResourceNotFoundException.order(orderId));
        
        // Check current status
        if (order.getStatus() != Order.OrderStatus.PROCESSING) {
            throw new InvalidOrderStatusException(
                "Chỉ có thể giao hàng khi đơn đã được xác nhận"
            );
        }
        
        // Update to SHIPPED status
        order.setStatus(Order.OrderStatus.DELIVERING);
        order.setUpdatedAt(LocalDateTime.now());
        
        // TODO: Create Shipment entity if needed
        // Shipment shipment = new Shipment();
        // shipment.setOrder(order);
        // shipment.setShipper(shipper);
        // ...
        
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }
    
    @Override
    @Transactional
    public VendorOrderDTO cancelOrder(String orderId, String storeId, String reason) {
        securityService.checkOrderOwnership(orderId, storeId);
        
        Order order = orderRepository.findByIdWithItems(orderId);
        if (order == null) {
            throw ResourceNotFoundException.order(orderId);
        }
        
        // Can only cancel NOT_PROCESSED or PROCESSING orders
        if (order.getStatus() != Order.OrderStatus.NOT_PROCESSED && 
            order.getStatus() != Order.OrderStatus.PROCESSING) {
            throw new InvalidOrderStatusException(
                "Chỉ có thể hủy đơn hàng ở trạng thái 'Chờ xử lý' hoặc 'Đang chuẩn bị'"
            );
        }
        
        // If order was confirmed (PROCESSING), rollback stock
        if (order.getStatus() == Order.OrderStatus.PROCESSING) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() + item.getQuantity());
                product.setSold(product.getSold() - item.getQuantity());
                productRepository.save(product);
            }
        }
        
        // If paid, refund to user wallet
        if (order.getIsPaidBefore() && order.getPayment() != null && 
            order.getPayment().getStatus() == Payment.PaymentStatus.PAID) {
            User user = order.getUser();
            user.setEWallet(user.getEWallet().add(order.getAmountFromUser()));
            userRepository.save(user);
            
            // Update payment status
            order.getPayment().setStatus(Payment.PaymentStatus.REFUNDED);
        }
        
        // Update order status
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }
    
    @Override
    @Transactional
    public VendorOrderDTO handleReturn(String orderId, String storeId, String reason) {
        securityService.checkOrderOwnership(orderId, storeId);
        
        Order order = orderRepository.findByIdWithItems(orderId);
        if (order == null) {
            throw ResourceNotFoundException.order(orderId);
        }
        
        // Can only return DELIVERED orders
        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new InvalidOrderStatusException(
                "Chỉ có thể trả hàng khi đơn đã được giao"
            );
        }
        
        // Rollback stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            product.setSold(Math.max(0, product.getSold() - item.getQuantity()));
            productRepository.save(product);
        }
        
        // Refund to user wallet
        User user = order.getUser();
        user.setEWallet(user.getEWallet().add(order.getAmountFromUser()));
        userRepository.save(user);
        
        // Update payment status
        if (order.getPayment() != null) {
            order.getPayment().setStatus(Payment.PaymentStatus.REFUNDED);
        }
        
        // Update order status
        order.setStatus(Order.OrderStatus.RETURNED);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }
    
    @Override
    @Transactional
    public VendorOrderDTO markAsDelivered(String orderId, String storeId) {
        securityService.checkOrderOwnership(orderId, storeId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.order(orderId));
        
        // Check current status - can only mark SHIPPED orders as delivered
        if (order.getStatus() != Order.OrderStatus.DELIVERING) {
            throw new InvalidOrderStatusException(
                "Chỉ có thể đánh dấu đã giao khi đơn hàng đang được vận chuyển"
            );
        }
        
        // Update order status to DELIVERED
        order.setStatus(Order.OrderStatus.DELIVERED);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Mark as paid to vendor
        order.setIsPaidBefore(true);
        
        // Transfer money to vendor's wallet
        Store store = order.getStore();
        BigDecimal currentWallet = store.getEWallet() != null ? store.getEWallet() : BigDecimal.ZERO;
        BigDecimal amountToStore = order.getAmountToStore() != null ? order.getAmountToStore() : BigDecimal.ZERO;
        
        store.setEWallet(currentWallet.add(amountToStore));
        storeRepository.save(store);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        return convertToDTO(savedOrder);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countOrdersByStatus(String storeId, Order.OrderStatus status) {
        return orderRepository.countByStoreIdAndStatus(storeId, status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue(String storeId) {
        BigDecimal revenue = orderRepository.calculateTotalRevenue(storeId);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenueByDateRange(String storeId, LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = orderRepository.calculateRevenueByDateRange(storeId, startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<VendorOrderDTO> getOrdersByDateRange(String storeId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByStoreIdAndDateRange(storeId, startDate, endDate);
        return orders.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Helper method to convert Order to DTO
    private VendorOrderDTO convertToDTO(Order order) {
        VendorOrderDTO dto = new VendorOrderDTO();
        
        dto.setId(order.getId());
        dto.setStatus(order.getStatus());
        dto.setIsPaidBefore(order.getIsPaidBefore());
        dto.setAddress(order.getAddress());
        dto.setPhone(order.getPhone());
        dto.setShippingFee(order.getShippingFee());
        dto.setAmountFromUser(order.getAmountFromUser());
        dto.setAmountToStore(order.getAmountToStore());
        dto.setAmountToGd(order.getAmountToGd());
        dto.setDiscountAmount(order.getDiscountAmount());
        
        // Discount details
        dto.setPromotionDiscount(order.getPromotionDiscount());
        dto.setVoucherDiscount(order.getVoucherDiscount());
        if (order.getPromotion() != null) {
            dto.setPromotionId(order.getPromotion().getId());
            dto.setPromotionName(order.getPromotion().getName());
        }
        if (order.getVoucher() != null) {
            dto.setVoucherId(order.getVoucher().getId());
            dto.setVoucherCode(order.getVoucher().getCode());
        }
        
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        // Customer info
        User user = order.getUser();
        dto.setUserId(user.getId());
        dto.setUserFullName(user.getFullName());
        dto.setUserEmail(user.getEmail());
        dto.setUserPhone(user.getPhone());
        dto.setUserAvatar(user.getAvatar());
        
        // Store info
        dto.setStoreId(order.getStore().getId());
        dto.setStoreName(order.getStore().getName());
        
        // Payment info
        if (order.getPayment() != null) {
            dto.setPaymentMethod(order.getPayment().getPaymentMethod().name());
            dto.setPaymentStatus(order.getPayment().getStatus().name());
            dto.setTransactionId(order.getPayment().getTransactionId());
        }
        
        // Shipment info
        if (order.getShipment() != null) {
            dto.setShipmentId(order.getShipment().getId());
            dto.setShipmentStatus(order.getShipment().getStatus().name());
            dto.setDeliveryImageUrl(order.getShipment().getDeliveryImageUrl());
            dto.setAssignedAt(order.getShipment().getAssignedAt());
            dto.setDeliveredAt(order.getShipment().getDeliveredAt());
            if (order.getShipment().getShipper() != null) {
                dto.setShipperName(order.getShipment().getShipper().getFullName());
                dto.setShipperPhone(order.getShipment().getShipper().getPhone());
                dto.setShipperAvatar(order.getShipment().getShipper().getAvatar());
            }
            if (order.getShipment().getShippingProvider() != null) {
                dto.setShippingProviderName(order.getShipment().getShippingProvider().getName());
            }
        }
        
        // Confirmation info
        dto.setConfirmedByUserAt(order.getConfirmedByUserAt());
        
        // Order items
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<VendorOrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
            dto.setOrderItems(itemDTOs);
            dto.setTotalItems(itemDTOs.size());
            
            BigDecimal total = itemDTOs.stream()
                .map(VendorOrderItemDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setTotalProductAmount(total);
            // totalAmount = total products + shipping fee - discount
            BigDecimal totalAmount = total.add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO)
                    .subtract(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);
            dto.setTotalAmount(totalAmount);
        }

        // Convenience fields for templates
        dto.setOrderId(order.getId());
        if (order.getUser() != null) dto.setCustomerName(order.getUser().getFullName());
        dto.setOrderDate(order.getCreatedAt());
        
        return dto;
    }
    
    private VendorOrderItemDTO convertItemToDTO(OrderItem item) {
        VendorOrderItemDTO dto = new VendorOrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        
        // Get first image
        String images = item.getProduct().getListImages();
        if (images != null && !images.equals("[]")) {
            try {
                String firstImage = images.substring(2, images.indexOf("\"", 2));
                dto.setProductImage(firstImage);
            } catch (Exception e) {
                dto.setProductImage("");
            }
        }
        
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setTotal(item.getTotal());

        // Parse styleValueIds JSON and resolve names
        if (item.getStyleValueIds() != null && !item.getStyleValueIds().isEmpty()) {
            try {
                java.util.List<String> ids = objectMapper.readValue(item.getStyleValueIds(), new TypeReference<java.util.List<String>>(){});
                if (ids != null && !ids.isEmpty()) {
                    java.util.List<String> names = styleValueRepository.findAllById(ids).stream()
                            .map(sv -> sv.getStyle().getName() + ": " + sv.getName())
                            .toList();
                    dto.setStyleValueNames(names);
                }
            } catch (Exception e) {
                // ignore parse errors
                dto.setStyleValueNames(java.util.List.of());
            }
        } else {
            dto.setStyleValueNames(java.util.List.of());
        }
        
        return dto;
    }
}