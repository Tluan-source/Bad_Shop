package vn.iotstar.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import vn.iotstar.entity.Favorite;
import vn.iotstar.service.FavoriteService;

@Controller
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    
    private final FavoriteService favoriteService;
    
    @GetMapping
    @Transactional(readOnly = true)
    public String viewFavorites(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            List<Favorite> favorites = favoriteService.getFavorites();
            model.addAttribute("favorites", favorites);
            model.addAttribute("favoriteCount", favorites.size());
        } catch (Exception e) {
            model.addAttribute("favorites", List.of());
            model.addAttribute("favoriteCount", 0);
            model.addAttribute("error", "Không thể tải danh sách yêu thích");
        }
        
        return "user/favorites";
    }
    
    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @RequestParam String productId,
            Authentication auth) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (auth == null || !auth.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập");
                return ResponseEntity.ok(response);
            }
            
            boolean added = favoriteService.toggleFavorite(productId);
            
            response.put("success", true);
            response.put("added", added);
            response.put("message", added ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích");
            response.put("favoriteCount", favoriteService.getFavoriteCount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    @GetMapping("/check/{productId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkFavorite(@PathVariable String productId) {
        Map<String, Object> response = new HashMap<>();
        response.put("isFavorite", favoriteService.isFavorite(productId));
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFavoriteCount() {
        Map<String, Object> response = new HashMap<>();
        response.put("count", favoriteService.getFavoriteCount());
        return ResponseEntity.ok(response);
    }
}
