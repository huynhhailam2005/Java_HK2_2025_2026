# SRPM Project - Hệ thống Quản lý Dự án Sinh viên

---
## 1. Yêu cầu hệ thống
Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt các công cụ sau:

* **Java:** tải JDK 17 trở lên hoặc dùng IntelliJ IDEA .
* **Node.js:** https://nodejs.org/en/download .
* **Cơ sở dữ liệu:** PostgreSQL 18 https://sbp.enterprisedb.com/getfile.jsp?fileid=1260118 .
* **IDE:** IntelliJ IDEA .
* **Postman:** test API .

---

## 2. Cấu trúc dự án
Dự án bao gồm hai phần chính nằm trong cùng một Repository:

* `/backend`: Mã nguồn Spring Boot (Xử lý Logic & API).
* `/frontend`: Mã nguồn React + Vite + Tailwind CSS (Giao diện người dùng).

---

## 3. Hướng dẫn cài đặt (Setup)

### A. Thiết lập Backend (Java Spring Boot)

#### 1. Cài đặt PostgreSQL
Để đảm bảo code chạy ngay không cần sửa cấu hình, yêu cầu cả nhóm cài đặt đúng thông số sau:
* **Quá trình cài đặt:**
    * **Password:** Trong lúc cài đặt, hệ thống sẽ yêu cầu đặt mật khẩu cho tài khoản `postgres`. Hãy đặt là: `123456` (để đồng bộ với dự án).
    * **Port:** Giữ nguyên cổng mặc định là: `5432`.
    * **Thành phần:** Hãy đảm bảo đã tích chọn cài đặt **pgAdmin 4** (công cụ quản lý giao diện).

#### 2. Khởi tạo Database
1.  Mở **pgAdmin 4** (đã cài ở bước trên).
2.  Chuột phải vào mục **Databases** -> **Create** -> **Database...**
3.  Nhập tên Database chính xác là: `srpm_db`. Sau đó nhấn **Save**.

#### 4. Khởi chạy
* Tìm file `SrpmApplication.java`, chuột phải và chọn **Run**.
* Nếu Console hiện `Started SrpmApplication...`, server đã sẵn sàng tại: `http://localhost:8080`.

### B. Thiết lập Frontend (React + Vite)
1.  **Mở thư mục:** Dùng Terminal (hoặc VS Code) truy cập vào thư mục `frontend`:
    ```bash
    cd frontend
    ```
2.  **Cài đặt thư viện:** 
    ```bash
    npm install
    ```
3.  **Khởi chạy:**
    ```bash
    npm run dev
    ```
4.  **Truy cập:** Mở trình duyệt vào địa chỉ `http://localhost:5173`.

---

## 4. Quy trình làm việc với Git
Để quản lý mã nguồn hiệu quả và tránh xung đột (Conflict), yêu cầu cả nhóm tuân thủ:

1.  **Cập nhật code:** Luôn chạy `git pull origin main` trước khi bắt đầu làm việc để lấy code mới nhất.
2.  **Tạo nhánh mới:** Không code trực tiếp trên nhánh `main`. Hãy tạo nhánh riêng cho mỗi tính năng:
    ```bash
    git checkout -b feature/ten-tinh-nang
    ```
3.  **Quy tắc đặt tên Commit:**
    Tất cả thành viên phải viết commit theo cấu trúc: `[Phạm vi] <loại>: <mô tả>`

### Phạm vi (Scope):
* **`[BE]`**: Thay đổi trong thư mục backend.
* **`[FE]`**: Thay đổi trong thư mục frontend.
* **`[DOCS]`**: Cập nhật tài liệu (README, hướng dẫn).
* **`[CONFIG]`**: Thay đổi file cấu hình (.gitignore, cấu hình dự án).

### Loại (Type):
* **`feat`**: Thêm chức năng mới.
* **`fix`**: Sửa lỗi (bug).
* **`docs`**: Cập nhật tài liệu.
* **`style`**: Thay đổi giao diện, CSS (không ảnh hưởng logic).
* **`refactor`**: Sửa code nhưng không đổi chức năng.
* **`chore`**: Cài đặt thư viện, cập nhật cấu hình lặt vặt.

### Ví dụ thực tế:
| Tình huống | Câu lệnh Commit mẫu |
| :--- | :--- |
| Thêm trang Đăng nhập | `git commit -m "[FE] feat: thiết kế giao diện trang login"` |
| Sửa lỗi kết nối DB | `git commit -m "[BE] fix: sửa lỗi kết nối database khi lưu user"` |
| Cài thư viện Axios | `git commit -m "[FE] chore: cài đặt thư viện axios"` |
| Cập nhật README | `git commit -m "[DOCS] docs: cập nhật quy tắc commit"` |

4.  **4. Quy trình làm việc với Git**.
    Để tránh xung đột code, mọi thành viên **bắt buộc** tuân thủ quy trình sau khi làm chức năng mới:

* **Bước 1 (Cập nhật):** `git checkout main` -> `git pull origin main` để lấy code mới nhất. (nếu thêm chức năng mới).
* **Bước 2 (Tạo nhánh):** Tạo nhánh riêng cho tính năng: `git checkout -b feature/ten-chuc-nang` (VD: `feature/login`).
* **Bước 3 (Code & Commit):** Thực hiện code và commit thường xuyên theo quy tắc ở mục 5.
* **Bước 4 (Push):** Đẩy nhánh lên GitHub: `git push origin feature/ten-chuc-nang`.
* **Bước 5 (Pull Request):** Lên GitHub, nhấn **Compare & Pull Request**, mô tả những gì đã làm và gán Nhóm trưởng vào phần **Reviewers**.
* **Bước 6 (Merge):** Sau khi Leader review và duyệt, code sẽ được gộp vào nhánh `main`. Bạn tiến hành xóa nhánh ở máy cục bộ.

---

## Lưu ý chung
* **Lỗi Maven:** Nếu IntelliJ không nhận thư viện, chuột phải vào file `pom.xml` -> **Maven** -> **Reload Project**.
* **Lỗi Node:** Nếu cài thư viện bị lỗi, hãy xóa thư mục `node_modules` và chạy lại `npm install`.