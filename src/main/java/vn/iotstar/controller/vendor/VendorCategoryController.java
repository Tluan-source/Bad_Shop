package vn.iotstar.controller.vendor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Style;
import vn.iotstar.entity.StyleValue;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.StyleRepository;
import vn.iotstar.repository.StyleValueRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/vendor/categories")
public class VendorCategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StyleRepository styleRepository;

    @Autowired
    private StyleValueRepository styleValueRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public String list(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "vendor/categories/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("category", new Category());
        return "vendor/categories/form";
    }

    @PostMapping
    public String create(@ModelAttribute Category category, RedirectAttributes ra) {
        category.setId(UUID.randomUUID().toString());
        if (category.getIsActive() == null) category.setIsActive(true);
        categoryRepository.save(category);
        ra.addFlashAttribute("success", "Tạo danh mục thành công");
        return "redirect:/vendor/categories";
    }

    // ------------------ Styles (per category) ------------------

    @GetMapping("/{categoryId}/styles")
    public String listStyles(@PathVariable String categoryId, Model model, RedirectAttributes ra) {
        Category c = categoryRepository.findById(categoryId).orElse(null);
        if (c == null) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại");
            return "redirect:/vendor/categories";
        }
    // show all available styles (including soft-deleted) so vendor can edit/reactivate them,
    // and mark which ones are already applied to this category
    List<Style> styles = styleRepository.findAll();
        List<String> appliedIds = new ArrayList<>();
        for (Style s : styles) {
            if (s.getCategoryIds() == null || s.getCategoryIds().isEmpty()) continue;
            try {
                List<String> ids = objectMapper.readValue(s.getCategoryIds(), new TypeReference<List<String>>(){});
                if (ids.contains(categoryId)) appliedIds.add(s.getId());
            } catch (Exception ex) {
                // ignore malformed
            }
        }
        model.addAttribute("category", c);
        model.addAttribute("styles", styles);
        model.addAttribute("appliedIds", appliedIds);
        return "vendor/categories/styles";
    }

    @PostMapping("/{categoryId}/styles/{styleId}/apply")
    public String applyStyleToCategory(@PathVariable String categoryId, @PathVariable String styleId, RedirectAttributes ra) {
        Style s = styleRepository.findById(styleId).orElse(null);
        if (s == null) {
            ra.addFlashAttribute("error", "Style không tồn tại");
            return "redirect:/vendor/categories/" + categoryId + "/styles";
        }
        try {
            List<String> ids = new ArrayList<>();
            if (s.getCategoryIds() != null && !s.getCategoryIds().isEmpty()) {
                ids = objectMapper.readValue(s.getCategoryIds(), new TypeReference<List<String>>(){});
            }
            if (!ids.contains(categoryId)) ids.add(categoryId);
            s.setCategoryIds(objectMapper.writeValueAsString(ids));
            styleRepository.save(s);
            ra.addFlashAttribute("success", "Áp dụng style thành công");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Không thể áp dụng style");
        }
        return "redirect:/vendor/categories/" + categoryId + "/styles";
    }

    @PostMapping("/{categoryId}/styles/{styleId}/unapply")
    public String unapplyStyleFromCategory(@PathVariable String categoryId, @PathVariable String styleId, RedirectAttributes ra) {
        Style s = styleRepository.findById(styleId).orElse(null);
        if (s == null) {
            ra.addFlashAttribute("error", "Style không tồn tại");
            return "redirect:/vendor/categories/" + categoryId + "/styles";
        }
        try {
            List<String> ids = new ArrayList<>();
            if (s.getCategoryIds() != null && !s.getCategoryIds().isEmpty()) {
                ids = objectMapper.readValue(s.getCategoryIds(), new TypeReference<List<String>>(){});
            }
            if (ids.contains(categoryId)) ids.removeIf(id -> id.equals(categoryId));
            s.setCategoryIds(objectMapper.writeValueAsString(ids));
            styleRepository.save(s);
            ra.addFlashAttribute("success", "Bỏ áp dụng style thành công");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Không thể bỏ áp dụng style");
        }
        return "redirect:/vendor/categories/" + categoryId + "/styles";
    }

    @GetMapping("/{categoryId}/styles/new")
    public String newStyleForm(@PathVariable String categoryId, Model model, RedirectAttributes ra) {
        Category c = categoryRepository.findById(categoryId).orElse(null);
        if (c == null) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại");
            return "redirect:/vendor/categories";
        }
        model.addAttribute("category", c);
        model.addAttribute("style", new Style());
        return "vendor/categories/style-form";
    }

    @PostMapping("/{categoryId}/styles")
    public String createStyle(@PathVariable String categoryId, @ModelAttribute Style style, RedirectAttributes ra) {
        Category c = categoryRepository.findById(categoryId).orElse(null);
        if (c == null) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại");
            return "redirect:/vendor/categories";
        }
        style.setId(UUID.randomUUID().toString());
        style.setIsDeleted(false);
        try {
            List<String> ids = new ArrayList<>(); ids.add(categoryId);
            style.setCategoryIds(objectMapper.writeValueAsString(ids));
        } catch (Exception ex) { style.setCategoryIds("[]"); }
        styleRepository.save(style);
        ra.addFlashAttribute("success", "Tạo style thành công");
        return "redirect:/vendor/categories/" + categoryId + "/styles";
    }

    @GetMapping("/{categoryId}/styles/{styleId}/edit")
    public String editStyleForm(@PathVariable String categoryId, @PathVariable String styleId, Model model, RedirectAttributes ra) {
        Category c = categoryRepository.findById(categoryId).orElse(null);
        Style s = styleRepository.findById(styleId).orElse(null);
        if (c == null || s == null) {
            ra.addFlashAttribute("error", "Danh mục hoặc style không tồn tại");
            return "redirect:/vendor/categories";
        }
        model.addAttribute("category", c);
        model.addAttribute("style", s);
        return "vendor/categories/style-form";
    }

    @PostMapping("/{categoryId}/styles/{styleId}/update")
    public String updateStyle(@PathVariable String categoryId, @PathVariable String styleId, @ModelAttribute Style style, RedirectAttributes ra) {
        Style s = styleRepository.findById(styleId).orElse(null);
        if (s == null) {
            ra.addFlashAttribute("error", "Style không tồn tại");
            return "redirect:/vendor/categories";
        }
        s.setName(style.getName());
        // allow vendor to reactivate / mark deleted via edit form
        // if checkbox omitted, treat as false (not deleted)
        Boolean deletedFlag = style.getIsDeleted();
        s.setIsDeleted(deletedFlag != null ? deletedFlag : false);
        try {
            List<String> ids = new ArrayList<>();
            if (s.getCategoryIds() != null && !s.getCategoryIds().isEmpty()) ids = objectMapper.readValue(s.getCategoryIds(), new TypeReference<List<String>>(){});
            if (!ids.contains(categoryId)) ids.add(categoryId);
            s.setCategoryIds(objectMapper.writeValueAsString(ids));
        } catch (Exception ex) { }
        styleRepository.save(s);
        ra.addFlashAttribute("success", "Cập nhật style thành công");
        return "redirect:/vendor/categories/" + categoryId + "/styles";
    }

    @PostMapping("/{categoryId}/styles/{styleId}/delete")
    public String deleteStyle(@PathVariable String categoryId, @PathVariable String styleId, RedirectAttributes ra) {
        Style s = styleRepository.findById(styleId).orElse(null);
        if (s == null) {
            ra.addFlashAttribute("error", "Style không tồn tại");
            return "redirect:/vendor/categories";
        }
        s.setIsDeleted(true);
        styleRepository.save(s);
        ra.addFlashAttribute("success", "Đã vô hiệu hóa style");
        return "redirect:/vendor/categories/" + categoryId + "/styles";
    }

    // ------------------ StyleValues ------------------

    @GetMapping("/{categoryId}/styles/{styleId}/values")
    public String listStyleValues(@PathVariable String categoryId, @PathVariable String styleId, Model model, RedirectAttributes ra) {
        Category c = categoryRepository.findById(categoryId).orElse(null);
        Style s = styleRepository.findById(styleId).orElse(null);
        if (c == null || s == null) {
            ra.addFlashAttribute("error", "Danh mục hoặc style không tồn tại");
            return "redirect:/vendor/categories";
        }
        // return all values (including soft-deleted) so vendor can see and reactivate them if needed
        List<StyleValue> values = styleValueRepository.findByStyleId(styleId);
        model.addAttribute("category", c);
        model.addAttribute("style", s);
        model.addAttribute("values", values);
        return "vendor/categories/style-values";
    }

    @GetMapping("/{categoryId}/styles/{styleId}/values/new")
    public String newStyleValueForm(@PathVariable String categoryId, @PathVariable String styleId, Model model, RedirectAttributes ra) {
        Category c = categoryRepository.findById(categoryId).orElse(null);
        Style s = styleRepository.findById(styleId).orElse(null);
        if (c == null || s == null) {
            ra.addFlashAttribute("error", "Danh mục hoặc style không tồn tại");
            return "redirect:/vendor/categories";
        }
        model.addAttribute("category", c);
        model.addAttribute("style", s);
        model.addAttribute("value", new StyleValue());
        return "vendor/categories/style-value-form";
    }

    @PostMapping("/{categoryId}/styles/{styleId}/values")
    public String createStyleValue(@PathVariable String categoryId, @PathVariable String styleId, @ModelAttribute StyleValue value, RedirectAttributes ra) {
        Style s = styleRepository.findById(styleId).orElse(null);
        if (s == null) {
            ra.addFlashAttribute("error", "Style không tồn tại");
            return "redirect:/vendor/categories";
        }
        value.setId(UUID.randomUUID().toString());
        value.setStyle(s);
        value.setIsDeleted(false);
        styleValueRepository.save(value);
        ra.addFlashAttribute("success", "Tạo giá trị thuộc tính thành công");
        return "redirect:/vendor/categories/" + categoryId + "/styles/" + styleId + "/values";
    }

    @GetMapping("/{categoryId}/styles/{styleId}/values/{valueId}/edit")
    public String editStyleValueForm(@PathVariable String categoryId, @PathVariable String styleId, @PathVariable String valueId, Model model, RedirectAttributes ra) {
        Category c = categoryRepository.findById(categoryId).orElse(null);
    Style s = styleRepository.findById(styleId).orElse(null);
        StyleValue v = styleValueRepository.findById(valueId).orElse(null);
        if (c == null || s == null || v == null) {
            ra.addFlashAttribute("error", "Dữ liệu không tồn tại");
            return "redirect:/vendor/categories";
        }
        model.addAttribute("category", c);
        model.addAttribute("style", s);
        model.addAttribute("value", v);
        return "vendor/categories/style-value-form";
    }

    @PostMapping("/{categoryId}/styles/{styleId}/values/{valueId}/update")
    public String updateStyleValue(@PathVariable String categoryId, @PathVariable String styleId, @PathVariable String valueId, @ModelAttribute StyleValue value, RedirectAttributes ra) {
        StyleValue v = styleValueRepository.findById(valueId).orElse(null);
        if (v == null) {
            ra.addFlashAttribute("error", "Giá trị không tồn tại");
            return "redirect:/vendor/categories/" + categoryId + "/styles/" + styleId + "/values";
        }
        v.setName(value.getName());
        // honor the checkbox: if checkbox absent (null) treat as not deleted
        v.setIsDeleted(value.getIsDeleted() != null ? value.getIsDeleted() : false);
        styleValueRepository.save(v);
        ra.addFlashAttribute("success", "Cập nhật giá trị thành công");
        return "redirect:/vendor/categories/" + categoryId + "/styles/" + styleId + "/values";
    }

    @PostMapping("/{categoryId}/styles/{styleId}/values/{valueId}/delete")
    public String deleteStyleValue(@PathVariable String categoryId, @PathVariable String styleId, @PathVariable String valueId, RedirectAttributes ra) {
        StyleValue v = styleValueRepository.findById(valueId).orElse(null);
        if (v == null) {
            ra.addFlashAttribute("error", "Giá trị không tồn tại");
            return "redirect:/vendor/categories/" + categoryId + "/styles/" + styleId + "/values";
        }
        v.setIsDeleted(true);
        styleValueRepository.save(v);
        ra.addFlashAttribute("success", "Đã vô hiệu hóa giá trị");
        return "redirect:/vendor/categories/" + categoryId + "/styles/" + styleId + "/values";
    }

    @PostMapping("/{categoryId}/styles/{styleId}/values/{valueId}/activate")
    public String activateStyleValue(@PathVariable String categoryId, @PathVariable String styleId, @PathVariable String valueId, RedirectAttributes ra) {
        StyleValue v = styleValueRepository.findById(valueId).orElse(null);
        if (v == null) {
            ra.addFlashAttribute("error", "Giá trị không tồn tại");
            return "redirect:/vendor/categories/" + categoryId + "/styles/" + styleId + "/values";
        }
        v.setIsDeleted(false);
        styleValueRepository.save(v);
        ra.addFlashAttribute("success", "Đã kích hoạt lại giá trị");
        return "redirect:/vendor/categories/" + categoryId + "/styles/" + styleId + "/values";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model, RedirectAttributes ra) {
        Category c = categoryRepository.findById(id).orElse(null);
        if (c == null) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại");
            return "redirect:/vendor/categories";
        }
        model.addAttribute("category", c);
        return "vendor/categories/form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable String id, @ModelAttribute Category category, RedirectAttributes ra) {
        Category c = categoryRepository.findById(id).orElse(null);
        if (c == null) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại");
            return "redirect:/vendor/categories";
        }
        c.setName(category.getName());
        c.setSlug(category.getSlug());
        c.setDescription(category.getDescription());
        c.setIsActive(category.getIsActive() != null ? category.getIsActive() : true);
        categoryRepository.save(c);
        ra.addFlashAttribute("success", "Cập nhật danh mục thành công");
        return "redirect:/vendor/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, RedirectAttributes ra) {
        Category c = categoryRepository.findById(id).orElse(null);
        if (c == null) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại");
            return "redirect:/vendor/categories";
        }
        // soft delete
        c.setIsActive(false);
        categoryRepository.save(c);
        ra.addFlashAttribute("success", "Đã vô hiệu hóa danh mục");
        return "redirect:/vendor/categories";
    }
}
