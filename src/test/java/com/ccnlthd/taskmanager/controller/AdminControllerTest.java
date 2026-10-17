package com.ccnlthd.taskmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ccnlthd.taskmanager.dto.AdminDashboardDTO;
import com.ccnlthd.taskmanager.service.AdminDashboardService;
import com.ccnlthd.taskmanager.service.AdminUserService;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminDashboardService adminDashboardService;

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectAttributes redirectAttributes;

    private AdminController newController() {
        return new AdminController(adminDashboardService, adminUserService);
    }

    @Test
    void usersUsesTenItemPageAndClampsNegativePage() {
        when(adminUserService.findUsers(eq("  An  "), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of()));

        AdminController controller = newController();
        ConcurrentModel model = new ConcurrentModel();
        assertEquals("admin-users", controller.users("  An  ", -4, model));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(adminUserService).findUsers(eq("  An  "), pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(10, pageable.getValue().getPageSize());
        assertEquals("DESC", pageable.getValue().getSort().getOrderFor("createdAt").getDirection().name());
        assertEquals(0, model.getAttribute("page"));
    }

    @Test
    void dashboardAddsDashboardToModel() {
        AdminDashboardDTO dashboard = new AdminDashboardDTO(
                12, 9, 3, 20, 8, 12, 4, 6, 4,
                java.util.List.of(), java.util.List.of(), java.util.List.of());
        when(adminDashboardService.getAdminDashboard()).thenReturn(dashboard);

        AdminController controller = newController();
        ConcurrentModel model = new ConcurrentModel();

        assertEquals("admin", controller.dashboard(model));
        assertEquals(dashboard, model.getAttribute("dashboard"));
        verify(adminDashboardService).getAdminDashboard();
    }

    @Test
    void taskStatisticsAddsStatisticsToModel() {
        AdminDashboardDTO dashboard = new AdminDashboardDTO(
                12, 9, 3, 20, 8, 12, 4, 6, 4,
                java.util.List.of(), java.util.List.of(), java.util.List.of());
        when(adminDashboardService.getAdminDashboard()).thenReturn(dashboard);

        AdminController controller = newController();
        ConcurrentModel model = new ConcurrentModel();

        assertEquals("admin-tasks", controller.taskStatistics(model));
        assertEquals(dashboard, model.getAttribute("statistics"));
        verify(adminDashboardService).getAdminDashboard();
    }

    @Test
    void updateStatusAddsSuccessMessageAndPreservesSearchAndPage() {
        when(authentication.getName()).thenReturn("admin");

        AdminController controller = newController();
        String view = controller.updateStatus(7L, false, "alice@example.com", 2,
                authentication, redirectAttributes);

        assertEquals("redirect:/admin/users?page=2&search=alice%40example.com", view);
        verify(adminUserService).updateStatus(7L, "admin", false);
        verify(redirectAttributes).addFlashAttribute("msg", "Đã khóa tài khoản.");
    }

    @Test
    void updateStatusAddsErrorMessageWhenServiceRejectsOperation() {
        when(authentication.getName()).thenReturn("admin");
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Không tìm thấy tài khoản"))
                .when(adminUserService).updateStatus(99L, "admin", true);

        AdminController controller = newController();
        String view = controller.updateStatus(99L, true, "", -1,
                authentication, redirectAttributes);

        assertEquals("redirect:/admin/users?page=0", view);
        verify(redirectAttributes).addFlashAttribute("error", "Không tìm thấy tài khoản");
    }

    @Test
    void updateStatusAddsErrorMessageWhenSelfLockRejected() {
        when(authentication.getName()).thenReturn("admin");
        org.mockito.Mockito.doThrow(new org.springframework.security.access.AccessDeniedException(
                "Admin không thể tự khóa tài khoản của mình"))
                .when(adminUserService).updateStatus(1L, "admin", false);

        AdminController controller = newController();
        String view = controller.updateStatus(1L, false, "", 0,
                authentication, redirectAttributes);

        assertEquals("redirect:/admin/users?page=0", view);
        verify(redirectAttributes).addFlashAttribute("error", "Admin không thể tự khóa tài khoản của mình");
    }
}
