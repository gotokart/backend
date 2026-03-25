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

    // Delete category
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

}