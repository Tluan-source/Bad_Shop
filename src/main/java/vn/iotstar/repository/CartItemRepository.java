package vn.iotstar.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.iotstar.entity.CartItem;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    
    List<CartItem> findByUser(User user);
    
    List<CartItem> findByUserOrderByCreatedAtDesc(User user);
    
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    
    void deleteByUser(User user);
    
    void deleteByUserAndProduct(User user, Product product);
    
    int countByUser(User user);
    
    boolean existsByUserAndProduct(User user, Product product);
}
