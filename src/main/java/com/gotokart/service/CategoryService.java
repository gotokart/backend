package com.gotokart.service;

import com.gotokart.model.Category;
import com.gotokart.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Create category
    public Category create(Category category) {

        // check if category already exists
        return categoryRepository.findByName(category.getName())
                .orElseGet(() -> categoryRepository.save(category));
    }

    // Get all categories
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    // Get category by id
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    // Update category name
    public Category update(Long id, Category patch) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (patch.getName() != null && !patch.getName().isBlank()) {
            existing.setName(patch.getName().trim());
        }
        return categoryRepository.save(existing);
    }

    // Delete category. Caller is responsible for re-categorising any products
    // that still reference this category — the FK is nullable so the rows are
    // not lost, but they will become "uncategorised" until reassigned.
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

}