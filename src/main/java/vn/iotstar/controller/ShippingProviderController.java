package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.ShippingProvider;
import vn.iotstar.repository.ShipmentRepository;
import vn.iotstar.repository.ShippingProviderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/shipping")
public class ShippingProviderController {

    @Autowired
    private ShippingProviderRepository shippingProviderRepository;
    
    @Autowired
    private ShipmentRepository shipmentRepository;

    @GetMapping("")
    public String list(@RequestParam(required = false) String search, Model model) {
        List<ShippingProvider> providers;
        if (search != null && !search.trim().isEmpty()) {
            providers = shippingProviderRepository.searchByName(search.trim());
            model.addAttribute("search", search);
        } else {
            providers = shippingProviderRepository.findAll();
        }
        model.addAttribute("providers", providers);
        model.addAttribute("totalProviders", providers != null ? providers.size() : 0);
        return "admin/shipping-providers";
    }

    @PostMapping("/create")
    public String create(@RequestParam String name,
                         @RequestParam(required = false, defaultValue = "0") String shippingFee,
                         RedirectAttributes redirectAttributes) {
        try {
            if (name == null || name.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("messageType", "danger");
                redirectAttributes.addFlashAttribute("message", "Tên nhà vận chuyển không được để trống");
                return "redirect:/admin/shipping";
            }

            Optional<ShippingProvider> exist = shippingProviderRepository.findByName(name.trim());
            if (exist.isPresent()) {
                redirectAttributes.addFlashAttribute("messageType", "danger");
                redirectAttributes.addFlashAttribute("message", "Tên nhà vận chuyển đã tồn tại");
                return "redirect:/admin/shipping";
            }

            ShippingProvider sp = new ShippingProvider();
            sp.setId("SP_" + System.currentTimeMillis());
            sp.setName(name.trim());
            BigDecimal fee = new BigDecimal(shippingFee.replaceAll("[^0-9.-]", ""));
            sp.setShippingFee(fee);
            sp.setIsActive(true);

            shippingProviderRepository.save(sp);

            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Tạo nhà vận chuyển thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("messageType", "danger");
            redirectAttributes.addFlashAttribute("message", "Lỗi khi tạo nhà vận chuyển: " + e.getMessage());
        }
        return "redirect:/admin/shipping";
    }

    @PostMapping("/update")
    public String update(@RequestParam String id,
                         @RequestParam String name,
                         @RequestParam(required = false, defaultValue = "0") String shippingFee,
                         @RequestParam(required = false) Boolean isActive,
                         RedirectAttributes redirectAttributes) {
        try {
            Optional<ShippingProvider> opt = shippingProviderRepository.findById(id);
            if (opt.isEmpty()) {
                redirectAttributes.addFlashAttribute("messageType", "danger");
                redirectAttributes.addFlashAttribute("message", "Nhà vận chuyển không tồn tại");
                return "redirect:/admin/shipping";
            }
            ShippingProvider sp = opt.get();
            sp.setName(name.trim());
            BigDecimal fee = new BigDecimal(shippingFee.replaceAll("[^0-9.-]", ""));
            sp.setShippingFee(fee);
            // Khi checkbox không được check, isActive sẽ là null => set false
            // Khi checkbox được check, isActive sẽ là true
            sp.setIsActive(isActive != null && isActive);
            shippingProviderRepository.save(sp);

            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("message", "Cập nhật thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("messageType", "danger");
            redirectAttributes.addFlashAttribute("message", "Lỗi khi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/shipping";
    }

    @PostMapping("/delete")
    @ResponseBody
    public String delete(@RequestParam String id) {
        try {
            // Kiểm tra xem nhà vận chuyển có đang được sử dụng không
            long count = shipmentRepository.countByShippingProvider_Id(id);
            if (count > 0) {
                return "ERR: Không thể xóa nhà vận chuyển đang được sử dụng trong " + count + " đơn hàng";
            }
            
            shippingProviderRepository.deleteById(id);
            return "OK";
        } catch (Exception e) {
            return "ERR: " + e.getMessage();
        }
    }
}