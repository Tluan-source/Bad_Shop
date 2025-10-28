package vn.iotstar.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import vn.iotstar.dto.CheckoutItemDTO;
import vn.iotstar.entity.User;
import vn.iotstar.entity.UserAddress;
import vn.iotstar.repository.UserAddressRepository;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    
    private final UserAddressRepository userAddressRepository;
    private final UserService userService;
    
    private static final String CHECKOUT_ITEMS_KEY = "checkoutItems";
    
    private HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true);
    }
    
    @SuppressWarnings("unchecked")
    public void setCheckoutItems(List<CheckoutItemDTO> items) {
        getSession().setAttribute(CHECKOUT_ITEMS_KEY, items);
    }
    
    @SuppressWarnings("unchecked")
    public List<CheckoutItemDTO> getCheckoutItems() {
        Object items = getSession().getAttribute(CHECKOUT_ITEMS_KEY);
        return items != null ? (List<CheckoutItemDTO>) items : new ArrayList<>();
    }
    
    public void clearCheckoutItems() {
        getSession().removeAttribute(CHECKOUT_ITEMS_KEY);
    }
    
    public List<UserAddress> getUserAddresses() {
        User user = getCurrentUser();
        if (user == null) {
            return new ArrayList<>();
        }
        return userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());
    }
    
    public UserAddress getDefaultAddress() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        return userAddressRepository.findByUserIdAndIsDefaultTrue(user.getId()).orElse(null);
    }
    
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(email).orElse(null);
    }
}
