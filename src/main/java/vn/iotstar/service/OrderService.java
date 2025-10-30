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
import vn.iotstar.entity.Promotion;
import vn.iotstar.entity.ShippingProvider;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;
import vn.iotstar.entity.Voucher;
import vn.iotstar.repository.OrderItemRepository;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.PromotionRepository;
import vn.iotstar.repository.ShippingProviderRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.StyleValueRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.repository.VoucherRepository;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final StyleValueRepository styleValueRepository;
    private final VoucherRepository voucherRepository;
    private final PromotionRepository promotionRepository;
    private final DiscountService discountService;
    private final ObjectMapper objectMapper;
    private final ShippingProviderRepository shippingProviderRepository;
    
    
    @Transactional
    public List<Order> createMultiStoreOrders(List<CheckoutItemDTO> items, CheckoutRequest request, 
                                               Map<String, String> promotionsByStore, String userId) {
        // Group items by store
        Map<String, List<CheckoutItemDTO>> itemsByStore = new HashMap<>();
        for (CheckoutItemDTO item : items) {
            itemsByStore.computeIfAbsent(item.getStoreId(), k -> new ArrayList<>()).add(item);
        }
        
        User user = userRepository.findById(userId).orElseThrow();
        List<Order> createdOrders = new ArrayList<>();
        
        // === LOGIC MỚI: Tính TỔNG TẤT CẢ SHOP để validate voucher ===
        BigDecimal grandTotal = items.stream()
                .map(CheckoutItemDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Validate voucher toàn sàn (check theo TỔNG TẤT CẢ SHOP)
        Voucher voucher = null;
        Map<String, BigDecimal> voucherDiscountsByStore = new HashMap<>();
        
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            voucher = discountService.validateVoucher(request.getVoucherCode(), grandTotal)
                    .orElseThrow(() -> new RuntimeException("Voucher không hợp lệ hoặc không đủ điều kiện sử dụng"));
        }
        
        // === BƯỚC 1: Tính promotion và amount sau promotion cho TẤT CẢ shop ===
        Map<String, BigDecimal> storeAmountsAfterPromotion = new HashMap<>();
        Map<String, Promotion> promotionsByStoreId = new HashMap<>();
        
        for (Map.Entry<String, List<CheckoutItemDTO>> entry : itemsByStore.entrySet()) {
            String storeId = entry.getKey();
            List<CheckoutItemDTO> storeItems = entry.getValue();
            
            // Calculate subtotal for this store
            BigDecimal subtotal = storeItems.stream()
                    .map(CheckoutItemDTO::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Validate promotion cho shop này
            Promotion promotion = null;
            String promotionId = promotionsByStore != null ? promotionsByStore.get(storeId) : null;
            
            if (promotionId != null && !promotionId.trim().isEmpty()) {
                promotion = discountService.validatePromotion(promotionId, storeId, subtotal)
                        .orElse(null);
                if (promotion != null) {
                    promotionsByStoreId.put(storeId, promotion);
                }
            }
            
            // Tính discount từ promotion
            BigDecimal promotionDiscount = BigDecimal.ZERO;
            if (promotion != null) {
                promotionDiscount = discountService.calculatePromotionDiscount(promotion, subtotal);
            }
            
            // Lưu amount sau promotion
            BigDecimal afterPromotion = subtotal.subtract(promotionDiscount);
            storeAmountsAfterPromotion.put(storeId, afterPromotion);
        }
        
        // === BƯỚC 2: Tính voucher discount cho TẤT CẢ shop (áp dụng maxDiscount đúng) ===
        if (voucher != null) {
            voucherDiscountsByStore = discountService.calculateVoucherDiscountsForAllStores(
                    voucher, grandTotal, storeAmountsAfterPromotion);
        }
        
        // === BƯỚC 3: Tạo đơn hàng cho từng shop với discount đã tính ===
        for (Map.Entry<String, List<CheckoutItemDTO>> entry : itemsByStore.entrySet()) {
            String storeId = entry.getKey();
            List<CheckoutItemDTO> storeItems = entry.getValue();
            
            Store store = storeRepository.findById(storeId).orElseThrow();
            
            // Create order
            Order order = new Order();

            // // Set shipping provider object vào Order
            // if (request.getShippingProviderId() != null && !request.getShippingProviderId().isEmpty()) {
            //     ShippingProvider provider = shippingProviderRepository.findById(request.getShippingProviderId())
            //         .orElse(null);

            //     if (provider != null) {
            //         order.setShippingProvider(provider);
            //         order.setShippingFee(provider.getShippingFee()); // nếu bạn muốn lưu phí ship
            //     }
            // }



            order.setId(UUID.randomUUID().toString());
            order.setUser(user);
            order.setStore(store);
            order.setAddress(formatAddress(request));
            order.setPhone(request.getPhone());
            order.setStatus(Order.OrderStatus.NOT_PROCESSED);
            order.setIsPaidBefore(false);
            
            LocalDateTime now = LocalDateTime.now();
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
            
            // Calculate subtotal for this store
            BigDecimal subtotal = BigDecimal.ZERO;
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
                subtotal = subtotal.add(item.getTotal());
            }
            
            order.setOrderItems(orderItems);
            
            // === ÁP DỤNG PROMOTION CỦA SHOP ===
            Promotion promotion = promotionsByStoreId.get(storeId);
            
            if (promotion != null) {
                order.setPromotion(promotion);
                promotion.setUsageCount(promotion.getUsageCount() + 1);
                promotionRepository.save(promotion);
            }
            
            // Tính giảm giá từ promotion
            BigDecimal promotionDiscount = BigDecimal.ZERO;
            if (promotion != null) {
                promotionDiscount = discountService.calculatePromotionDiscount(promotion, subtotal);
            }
            
            order.setPromotionDiscount(promotionDiscount);
            
            // Số tiền sau promotion
            BigDecimal afterPromotion = subtotal.subtract(promotionDiscount);
            
            // === ÁP DỤNG VOUCHER (đã tính sẵn với maxDiscount) ===
            BigDecimal voucherDiscount = voucherDiscountsByStore.getOrDefault(storeId, BigDecimal.ZERO);
            if (voucher != null && voucherDiscount.compareTo(BigDecimal.ZERO) > 0) {
                order.setVoucher(voucher);
            }
            
            order.setVoucherDiscount(voucherDiscount);
            order.setDiscountAmount(promotionDiscount.add(voucherDiscount));
            
            // Số tiền cuối cùng
            BigDecimal finalAmount = afterPromotion.subtract(voucherDiscount);
            
            order.setAmountFromUser(finalAmount);
            order.setAmountFromStore(finalAmount);
            order.setAmountToStore(finalAmount);
            
            // Create payment record
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setOrder(order);
            payment.setAmount(finalAmount);
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setPaymentMethod("VNPAY".equalsIgnoreCase(request.getPaymentMethod()) 
                    ? Payment.PaymentMethod.VNPAY : Payment.PaymentMethod.COD);
            
            order.setPayment(payment);
            
            
             // Set shipping provider object vào Order
            if (request.getShippingProviderId() != null && !request.getShippingProviderId().isEmpty()) {
                ShippingProvider provider = shippingProviderRepository.findById(request.getShippingProviderId())
                    .orElse(null);

                if (provider != null) {
                    order.setShippingProvider(provider);
                    order.setShippingFee(provider.getShippingFee()); // nếu bạn muốn lưu phí ship
                }
            }
            
            createdOrders.add(orderRepository.save(order));
        }
        
        // Tăng usage count của voucher (chỉ 1 lần)
        if (voucher != null) {
            voucher.setUsageCount(voucher.getUsageCount() + 1);
            voucherRepository.save(voucher);
        }
        
        return createdOrders;
    }
    
    @Transactional
public List<Order> createOrders(List<CheckoutItemDTO> items, CheckoutRequest request, String userId) {

    // Gom sản phẩm theo từng shop
    Map<String, List<CheckoutItemDTO>> itemsByStore = new HashMap<>();
    for (CheckoutItemDTO item : items) {
        itemsByStore.computeIfAbsent(item.getStoreId(), k -> new ArrayList<>()).add(item);
    }

    User user = userRepository.findById(userId).orElseThrow();
    List<Order> orders = new ArrayList<>();

    for (Map.Entry<String, List<CheckoutItemDTO>> entry : itemsByStore.entrySet()) {
        String storeId = entry.getKey();
        List<CheckoutItemDTO> storeItems = entry.getValue();

        Store store = storeRepository.findById(storeId).orElseThrow();

        Order order = new Order();
        
        //  // Set shipping provider object vào Order
        // if (request.getShippingProviderId() != null && !request.getShippingProviderId().isEmpty()) {
        //     ShippingProvider provider = shippingProviderRepository.findById(request.getShippingProviderId())
        //         .orElse(null);

        //     if (provider != null) {
        //         order.setShippingProvider(provider);
        //         order.setShippingFee(provider.getShippingFee()); // nếu bạn muốn lưu phí ship
        //     }
        // }



        order.setId(UUID.randomUUID().toString());
        order.setUser(user);
        order.setStore(store);
        order.setAddress(formatAddress(request));
        order.setPhone(request.getPhone());
        order.setStatus(Order.OrderStatus.NOT_PROCESSED);
        order.setIsPaidBefore(false);

        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        BigDecimal subtotal = BigDecimal.ZERO;
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

            if (item.getStyleValueIds() != null && !item.getStyleValueIds().isEmpty()) {
                try {
                    orderItem.setStyleValueIds(objectMapper.writeValueAsString(item.getStyleValueIds()));
                } catch (Exception e) {
                    orderItem.setStyleValueIds("[]");
                }
            }

            orderItems.add(orderItem);
            subtotal = subtotal.add(item.getTotal());

            product.setQuantity(product.getQuantity() - item.getQuantity());
            product.setSold(product.getSold() + item.getQuantity());
            productRepository.save(product);
        }

        order.setOrderItems(orderItems);
        order.setAmountFromUser(subtotal);
        order.setAmountFromStore(subtotal);
        order.setAmountToStore(subtotal);

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setOrder(order);
        payment.setAmount(subtotal);
        payment.setStatus(Payment.PaymentStatus.PENDING);

        switch (request.getPaymentMethod().toUpperCase()) {
            case "VNPAY":
                payment.setPaymentMethod(Payment.PaymentMethod.VNPAY);
                break;
            case "BANK_QR":
                payment.setPaymentMethod(Payment.PaymentMethod.BANK_QR);
                break;
            default:
                payment.setPaymentMethod(Payment.PaymentMethod.COD);
        }

        order.setPayment(payment);

 // Set shipping provider object vào Order
        if (request.getShippingProviderId() != null && !request.getShippingProviderId().isEmpty()) {
            ShippingProvider provider = shippingProviderRepository.findById(request.getShippingProviderId())
                .orElse(null);

            if (provider != null) {
                order.setShippingProvider(provider);
                order.setShippingFee(provider.getShippingFee()); // nếu bạn muốn lưu phí ship
            }
        }

        
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
        return orderRepository.findById(orderId);
    }
}