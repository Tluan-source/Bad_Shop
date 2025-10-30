package vn.iotstar.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import vn.iotstar.dto.CheckoutItemDTO;
import vn.iotstar.dto.CheckoutRequest;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.OrderItem;
import vn.iotstar.entity.Payment;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;
import vn.iotstar.entity.ShippingProvider;
import vn.iotstar.entity.Shipment;
import vn.iotstar.repository.OrderItemRepository;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.StyleValueRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.repository.ShippingProviderRepository;
import vn.iotstar.repository.ShipmentRepository;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final StyleValueRepository styleValueRepository;
    private final ObjectMapper objectMapper;
    private final ShippingProviderRepository shippingProviderRepository;
    private final ShipmentRepository shipmentRepository;
    
    @Transactional
    public List<Order> createOrders(List<CheckoutItemDTO> items, CheckoutRequest request, String userId) {
        // Group items by store
        Map<String, List<CheckoutItemDTO>> itemsByStore = new HashMap<>();
        for (CheckoutItemDTO item : items) {
            itemsByStore.computeIfAbsent(item.getStoreId(), k -> new ArrayList<>()).add(item);
        }

        User user = userRepository.findById(userId).orElseThrow();
        
        // Get shipping fee and provider from selected shipping provider
        BigDecimal shippingFee = BigDecimal.ZERO;
        ShippingProvider shippingProvider = null;
        if (request.getShippingProviderId() != null && !request.getShippingProviderId().isEmpty()) {
            Optional<ShippingProvider> providerOpt = shippingProviderRepository.findById(request.getShippingProviderId());
            if (providerOpt.isPresent()) {
                shippingProvider = providerOpt.get();
                shippingFee = shippingProvider.getShippingFee();
            }
        }
        
        List<Order> orders = new ArrayList<>();

        for (Map.Entry<String, List<CheckoutItemDTO>> entry : itemsByStore.entrySet()) {
            String storeId = entry.getKey();
            List<CheckoutItemDTO> storeItems = entry.getValue();

            Store store = storeRepository.findById(storeId).orElseThrow();

            // Create order
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setUser(user);
            order.setStore(store);
            order.setAddress(formatAddress(request));
            order.setPhone(request.getPhone());
            order.setStatus(Order.OrderStatus.NOT_PROCESSED);
            order.setIsPaidBefore(false);

            // Set timestamps explicitly to ensure they are not null
            LocalDateTime now = LocalDateTime.now();
            order.setCreatedAt(now);
            order.setUpdatedAt(now);

            // Calculate totals
            BigDecimal productTotal = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (CheckoutItemDTO item : storeItems) {
                Product product = productRepository.findById(item.getProductId()).orElseThrow();

                OrderItem orderItem = new OrderItem();
                orderItem.setId(UUID.randomUUID().toString());
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPrice(item.getPrice());
                orderItem.setTotal(item.getTotal());

                // Save style value IDs as JSON
                if (item.getStyleValueIds() != null && !item.getStyleValueIds().isEmpty()) {
                    try {
                        orderItem.setStyleValueIds(objectMapper.writeValueAsString(item.getStyleValueIds()));
                    } catch (Exception e) {
                        orderItem.setStyleValueIds("[]");
                    }
                }

                orderItems.add(orderItem);
                productTotal = productTotal.add(item.getTotal());

                // REMOVED: Don't update product quantity here
                // Product quantity will be deducted when vendor confirms the order
                // This prevents double deduction bug
            }

            // Calculate total with shipping fee
            BigDecimal totalWithShipping = productTotal.add(shippingFee);

            order.setShippingFee(shippingFee); // Set shipping fee vào order
            order.setShippingProvider(shippingProvider); // Set shipping provider vào order
            order.setAmountFromUser(totalWithShipping);
            order.setAmountFromStore(totalWithShipping);
            order.setAmountToStore(totalWithShipping);
            order.setOrderItems(orderItems);

            // Create payment record with total including shipping fee
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setOrder(order);
            payment.setAmount(totalWithShipping);
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setPaymentMethod("VNPAY".equalsIgnoreCase(request.getPaymentMethod()) ? Payment.PaymentMethod.VNPAY : Payment.PaymentMethod.COD);

            order.setPayment(payment);

            // Save order (KHÔNG tạo Shipment ở đây - sẽ tạo khi shipper nhận đơn)
            orders.add(orderRepository.save(order));
        }

        return orders;
    }
    
    private String formatAddress(CheckoutRequest request) {
        StringBuilder address = new StringBuilder();
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            address.append(request.getAddress());
        }
        if (request.getWard() != null && !request.getWard().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(request.getWard());
        }
        if (request.getDistrict() != null && !request.getDistrict().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(request.getDistrict());
        }
        if (request.getProvince() != null && !request.getProvince().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(request.getProvince());
        }
        return address.toString();
    }
    
    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Lấy danh sách đơn hàng của user với thông tin style values đã được resolve
     */
    @Transactional(readOnly = true)
    public List<Order> getUserOrdersWithStyleValues(String userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        System.out.println("=== DEBUG: Found " + orders.size() + " orders for user " + userId);
        
        // Resolve style values cho mỗi order item
        for (Order order : orders) {
            System.out.println("=== DEBUG: Processing order " + order.getId() + " with " + order.getOrderItems().size() + " items");
            
            for (OrderItem item : order.getOrderItems()) {
                System.out.println("=== DEBUG: Processing item " + item.getId() + ", product: " + item.getProduct().getName());
                System.out.println("=== DEBUG: styleValueIds: " + item.getStyleValueIds());
                
                if (item.getStyleValueIds() != null && !item.getStyleValueIds().isEmpty()) {
                    try {
                        List<String> styleValueIdList = objectMapper.readValue(item.getStyleValueIds(), 
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                        
                        System.out.println("=== DEBUG: Parsed styleValueIds: " + styleValueIdList);
                        
                        List<String> styleValueNames = styleValueRepository.findAllById(styleValueIdList)
                            .stream()
                            .map(sv -> sv.getName())
                            .toList();
                        
                        System.out.println("=== DEBUG: Found style value names: " + styleValueNames);
                        
                        // Tạo attribute string để hiển thị
                        item.setStyleValueNames(styleValueNames);
                        
                    } catch (Exception e) {
                        System.out.println("=== DEBUG: Error parsing styleValueIds: " + e.getMessage());
                        // Nếu parse JSON thất bại, bỏ qua
                        item.setStyleValueNames(new ArrayList<>());
                    }
                } else {
                    System.out.println("=== DEBUG: No styleValueIds for this item");
                    item.setStyleValueNames(new ArrayList<>());
                }
            }
        }
        
        return orders;
    }
    
    public Optional<Order> getOrderById(String orderId) {
        Order order = orderRepository.findByIdWithFullDetails(orderId);
        return Optional.ofNullable(order);
    }
}