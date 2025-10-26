package vn.iotstar.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simple wrapper service for Cloudinary uploads
 */
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.cloud-name}") String cloudName,
                             @Value("${cloudinary.api-key}") String apiKey,
                             @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    /** Upload a single file and return secure URL (or null on failure) */
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            Map<?, ?> res = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
            Object url = res.get("secure_url");
            return url != null ? url.toString() : null;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary: " + e.getMessage(), e);
        }
    }

    /** Upload multiple files and return list of secure URLs (skips failures) */
    public List<String> uploadFiles(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        if (files == null) return urls;
        for (MultipartFile f : files) {
            try {
                String u = uploadFile(f);
                if (u != null) urls.add(u);
            } catch (Exception ex) {
                // skip failed uploads but continue
            }
        }
        return urls;
    }
}
