package vn.iotstar.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.iotstar.entity.UserAddress;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, String> {
    
    List<UserAddress> findByUserIdOrderByIsDefaultDescCreatedAtDesc(String userId);
    
    Optional<UserAddress> findByUserIdAndIsDefaultTrue(String userId);
    
    List<UserAddress> findByUserId(String userId);
}
