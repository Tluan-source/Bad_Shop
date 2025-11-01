package vn.iotstar.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.iotstar.entity.Shipment;
import vn.iotstar.entity.Shipment.ShipmentStatus;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String> {

    /* ===========================================================
       🔹 1. Truy vấn cơ bản theo trạng thái và shipper
       =========================================================== */


    // 🟡 Danh sách tất cả shipment theo trạng thái (không lọc shipper)
    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);

    // 🟡 Shipment theo trạng thái và không có shipper (đơn đang chờ nhận)
    Page<Shipment> findByStatusAndShipperIsNull(ShipmentStatus status, Pageable pageable);

    // 🔵 Shipment của 1 shipper theo trạng thái
    Page<Shipment> findByShipper_IdAndStatus(String shipperId, ShipmentStatus status, Pageable pageable);

    Shipment findByOrderId(String orderId);

    // 🔵 Shipment của 1 shipper (dạng list, dùng cho thống kê hoặc không phân trang)
    List<Shipment> findByShipper_IdAndStatus(String shipperId, ShipmentStatus status);

    // 📊 Đếm số lượng đơn theo trạng thái (cho cards tổng quan)
    long countByStatusAndShipperIsNull(ShipmentStatus status);
    long countByShipper_IdAndStatus(String shipperId, ShipmentStatus status);


    /* ===========================================================
       🔹 2. Lọc theo thời gian (Từ ngày – Đến ngày)
       =========================================================== */

    // 🟡 Đơn chờ nhận (chưa có shipper) trong khoảng thời gian
    @Query("""
            SELECT s FROM Shipment s
            WHERE s.status = :status 
            AND s.shipper IS NULL
            AND s.createdAt BETWEEN :start AND :end
            """)
    Page<Shipment> findByStatusAndShipperIsNullAndCreatedAtBetween(
            @Param("status") ShipmentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    // 🔵 Đơn của 1 shipper trong khoảng thời gian
    @Query("""
            SELECT s FROM Shipment s
            WHERE s.shipper.id = :shipperId 
            AND s.status = :status
            AND s.createdAt BETWEEN :start AND :end
            """)
    Page<Shipment> findByShipper_IdAndStatusAndCreatedAtBetween(
            @Param("shipperId") String shipperId,
            @Param("status") ShipmentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);


    /* ===========================================================
       🔹 3. Tìm kiếm theo keyword (Tên KH / Địa chỉ)
       =========================================================== */

    // 🟡 Đơn chờ nhận (chưa có shipper) tìm theo keyword
    @Query("""
            SELECT s FROM Shipment s
            WHERE s.status = :status
            AND s.shipper IS NULL
            AND (
                LOWER(s.order.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.order.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.order.id) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    Page<Shipment> searchByStatusAndKeywordAndShipperIsNull(
            @Param("status") ShipmentStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    // 🔵 Đơn của 1 shipper tìm theo keyword
    @Query("""
            SELECT s FROM Shipment s
            WHERE s.shipper.id = :shipperId 
            AND s.status = :status
            AND (
                LOWER(s.order.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.order.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.order.id) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    Page<Shipment> searchByShipperAndStatusAndKeyword(
            @Param("shipperId") String shipperId,
            @Param("status") ShipmentStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);


    /* ===========================================================
       🔹 4. Các hàm cũ (List) — tương thích với bản dashboard cũ
       =========================================================== */

    List<Shipment> findByStatus(ShipmentStatus status);
    List<Shipment> findByShipper_Id(String shipperId);
    List<Shipment> findByShipper_IdAndStatusIn(String shipperId, List<ShipmentStatus> statuses);
    
    /* ===========================================================
       🔹 5. Các hàm mới cho Report và Profile
       =========================================================== */
    
    // Lấy shipments theo shipper và khoảng thời gian
    @Query("""
            SELECT s FROM Shipment s
            WHERE s.shipper.id = :shipperId 
            AND s.createdAt BETWEEN :start AND :end
            ORDER BY s.createdAt DESC
            """)
    List<Shipment> findByShipper_IdAndCreatedAtBetween(
            @Param("shipperId") String shipperId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    
    // Lấy 10 shipments gần nhất của shipper (cho profile)
    @Query("""
            SELECT s FROM Shipment s
            WHERE s.shipper.id = :shipperId 
            ORDER BY s.createdAt DESC
            LIMIT 10
            """)
    List<Shipment> findTop10ByShipper_IdOrderByCreatedAtDesc(@Param("shipperId") String shipperId);
    
    // Đếm số lượng shipment đang sử dụng shipping provider
    long countByShippingProvider_Id(String shippingProviderId);
}