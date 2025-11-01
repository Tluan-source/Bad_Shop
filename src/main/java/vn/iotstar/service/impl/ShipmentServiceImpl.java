package vn.iotstar.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.Shipment;
import vn.iotstar.entity.Shipment.ShipmentStatus;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.ShipmentRepository;
import vn.iotstar.service.ShipmentService;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final Cloudinary cloudinary;

    @Override
    public List<Shipment> getShipperShipments(String shipperId) {
        return shipmentRepository.findByShipper_IdAndStatus(shipperId, ShipmentStatus.DELIVERING);
    }

    @Override
    public Shipment getById(String id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
    }

    @Override
    public void confirmDelivered(String shipmentId, MultipartFile file, String shipperId) {
        Shipment shipment = getById(shipmentId);

        if (shipment.getShipper() == null ||
            !shipment.getShipper().getId().equals(shipperId)) {
            throw new RuntimeException("Shipper không có quyền xác nhận đơn này");
        }

        try {
            // Upload ảnh lên Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(), ObjectUtils.emptyMap()
            );
            String imgUrl = uploadResult.get("secure_url").toString();

            shipment.setStatus(ShipmentStatus.DELIVERED);
            shipment.setDeliveredAt(LocalDateTime.now());
            shipment.setDeliveryImageUrl(imgUrl);

            shipmentRepository.save(shipment);

            // Đồng thời cập nhật Order
            Order order = shipment.getOrder();
            order.setStatus(Order.OrderStatus.DELIVERED);
            // order.setDeliveredAt(LocalDateTime.now());

            orderRepository.save(order);

        } catch (Exception e) {
            throw new RuntimeException("Upload ảnh thất bại: " + e.getMessage());
        }
    }
}
