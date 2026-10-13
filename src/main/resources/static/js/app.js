
// Tương tác cơ bản cho trang quản trị
if (document.body.classList.contains('admin-page')) {
  $$('form[action*="/admin/users/"][action$="/status"]').forEach(form => {
    form.addEventListener('submit', e => {
      const button = form.querySelector('button[type="submit"]');
      const isLocking = form.querySelector('input[name="enabled"][value="false"]');
      const message = isLocking ? 'Bạn có chắc muốn khóa tài khoản này?' : 'Bạn có chắc muốn mở khóa tài khoản này?';

      if (!window.confirm(message)) {
        e.preventDefault();
        return;
      }

      if (button) {
        button.disabled = true;
        button.textContent = 'Đang xử lý...';
      }
    });
  });

  $$('.admin-alert').forEach(alert => {
    window.setTimeout(() => {
      alert.style.opacity = '0';
      alert.style.transition = 'opacity .3s ease';
      window.setTimeout(() => alert.remove(), 300);
    }, 5000);
  });
}
