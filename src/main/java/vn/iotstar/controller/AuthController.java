package vn.iotstar.controller;

import vn.iotstar.entity.User;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.service.UserService;
import vn.iotstar.service.OtpService;
import vn.iotstar.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private PasswordService passwordService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, @RequestParam("password") String password, Model model) {
        try {
            // create user (inactive) and save hashed password
            User saved = userService.registerUser(user, password);

            // create OTP and send email
            otpService.createOtpForUser(saved, OtpToken.OtpType.REGISTER, 10);

            model.addAttribute("userId", saved.getId());
            return "auth/verify-otp"; // show verify form
        } catch (Exception e) {
            model.addAttribute("error", "Email đã tồn tại hoặc lỗi hệ thống");
            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam(value = "userId", required = false) String userId, Model model) {
        model.addAttribute("userId", userId);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("userId") String userId, @RequestParam("code") String code, Model model) {
        var uOpt = userService.findById(userId);
        if (uOpt.isEmpty()) {
            model.addAttribute("error", "Người dùng không tồn tại");
            return "auth/verify-otp";
        }
        User user = uOpt.get();
        boolean ok = otpService.verifyOtp(user, code, OtpToken.OtpType.REGISTER);
        if (ok) {
            user.setStatus(User.UserStatus.ACTIVE);
            userService.updateUser(user);
            return "redirect:/login?activated";
        } else {
            model.addAttribute("error", "Mã OTP không hợp lệ hoặc đã hết hạn");
            model.addAttribute("userId", userId);
            return "auth/verify-otp";
        }
    }

    @GetMapping("/forgot")
    public String forgotPage() {
        return "auth/forgot";
    }

    @PostMapping("/forgot")
    public String sendForgotOtp(@RequestParam("email") String email, Model model) {
        var uOpt = userService.findByEmail(email);
        if (uOpt.isEmpty()) {
            model.addAttribute("error", "Email không tồn tại");
            return "auth/forgot";
        }
        User user = uOpt.get();
        otpService.createOtpForUser(user, OtpToken.OtpType.RESET, 10);
        model.addAttribute("userId", user.getId());
        return "auth/verify-reset";
    }

    @PostMapping("/reset")
    public String resetPassword(@RequestParam("userId") String userId, @RequestParam("code") String code,
                                @RequestParam("password") String password, Model model) {
        var uOpt = userService.findById(userId);
        if (uOpt.isEmpty()) {
            model.addAttribute("error", "Người dùng không tồn tại");
            return "auth/verify-reset";
        }
        User user = uOpt.get();
        boolean ok = otpService.verifyOtp(user, code, OtpToken.OtpType.RESET);
        if (ok) {
            passwordService.createUserWithHashedPassword(user, password);
            userService.updateUser(user);
            return "redirect:/login?reset";
        } else {
            model.addAttribute("error", "Mã OTP không hợp lệ hoặc đã hết hạn");
            model.addAttribute("userId", userId);
            return "auth/verify-reset";
        }
    }
}