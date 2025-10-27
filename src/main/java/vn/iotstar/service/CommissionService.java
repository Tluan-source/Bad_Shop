package vn.iotstar.service;

import vn.iotstar.entity.Commission;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for managing Commissions
 * Admin uses this to set app discount/commission for stores
 */
public interface CommissionService {
    
    /**
     * Get all commissions
     */
    List<Commission> getAllCommissions();
    
    /**
     * Get commission by id
     */
    Commission getCommissionById(String id);
    
    /**
     * Create new commission
     */
    Commission createCommission(String name, BigDecimal feePercent, String description);
    
    /**
     * Update commission
     */
    Commission updateCommission(String id, String name, BigDecimal feePercent, String description);
    
    /**
     * Delete commission
     */
    void deleteCommission(String id);
    
    /**
     * Search commissions
     */
    List<Commission> searchCommissions(String keyword);
    
    /**
     * Check if commission name exists
     */
    boolean isCommissionNameExists(String name, String excludeId);
}
