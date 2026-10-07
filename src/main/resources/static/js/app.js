const $ = s => document.querySelector(s), $$ = s => document.querySelectorAll(s);
const openM = m => m.classList.remove('hidden'), closeM = m => m.classList.add('hidden');

// Menu tài khoản + đóng popup khi bấm ra ngoài
const um = $('#userMenuTrigger'), am = $('#accountMenu');
if (um && am) {
  um.onclick = e => { e.stopPropagation(); am.classList.toggle('show'); };
  am.onclick = e => e.stopPropagation();
}
document.addEventListener('click', () => {
  if (am) am.classList.remove('show');
  $$('.context-menu').forEach(m => m.remove());
});

// Modal: bấm nền hoặc nút Hủy để đóng
$$('.modal').forEach(m => {
  m.onclick = e => { if (e.target === m) closeM(m); };
  m.querySelectorAll('[data-close]').forEach(b => b.onclick = () => closeM(m));
});

// Tìm kiếm tức thời (lọc phía client như bản gốc)
const filter = (input, sel) => {
  const field = $(input);
  if (!field) return;
  field.oninput = e => {
    const k = e.target.value.toLowerCase();
    $$(sel).forEach(el => el.style.display = el.textContent.toLowerCase().includes(k) ? '' : 'none');
  };
};
filter('#categorySearch', '#categoryList li');
filter('#taskSearch', '.task');

// Danh mục: thêm / sửa / xóa
const catModal = $('#catModal'), catForm = $('#catForm'), catName = $('#catName');
function openCat(action, title, name) {
  catForm.action = action; $('#catTitle').textContent = title; catName.value = name;
  openM(catModal); catName.focus();
}
const addCategory = $('#addCategory');
if (addCategory && catForm && catName && catModal) {
  addCategory.onclick = () => openCat(ctx + '/categories', 'Thêm danh mục', '');
}
$$('.category-more').forEach(b => b.onclick = e => {
  e.stopPropagation();
  $$('.context-menu').forEach(m => m.remove());
  const { id, name } = b.dataset;
  const m = document.createElement('div');
  m.className = 'context-menu';
  m.innerHTML = '<button class="edit">✏ Sửa</button><button class="delete">🗑 Xóa</button>';
  m.onclick = ev => ev.stopPropagation();
  m.querySelector('.edit').onclick = () => { m.remove(); openCat(ctx + '/categories/' + id + '/edit', 'Sửa danh mục', name); };
  m.querySelector('.delete').onclick = () => {
    if (confirm('Xóa danh mục này và toàn bộ công việc trong đó?')) {
      const f = $('#delCatForm'); f.action = ctx + '/categories/' + id + '/delete'; f.submit();
    }
  };
  b.closest('li').appendChild(m);
});

// Công việc: thêm / sửa
const taskModal = $('#taskModal');
function openTask(t) {
  $('#taskModalTitle').textContent = t ? 'Sửa công việc' : 'Thêm công việc';
  $('#tId').value = t ? t.id : '';
  $('#tTitle').value = t ? t.title : '';
  $('#tDesc').value = t ? t.desc : '';
  $('#tDue').value = t ? t.due : '';
  $('#tCat').value = t ? t.cat : $('#tCat').dataset.def;
  openM(taskModal); $('#tTitle').focus();
}
if ($('#addTask')) $('#addTask').onclick = () => openTask(null);
if ($('#editTask')) $('#editTask').onclick = e => openTask(e.currentTarget.dataset);

const registerForm = document.querySelector('form[action$="/register"]');
if (registerForm) {
  const timezoneField = registerForm.querySelector('#clientTimezoneOffset');
  if (timezoneField) timezoneField.value = new Date().getTimezoneOffset();
}

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
