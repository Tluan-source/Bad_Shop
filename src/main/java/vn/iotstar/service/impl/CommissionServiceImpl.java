package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Commission;
import vn.iotstar.repository.CommissionRepository;
import vn.iotstar.service.CommissionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of CommissionService
 */
@Service
public class CommissionServiceImpl implements CommissionService {
    
    @Autowired
    private CommissionRepository commissionRepository;
    
    @Override
    public List<Commission> getAllCommissions() {
        return commissionRepository.findAllByOrderByNameAsc();
    }
    
    @Override
    public Commission getCommissionById(String id) {
        return commissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission not found with id: " + id));
    }
    
    @Override
    @Transactional
    public Commission createCommission(String name, BigDecimal feePercent, String description) {
        // Check if name already exists
        if (commissionRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("Commission with name '" + name + "' already exists");
        }
        
        // Validate fee percent
        if (feePercent.compareTo(BigDecimal.ZERO) < 0 || feePercent.compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("Fee percent must be between 0 and 100");
        }
        
        Commission commission = new Commission();
        commission.setId(UUID.randomUUID().toString());
        commission.setName(name);
        commission.setFeePercent(feePercent);
        commission.setDescription(description);
        
        return commissionRepository.save(commission);
    }
    
    @Override
    @Transactional
    public Commission updateCommission(String id, String name, BigDecimal feePercent, String description) {
        Commission commission = getCommissionById(id);
        
        // Check if new name conflicts with existing (excluding current)
        if (!commission.getName().equalsIgnoreCase(name)) {
            if (commissionRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
                throw new RuntimeException("Commission with name '" + name + "' already exists");
            }
        }
        
        // Validate fee percent
        if (feePercent.compareTo(BigDecimal.ZERO) < 0 || feePercent.compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("Fee percent must be between 0 and 100");
        }
        
        commission.setName(name);
        commission.setFeePercent(feePercent);
        commission.setDescription(description);
        
        return commissionRepository.save(commission);
    }
    
    @Override
    @Transactional
    public void deleteCommission(String id) {
        Commission commission = getCommissionById(id);
        
        // Check if commission is being used by any store
        if (commission.getStores() != null && !commission.getStores().isEmpty()) {
            throw new RuntimeException("Cannot delete commission that is assigned to stores");
        }
        
        commissionRepository.delete(commission);
    }
    
    @Override
    public List<Commission> searchCommissions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCommissions();
        }
        return commissionRepository.searchCommissions(keyword.trim());
    }
    
    @Override
    public boolean isCommissionNameExists(String name, String excludeId) {
        if (excludeId != null) {
            return commissionRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
        }
        return commissionRepository.existsByNameIgnoreCase(name);
    }
}
