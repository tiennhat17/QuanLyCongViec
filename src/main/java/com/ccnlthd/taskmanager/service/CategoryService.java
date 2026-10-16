package com.ccnlthd.taskmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ccnlthd.taskmanager.dto.CategoryCreateDTO;
import com.ccnlthd.taskmanager.model.Category;
import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.repository.CategoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class CategoryService {
    private final CategoryRepository categories;

    public Category save(Category category) {
        return categories.save(category);
    }

    public List<Category> findByUserIdOrderByIdAsc(Long userId) {
        return categories.findByUserIdOrderByIdAsc(userId);
    }

    public Category findByIdAndUserId(Long id, Long userId) {
        return categories.findByIdAndUserId(id, userId);
    }

    public List<Category> findByUser(User user) {
        return categories.findByUser(user);
    }

    public void delete(Category category) {
        categories.delete(category);
    }


    @Transactional
    public Category create(CategoryCreateDTO dto, User user) {

        Category category = new Category();

        category.setName(dto.getName().trim());
        category.setUser(user);

        return categories.save(category);
    }

    @Transactional
    public Category update(CategoryCreateDTO dto, User user) {

        Category category = categories.findByIdAndUserId(dto.getId(), user.getId());

        category.setName(dto.getName().trim());
        category.setUser(user);

        return categories.save(category);
    }

}
