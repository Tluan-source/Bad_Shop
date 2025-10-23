package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "review_images")
@Data
public class ReviewImage {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;
    
    @Column(nullable = false)
    private String url;
    
    @Enumerated(EnumType.STRING)
    private MediaType type = MediaType.IMAGE;
    
    public enum MediaType {
        IMAGE, VIDEO
    }
}
