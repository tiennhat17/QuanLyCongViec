package com.ccnlthd.taskmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ccnlthd.taskmanager.service.UserService;
import com.ccnlthd.taskmanager.service.AdminUserStatistics;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminController controller;

    @Test
    void usersUsesTenItemPageAndClampsNegativePage() {
        when(userService.findUsers(eq("  An  "), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of()));

        ConcurrentModel model = new ConcurrentModel();
        assertEquals("admin-users", controller.users("  An  ", -4, model));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).findUsers(eq("  An  "), pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(10, pageable.getValue().getPageSize());
        assertEquals("DESC", pageable.getValue().getSort().getOrderFor("createdAt").getDirection().name());
        assertEquals(0, model.getAttribute("page"));
    }

    @Test
    void dashboardAddsUserStatisticsToModel() {
        AdminUserStatistics statistics = new AdminUserStatistics(12, 9, 3,
                java.util.List.of());
        when(userService.getAdminUserStatistics()).thenReturn(statistics);

        ConcurrentModel model = new ConcurrentModel();

        assertEquals("admin", controller.dashboard(model));
        assertEquals(statistics, model.getAttribute("statistics"));
        verify(userService).getAdminUserStatistics();
    }

    @Test
    void updateStatusAddsSuccessMessageAndPreservesSearchAndPage() {
        when(authentication.getName()).thenReturn("admin");

        String view = controller.updateStatus(7L, false, "alice@example.com", 2,
                authentication, redirectAttributes);

        assertEquals("redirect:/admin/users?page=2&search=alice%40example.com", view);
        verify(userService).updateStatus(7L, "admin", false);
        verify(redirectAttributes).addFlashAttribute("msg", "Đã khóa tài khoản.");
    }

    @Test
    void updateStatusAddsErrorMessageWhenServiceRejectsOperation() {
        when(authentication.getName()).thenReturn("admin");
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Không tìm thấy tài khoản"))
                .when(userService).updateStatus(99L, "admin", true);

        String view = controller.updateStatus(99L, true, "", -1,
                authentication, redirectAttributes);

        assertEquals("redirect:/admin/users?page=0", view);
        verify(redirectAttributes).addFlashAttribute("error", "Không tìm thấy tài khoản");
    }
}
