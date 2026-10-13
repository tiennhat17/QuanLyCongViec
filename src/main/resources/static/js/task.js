const $ = selector => document.querySelector(selector);
const $$ = selector => document.querySelectorAll(selector);

// ACCOUNT MENU
const userMenuTrigger = $('#userMenuTrigger');
const accountMenu = $('#accountMenu');

if (userMenuTrigger && accountMenu) {

    userMenuTrigger.addEventListener('click', event => {

        event.stopPropagation();

        accountMenu.classList.toggle('show');
    });

    accountMenu.addEventListener('click', event => {
        event.stopPropagation();
    });
}

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


/* =========================================================
   CLOSE POPUPS WHEN CLICK OUTSIDE
   ========================================================= */

document.addEventListener('click', () => {

    if (accountMenu) {
        accountMenu.classList.remove('show');
    }

    $$('.context-menu').forEach(menu => {
        menu.remove();
    });
});


// MODAL
const openModal = modal => {
    if (modal) {
        modal.classList.remove('hidden');
    }
};

const closeModal = modal => {
    if (modal) {
        modal.classList.add('hidden');
    }

};


// Modal: bấm nền hoặc nút Hủy để đóng
$$('.modal').forEach(modal => {

    modal.addEventListener('click', event => {

        if (event.target === modal) {
            closeModal(modal);
        }
    });

    modal.querySelectorAll('[data-close]').forEach(button => {

        button.addEventListener('click', () => {
            closeModal(modal);
        });

    });
});

// CATEGORY
const catModal = $('#catModal');
const catForm = $('#catForm');
const catName = $('#catName');

function openCategoryModal() {

    if (!catModal) {
        return;
    }
    openModal(catModal);

    if (catName) {
        catName.focus();
    }
}


// Thêm Category
const addCategory = $('#addCategory');

if (addCategory) {

    addCategory.addEventListener('click', () => {

        if (catForm) {
            catForm.action = `${ctx}/categories`;
        }
        $('#cId').value = '';
        catName.value = ''; 

        if (catModal) {
            catModal.querySelectorAll('.invalid-feedback')
                    .forEach(error => {
                        error.textContent = '';
                    });
        }


        openCategoryModal();
    });
}


// Sửa / Xóa Category
$$('.category-more').forEach(button => {

    button.addEventListener('click', event => {

        event.stopPropagation();

        $$('.context-menu').forEach(menu => {
            menu.remove();
        });

        const id = button.dataset.id;
        const name = button.dataset.name;

        const menu = document.createElement('div');

        menu.className = 'context-menu';

        menu.innerHTML = `
            <button class="edit">
                ✏ Sửa
            </button>

            <button class="delete">
                🗑 Xóa
            </button>
        `;

        menu.addEventListener('click', event => {
            event.stopPropagation();
        });


        // EDIT
        menu.querySelector('.edit')
            .addEventListener('click', () => {

                menu.remove();

                if (catForm) {

                    catForm.action =
                        `${ctx}/categories`;

                    $('#cId').value = id;
                    catName.value = name;
                }

                openCategoryModal();
            });


        // DELETE
        menu.querySelector('.delete')
            .addEventListener('click', () => {

                if (
                    !confirm(
                        'Xóa danh mục này và toàn bộ công việc trong đó?'
                    )
                ) {
                    return;
                }

                const form = $('#delCatForm');

                if (form) {

                    form.action =
                        `${ctx}/categories/${id}/delete`;

                    form.submit();
                }

            });

        button.closest('li').appendChild(menu);
    });
});


// TASK MODAL
const taskModal = $('#taskModal');

function openTask(task) {
    if (!taskModal) {
        return;
    }

    $('#taskModalTitle').textContent = task ? 'Sửa công việc' : 'Thêm công việc';

    if ($('#tId')) {
        $('#tId').value = task ? task.id : '';
    }

    if ($('#tTitle')) {
        $('#tTitle').value = task ? task.title : '';
    }

    if ($('#tDesc')) {
        $('#tDesc').value = task ? task.desc : '';
    }

    if ($('#tDue')) {
        $('#tDue').value = task ? task.due : '';
    }

    if ($('#tCat')) {
        $('#tCat').value = task ? task.cat: $('#tCat').dataset.def;
    }

    openModal(taskModal);

    if ($('#tTitle')) {
        $('#tTitle').focus();
    }
}


const addTask = $('#addTask');

if (addTask) {
    addTask.addEventListener('click', () => {
        openTask(null);
    });
}


const editTask = $('#editTask');

if (editTask) {
    editTask.addEventListener('click', event => {
        openTask(event.currentTarget.dataset);
    });
}
