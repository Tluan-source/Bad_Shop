package vn.iotstar.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.entity.Shipment;

public interface ShipmentService {

    List<Shipment> getShipperShipments(String shipperId);

    void confirmDelivered(String shipmentId, MultipartFile image, String shipperId);

    Shipment getById(String id);
}
