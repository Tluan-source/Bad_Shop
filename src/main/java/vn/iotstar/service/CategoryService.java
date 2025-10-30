package vn.iotstar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.Category;
import vn.iotstar.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc();
    }

    public Category getCategoryById(String id) {
        return categoryRepository.findById(id).orElse(null);
    }
}
