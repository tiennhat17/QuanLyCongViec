package com.ccnlthd.taskmanager.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


import com.ccnlthd.taskmanager.dto.CategoryCreateDTO;
import com.ccnlthd.taskmanager.dto.TaskCreateDTO;
import com.ccnlthd.taskmanager.model.Category;
import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.service.CategoryService;
import com.ccnlthd.taskmanager.service.TaskService;
import com.ccnlthd.taskmanager.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;



@Controller 
@RequiredArgsConstructor 
public class CategoryController {
    private final CategoryService categoryService;
    private final UserService userService;
    private final TaskService taskService;
    
    /* 
        =========== Thêm hoặc sửa Danh mục ===========
    */
    @PostMapping("/categories")
    public String saveCategory(
            @Valid @ModelAttribute("categoryForm") CategoryCreateDTO dto,
            BindingResult result,
            Authentication authentication,
            Model model) {

        // Bean Validation thất bại
        if (result.hasErrors()) {
            model.addAttribute("openCatModal", true);
            model.addAttribute("taskForm", new TaskCreateDTO());

            loadTaskPage(model, authentication);

            return "tasks";
        }

        // Lấy user hiện tại
        User user = getCurrentUser(authentication);

        // Business logic tạo Category
        if (dto.getId() == null) {
            categoryService.create(dto, user);
        }
        else {

            categoryService.update(dto, user);
        }

        return "redirect:/tasks";
    }


    @PostMapping("/categories/{id}/delete")
    public String delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);

        Category category =
                categoryService.findByIdAndUserId(
                        id,
                        user.getId()
                );
                
        // Xóa toàn bộ task thuộc category
        taskService.deleteByCategoryIdAndUserId(
                id,
                user.getId()
        );

        // Sau đó xóa category
        categoryService.delete(category);

        return "redirect:/tasks";
    }
    


    /* 
        =========== HELPERS ===========
    */

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username);
    }

    private void loadTaskPage( Model model, Authentication authentication) 
    {

        User user = getCurrentUser(authentication);

        model.addAttribute("user", user);

        model.addAttribute(
                "categories",
                categoryService.findByUser(user)
        );
    }
}
