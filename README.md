-Truy cập trực tiếp trang đăng ký test tại: https://sandbox.vnpayment.vn/devreg/

-Mở Email vừa dùng để đăng ký, tìm thư kích hoạt từ VNPay và nhấn vào đường link xác thực tài khoản.





# Payment VNPay Service (Docker & Docker Compose)

Hệ thống tích hợp cổng thanh toán **VNPay** xây dựng trên nền tảng **Spring Boot 4 / Java 21** kết hợp **MySQL 8.0** và **phpMyAdmin**, được đóng gói hoàn chỉnh bằng **Docker & Docker Compose**.

---

## 🚀 Danh Sách Service & Cổng Truy Cập (Docker Host)

| Service | Container Name | Host Port | Internal Port | Mô Tả |
| :--- | :--- | :--- | :--- | :--- |
| **payment-vnpay-app** | `payment-vnpay-app` | **`8080`** | `8080` | Spring Boot Application (REST API VNPay) |
| **payment-vnpay-mysql** | `payment-vnpay-mysql` | **`3308`** | `3306` | MySQL 8.0 Database (Host 3308 tránh xung đột với MySQL local) |
| **payment-vnpay-phpmyadmin** | `payment-vnpay-phpmyadmin` | **`8085`** | `80` | Web UI quản trị cơ sở dữ liệu MySQL |

---

## 📁 Cấu Trúc Thư Mục Triển Khai

```text
payment-VNPay/
├── Dockerfile                   # Multi-stage build tối ưu cho Java 21
├── docker-compose.yml           # Compose file điều phối full 3 service
├── .dockerignore                # Loại trừ target, file tạm khi build Docker
├── .env                         # File cấu hình biến môi trường chính thức
├── .env.example                 # File mẫu biến môi trường tham khảo
├── init-db/
│   └── 01-init.sql              # Script SQL tự động khởi tạo DB & UTF8MB4
├── pom.xml                      # Cấu hình Maven dependencies
└── src/                         # Mã nguồn Spring Boot
    └── main/
        └── resources/
            └── application.properties # Cấu hình Spring Boot hỗ trợ ENV fallback
```

---

## ⚙️ Cấu Hình Biến Môi Trường (ENV)

Tất cả các biến môi trường được đặt tập trung trong file `.env`:

```ini
# Cổng ứng dụng trên máy Host
SERVER_PORT=8080

# Cấu hình Cơ sở dữ liệu MySQL
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=payment_vnpay_db
MYSQL_USER=vnpay_user
MYSQL_PASSWORD=vnpay_password
MYSQL_LOCAL_PORT=3308

# Cấu hình JPA / Hibernate
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true

# Cấu hình Cổng thanh toán VNPay Sandbox
# (Thay thế bằng thông tin thực tế từ VNPay Merchant khi triển khai thật)
VNPAY_TMN_CODE=DEMOVNPAY
VNPAY_HASH_SECRET=DEMOHASHSECRETKEY
VNPAY_PAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_RETURN_URL=http://localhost:8080/api/payment/vnpay-return
VNPAY_IPN_URL=http://localhost:8080/api/payment/vnpay-ipn

# Cổng truy cập phpMyAdmin
PMA_PORT=8085
```

---

## 🛠 Hướng Dẫn Vận Hành

### 1. Khởi động toàn bộ Services
```bash
docker compose up -d --build
```

### 2. Kiểm tra trạng thái các container
```bash
docker compose ps
```
Cả 3 container sẽ ở trạng thái `running` (với `payment-vnpay-mysql` ở trạng thái `healthy`).

### 3. Xem log hoạt động theo thời gian thực
```bash
# Xem log app Spring Boot
docker compose logs -f payment-vnpay-app

# Xem log toàn bộ services
docker compose logs -f
```

### 4. Dừng hệ thống
```bash
docker compose down
```

---

## 🧪 Kiểm Thử API Thanh Toán (Endpoints)

### 1. Tạo liên kết thanh toán VNPay (Create Payment URL)
* **Method**: `GET`
* **URL**: `http://localhost:8080/api/payment/create?orderCode=ORDER_TEST_001&amount=500000`
* **Curl**:
```bash
curl "http://localhost:8080/api/payment/create?orderCode=ORDER_TEST_001&amount=500000"
```
* **Kết quả**: Trả về URL thanh toán VNPay Sandbox có chứa checksum `vnp_SecureHash`. Copy URL này dán lên trình duyệt để thanh toán thử nghiệm.

### 2. Return URL (Khách hàng redirect về sau khi thanh toán)
* **Method**: `GET`
* **URL**: `http://localhost:8080/api/payment/vnpay-return`
* **Mô tả**: VNPay redirect người dùng về đây kèm các tham số giao dịch (`vnp_ResponseCode`, `vnp_TxnRef`,...).

### 3. IPN URL (Server VNPay gọi webhook ngầm)
* **Method**: `GET`
* **URL**: `http://localhost:8080/api/payment/vnpay-ipn`
* **Mô tả**: VNPay gửi thông tin kết quả giao dịch ngầm, hệ thống sẽ xác thực và tự động cập nhật trạng thái đơn hàng (`SUCCESS` / `FAILED`) vào bảng `payment_transactions`.

---

## 🗄 Quản Lý Database Qua phpMyAdmin

* Truy cập trình duyệt: **`http://localhost:8085`**
* Đăng nhập với thông tin:
  * **Server**: `payment-vnpay-mysql`
  * **Username**: `vnpay_user` (hoặc `root`)
  * **Password**: `vnpay_password` (hoặc `root`)
* Database: `payment_vnpay_db`
* Bảng dữ liệu giao dịch: `payment_transactions`
