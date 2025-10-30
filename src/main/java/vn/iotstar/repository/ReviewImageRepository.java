package vn.iotstar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.entity.ReviewImage;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, String> {
    List<ReviewImage> findByReview_Id(String reviewId);
}