package com.ccnlthd.taskmanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ccnlthd.taskmanager.model.Category;
import com.ccnlthd.taskmanager.model.User;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrderByIdAsc(Long userId);
    Category findByIdAndUserId(Long id, Long userId);
    List<Category> findByUser(User user);
}

