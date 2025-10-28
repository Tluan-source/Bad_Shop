package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.iotstar.entity.ShippingProvider;

import java.util.List;
import java.util.Optional;

public interface ShippingProviderRepository extends JpaRepository<ShippingProvider, String> {
    @Query("SELECT s FROM ShippingProvider s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<ShippingProvider> searchByName(@Param("q") String q);

    Optional<ShippingProvider> findByName(String name);
}
