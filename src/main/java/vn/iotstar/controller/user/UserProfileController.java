package vn.iotstar.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.iotstar.entity.User;
import vn.iotstar.service.user.UserProfileService;

import java.util.Optional;

@Controller
@RequestMapping("/user/profile")
public class UserProfileController {
    
    @Autowired
    private UserProfileService userProfileService;
    
    @GetMapping
    public String profilePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        Optional<User> userOpt = userProfileService.getUserByEmail(email);
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            return "user/profile";
        }
        
        return "redirect:/login";
    }
    
    @PostMapping("/update")
    public String updateProfile(
        @RequestParam("fullName") String fullName,
        @RequestParam("phone") String phone,
        RedirectAttributes redirectAttributes) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        Optional<User> userOpt = userProfileService.getUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFullName(fullName);
            user.setPhone(phone);
            userProfileService.updateProfile(user);
            
            redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "Có lỗi xảy ra!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/user/profile";
    }
    
    @PostMapping("/change-password")
    public String changePassword(
        @RequestParam("oldPassword") String oldPassword,
        @RequestParam("newPassword") String newPassword,
        @RequestParam("confirmPassword") String confirmPassword,
        RedirectAttributes redirectAttributes) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        Optional<User> userOpt = userProfileService.getUserByEmail(email);
        if (userOpt.isPresent()) {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("message", "Mật khẩu mới không khớp!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/user/profile";
            }
            
            User user = userOpt.get();
            boolean success = userProfileService.changePassword(user.getId(), oldPassword, newPassword);
            
            if (success) {
                redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công!");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", "Mật khẩu cũ không đúng!");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }
        } else {
            redirectAttributes.addFlashAttribute("message", "Có lỗi xảy ra!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/user/profile";
    }
    @PostMapping("/avatar")
    public String updateAvatar(@RequestParam("avatar") MultipartFile file,
                            RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        try {
            Optional<User> userOpt = userProfileService.getUserByEmail(email);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy người dùng!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/user/profile";
            }

            User user = userOpt.get();

            if (file != null && !file.isEmpty()) {
                // Upload ảnh lên Cloudinary
                String imageUrl = userProfileService.uploadAvatar(file);
                user.setAvatar(imageUrl);
                userProfileService.updateProfile(user);

                redirectAttributes.addFlashAttribute("message", "Cập nhật ảnh đại diện thành công!");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", "Vui lòng chọn ảnh hợp lệ!");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Lỗi khi tải ảnh: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/user/profile";
    }
}
