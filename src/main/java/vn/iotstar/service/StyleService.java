package vn.iotstar.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.iotstar.entity.Style;
import vn.iotstar.repository.StyleRepository;

@Service
public class StyleService {

    @Autowired
    private StyleRepository styleRepository;

    public List<Style> getStylesByCategory(String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) {
         return styleRepository.findByIsDeletedFalse();
      }
      return styleRepository.findByCategoryId(categoryId);
    }
}
