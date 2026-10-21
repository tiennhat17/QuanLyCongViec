
package com.ccnlthd.taskmanager.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;


import com.ccnlthd.taskmanager.dto.CategoryCreateDTO;
import com.ccnlthd.taskmanager.dto.TaskCreateDTO;
import com.ccnlthd.taskmanager.exception.ProblemSupport;
import com.ccnlthd.taskmanager.model.Category;
import com.ccnlthd.taskmanager.model.Task;
import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.service.CategoryService;
import com.ccnlthd.taskmanager.service.TaskService;
import com.ccnlthd.taskmanager.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TaskController {

    private final UserService userService;
    private final TaskService taskService;
    private final CategoryService categoryService;


    /* 
        =========== GET Tasks ===========
    */
    @GetMapping("/tasks")
    public String list(
            @RequestParam(required = false) Long cid,
            @RequestParam(required = false) Long tid,
            Authentication authentication,
            Model model) {

        User user = getCurrentUser(authentication);

        // Object dùng cho form thêm/sửa Task.
        if (!model.containsAttribute("taskForm")) {
            model.addAttribute("taskForm", new TaskCreateDTO());
        }

        // Object dùng cho form Category.
        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new CategoryCreateDTO());
        }

        loadTaskPage(model, user, cid, tid);

        return "tasks";
    }

    /* 
        =========== Thêm hoặc sửa Task ===========
    */
    @PostMapping("/tasks")
    public String saveTask(
            @Valid @ModelAttribute("taskForm") TaskCreateDTO dto,
            BindingResult result,
            Authentication authentication,
            HttpServletRequest request,
            Model model) throws BindException {
        User user = getCurrentUser(authentication);

        // Bean Validation thất bại.
        if (result.hasErrors()) {
            if (ProblemSupport.isApi(request)) {
                throw new BindException(result);
            }

            model.addAttribute("openTaskModal", true);
            model.addAttribute("categoryForm", new CategoryCreateDTO());

            // Load lại toàn bộ dữ liệu cần thiết cho tasks.jsp.
            loadTaskPage(model, user, dto.getCategoryId(), dto.getId());

            return "tasks";
        }


        /*
         * Kiểm tra category có thực sự thuộc
         * user hiện tại hay không.
         *
         * Đây là kiểm tra authorization,
         * không được tin categoryId từ client.
         */
        Category category = categoryService.findByIdAndUserId(dto.getCategoryId(), user.getId());

        if (category == null) {
            throw notFound();
        }

        /*
         * If: thêm task mới
         *
         * Else: sửa task hiện có
         */
        if (dto.getId() == null) {

            taskService.create(dto, user, category);
        }
        else {

            taskService.update(dto,user,category);
        }


        return back(category.getId(), dto.getId());
    }

    /* 
        =========== Xóa Task ===========
    */
    @PostMapping("/tasks/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam(required = false) Long cid, Authentication authentication) {
        User user = getCurrentUser(authentication);

        Task task = taskService.findByIdAndUserId(id, user.getId());

        if (task == null) {
            throw notFound();
        }

        taskService.deleteTask(task);

        return back(cid, null);
    }

    /* 
        =========== Cập nhật trạng thái Task ===========
    */
    @PostMapping("/tasks/{id}/toggle")
    public String toggle(@PathVariable Long id, @RequestParam(required = false) Long cid, Authentication authentication) {
        User user = getCurrentUser(authentication);

        Boolean isToggled = taskService.updateStatusByIdAndUserId(id, user.getId());

        if (!isToggled) {
            throw notFound();
        }

        return back(cid, null);
    }
    


    /* 
        =========== HELPERS ===========
    */

    // Lấy user hiện tại từ Authentication
    private User getCurrentUser(Authentication authentication) 
    {
        String username = authentication.getName();
        return userService.findByUsername(username);
    }
    
    // load dữ liệu trang tasks.jsp
    private void loadTaskPage(Model model, User user, Long cid, Long tid) {
        Long uid = user.getId();
        // Lấy categories của user.
        List<Category> categories = categoryService.findByUserIdOrderByIdAsc(uid);

        // Xác định category hiện tại.
        Category curCat =
                categories.stream()
                        .filter(c ->
                                c.getId().equals(cid))
                        .findFirst()
                        .orElse(
                                categories.isEmpty()
                                        ? null
                                        : categories.get(0)
                        );



        // Lấy task thuộc category hiện tại.
        List<Task> allTask;

        if (curCat == null) 
        {
            allTask = List.of();
        } 
        else 
        {
            allTask = taskService.findByUserIdAndCategoryIdOrderByDueDateAscIdAsc(uid, curCat.getId());
        }

        // Xác định task đang được chọn.
        Task selTask =
                allTask.stream()
                        .filter(t ->
                                t.getId().equals(tid))
                        .findFirst()
                        .orElse(
                                allTask.isEmpty()
                                        ? null
                                        : allTask.get(0)
                        );


        // Đưa dữ liệu sang JSP.
        model.addAttribute("user", user);

        model.addAttribute("categories", categories);

        model.addAttribute("curCat", curCat);

        model.addAttribute("selTask", selTask);


        // Pending tasks
        model.addAttribute(
                "pending",
                allTask.stream()
                        .filter(t -> !t.isCompleted())
                        .toList()
        );

        // Completed tasks
        model.addAttribute(
                "done",
                allTask.stream()
                        .filter(Task::isCompleted)
                        .toList()
        );

        // Task đến hạn hôm nay.
        model.addAttribute(
                "dueToday",
                taskService.countByUserIdAndStatusAndDueDate(
                        uid,
                        Task.Status.TODO,
                        LocalDate.now()
                )
        );
    }



    // Redirect
    private static String back(Long cid, Long tid) {

        StringBuilder url = new StringBuilder("redirect:/tasks?");

        if (cid != null) {
            url.append("cid=").append(cid);
        }

        if (tid != null) {
            if (cid != null) {
                url.append("&");
            }
            url.append("tid=").append(tid);
        }

        return url.toString();
    }


    
    // 404
    private static ResponseStatusException notFound() {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND
        );
    }
}
