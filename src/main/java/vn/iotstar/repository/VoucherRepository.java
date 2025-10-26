package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Voucher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {
    
    // Find voucher by code
    Optional<Voucher> findByCode(String code);
    
    // Find active vouchers
    List<Voucher> findByIsActiveTrue();
    
    // Find available vouchers (active and not expired)
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true AND v.endDate > :now AND v.startDate <= :now AND v.usageCount < v.quantity")
    List<Voucher> findAvailableVouchers(LocalDateTime now);
    
    // Search vouchers by code or description
    @Query("SELECT v FROM Voucher v WHERE LOWER(v.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(v.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Voucher> searchVouchers(String keyword);
    
    // Count active vouchers
    long countByIsActiveTrue();
    
    // Count expired vouchers
    @Query("SELECT COUNT(v) FROM Voucher v WHERE v.endDate < :now")
    long countExpiredVouchers(LocalDateTime now);
}
