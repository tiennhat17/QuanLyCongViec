# Theo dõi module ADMIN

Lần kiểm tra gần nhất: 2026-10-06

## Trạng thái hiện tại

| Module | Trạng thái | Ghi chú |
|---|---|---|
| Xem/tìm kiếm người dùng | 🟡 Hoàn thành một phần | Đã có danh sách, tìm kiếm theo username/email, phân trang 10 user/trang, vai trò, trạng thái và ngày đăng ký. Số lượng Task được tạm hoãn vì phụ thuộc module Task do teammate khác phụ trách. |
| Khóa/mở khóa tài khoản | ✅ Hoàn thành | Admin có thể khóa hoặc mở khóa tài khoản khác ngay trên danh sách người dùng. Chức năng được bảo vệ bởi role ADMIN và có CSRF. |
| Ngăn Admin tự khóa tài khoản | ✅ Hoàn thành | Admin không thể tự khóa tài khoản của chính mình. Việc tự mở khóa hiện vẫn được service cho phép nếu tài khoản đang bị khóa. |
| Xử lý tài khoản bị khóa | ✅ Hoàn thành | Tài khoản bị khóa không thể đăng nhập. Nếu đang có session, Security Context bị xóa, session bị hủy ở request tiếp theo và user được chuyển về trang đăng nhập kèm thông báo. |
| Dashboard Admin | ✅ Hoàn thành phần user | Đã có tổng số tài khoản, số đang hoạt động, số bị khóa và 5 user đăng ký gần đây. Thống kê Task/Category được tạm hoãn cho đến khi các module tương ứng tồn tại. |

## Luồng khóa tài khoản đã hoàn thành

- `POST /admin/users/{id}/status` cập nhật trạng thái tài khoản mục tiêu.
- Danh sách user hiển thị nút thao tác phù hợp với trạng thái hiện tại.
- Hiển thị thông báo thành công hoặc lỗi sau khi chuyển hướng về danh sách.
- Giữ lại bộ lọc tìm kiếm sau khi thực hiện thao tác.
- Danh sách được phân trang với 10 user mỗi trang; bộ lọc tìm kiếm được giữ khi chuyển trang.
- Việc khóa tài khoản không xóa dữ liệu của user, chỉ thay đổi trạng thái tài khoản.
- User bị khóa nhận được thông báo `Tài khoản đã bị khóa` khi đăng nhập.
- Session đã xác thực của user bị khóa sẽ bị vô hiệu hóa ở request tiếp theo.

## Kiểm chứng

Lệnh đã chạy:

```powershell
.\mvnw.cmd -B verify
```

Kết quả gần nhất:

```text
Tests run: 23
Failures: 0
Errors: 0
BUILD SUCCESS
```

Các test liên quan đến ADMIN/tài khoản đã có:

- Admin có thể xem và tìm kiếm user.
- User thường không thể truy cập `/admin/users`.
- Admin có thể khóa và mở khóa user khác.
- Admin không thể tự khóa tài khoản của chính mình. Việc tự mở khóa hiện
  vẫn được service cho phép nếu tài khoản đang bị khóa.
- User bị khóa không thể đăng nhập.
- Session của user bị khóa bị vô hiệu hóa ở request tiếp theo.

### Kiểm thử riêng theo từng lớp

- `AdminControllerTest`: 4 test cho phân trang, giữ bộ lọc/trang khi khóa
  tài khoản, thông báo lỗi và đưa thống kê dashboard vào model.
- `UserServiceAdminTest`: 6 test cho tìm kiếm có trim, khóa, mở khóa,
  chặn tự khóa, user không tồn tại và tính thống kê user.
- `UserRepositoryAdminTest`: 4 test với H2 cho tìm kiếm không phân biệt hoa
  thường, phân trang 10 user/trang, phân trang khi tìm kiếm có hơn 10 kết quả
  và các truy vấn thống kê user.
- `TaskManagerApiApplicationTests`: 9 test tích hợp cho phân quyền, luồng
  khóa/mở khóa, đăng nhập tài khoản bị khóa, session bị vô hiệu hóa, phân
  trang và dashboard user qua HTTP.

Tổng số test ADMIN hiện tại: **23 test**.

Kết quả kiểm thử gần nhất:

```text
Tests run: 23
Failures: 0
Errors: 0
BUILD SUCCESS
```

### Kiểm thử dashboard user

- Controller kiểm tra việc gọi service và đưa thống kê vào model.
- Service kiểm tra tổng số user, số user hoạt động, số user bị khóa và danh
  sách 5 user đăng ký gần đây.
- Repository kiểm tra các truy vấn đếm theo trạng thái và giới hạn 5 user
  đăng ký gần đây.
- Test tích hợp kiểm tra ADMIN truy cập được `/admin` và nhận model thống kê.

## Cập nhật giao diện và trải nghiệm ADMIN

- Đã chuyển các tab chức năng sang sidebar bên trái, gồm Dashboard, quản trị
  người dùng và mục quản trị công việc đang ở trạng thái `Sắp có`.
- Dashboard và danh sách người dùng dùng chung dark theme với module
  login/register.
- Đã bổ sung JavaScript dùng chung cho ADMIN:
  xác nhận trước khi khóa/mở khóa, vô hiệu hóa nút trong lúc xử lý và tự ẩn
  thông báo sau 5 giây.
- Nút `Khóa tài khoản` và `Mở khóa` có cùng kích thước.
- Nút tìm kiếm và xóa bộ lọc được căn chỉnh cùng hàng với ô nhập.
- Đã bỏ dòng hiển thị tổng số tài khoản khỏi trang `/admin/users`.

## Định dạng ngày và múi giờ đăng ký

- `createdAt` được hiển thị theo định dạng `dd/MM/yyyy HH:mm`.
- Form đăng ký gửi múi giờ của trình duyệt thông qua `clientTimezoneOffset`.
- Backend tạo thời điểm đăng ký theo múi giờ client khi tài khoản được tạo.
- Các tài khoản cũ vẫn giữ thời điểm đã lưu trước khi thay đổi này.

## Công việc ADMIN còn lại, không cần chờ Task/Category

- Không có việc cần làm

## Các phần cố tình tạm hoãn

- Số lượng Task của từng user.
- Thống kê Task.
- Thống kê Category.

Các chức năng này cần module Task/Category và chỉ nên triển khai sau khi có entity cùng các quan hệ dữ liệu tương ứng.
