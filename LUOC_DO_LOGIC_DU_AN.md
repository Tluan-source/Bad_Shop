# LƯỢC ĐỒ LOGIC HỆ THỐNG BADMINTON MARKETPLACE

## 1. KIẾN TRÚC TỔNG QUAN HỆ THỐNG

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   Browser    │  │   Mobile     │  │  API Client  │         │
│  │  (Thymeleaf) │  │   WebView    │  │   (Future)   │         │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘         │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
                    ┌────────▼────────┐
                    │  SPRING BOOT    │
                    │   APPLICATION   │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼────────┐  ┌────────▼────────┐  ┌───────▼────────┐
│   CONTROLLER   │  │     SERVICE     │  │   REPOSITORY   │
│     LAYER      │  │     LAYER       │  │     LAYER      │
├────────────────┤  ├─────────────────┤  ├────────────────┤
│ - HomeController│  │ - ProductService│  │ - ProductRepo  │
│ - AuthController│  │ - OrderService │  │ - OrderRepo    │
│ - CartController│  │ - CartService  │  │ - UserRepo     │
│ - VendorController│ │ - AiChatService│  │ - CategoryRepo │
│ - AdminController│ │ - ChatService  │  │ - ...          │
│ - ...           │  │ - ...          │  │                │
└───────┬────────┘  └────────┬────────┘  └───────┬────────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
                    ┌────────▼────────┐
                    │  SQL SERVER     │
                    │   DATABASE      │
                    └─────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼────────┐  ┌────────▼────────┐  ┌───────▼────────┐
│   CLOUDINARY   │  │     VNPAY       │  │   OPENROUTER   │
│  (Image Storage)│  │   (Payment)    │  │   (AI Model)   │
└────────────────┘  └─────────────────┘  └────────────────┘
```

## 2. LUỒNG XÁC THỰC (AUTHENTICATION FLOW)

```
┌─────────┐
│  USER   │
└────┬────┘
     │
     │ [GET /register] ──────────────────────────┐
     │                                           │
     ▼                                           ▼
┌──────────┐                            ┌──────────────┐
│Register  │                            │ AuthController│
│ Form     │                            │              │
└────┬─────┘                            └──────┬───────┘
     │                                          │
     │ [POST /register]                         │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│  UserService │                                │
│  registerUser│                                │
└────┬─────┘                                    │
     │                                          │
     │ Tạo user với status INACTIVE             │
     │ Hash password                            │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│  OtpService│                                  │
│  createOtp│                                   │
└────┬─────┘                                    │
     │                                          │
     │ Gửi email OTP                            │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│Verify OTP│                                    │
│   Form   │                                    │
└────┬─────┘                                    │
     │                                          │
     │ [POST /verify-otp]                       │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│ OtpService│                                   │
│ verifyOtp│                                    │
└────┬─────┘                                    │
     │                                          │
     │ Đúng OTP?                                │
     │     │                                    │
     │  ┌──┴──┐                                │
     │  │ YES │  NO                            │
     │  │     │                                │
     ▼  ▼     ▼                                │
┌──────────┐ ┌──────────┐                      │
│Set status│ │Show error│                      │
│ACTIVE    │ │          │                      │
└────┬─────┘ └──────────┘                      │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│ Login    │                                    │
│  Form    │                                    │
└────┬─────┘                                    │
     │                                          │
     │ [POST /login]                            │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│Spring    │                                    │
│Security  │                                    │
│Form Login│                                    │
└────┬─────┘                                    │
     │                                          │
     │ CustomUserDetailsService                 │
     │ Load user từ DB                          │
     │ Verify password                          │
     │                                          │
     │ Đúng?                                    │
     │   │                                      │
     │ ┌─┴─┐                                    │
     │ │YES│ NO                                 │
     │ │   │                                    │
     ▼ ▼   ▼                                    │
┌──────────┐ ┌──────────┐                      │
│Success   │ │Failure   │                      │
│Handler   │ │Redirect  │                      │
│          │ │/login?   │                      │
│          │ │error=true│                      │
└────┬─────┘ └──────────┘                      │
     │                                          │
     │ Redirect dựa trên role:                  │
     │ - ADMIN → /admin/dashboard               │
     │ - VENDOR → /vendor/dashboard             │
     │ - SHIPPER → /shipper/dashboard           │
     │ - USER → /                               │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│Dashboard │                                    │
│ theo role│                                    │
└──────────┘                                    │
```

## 3. LUỒNG MUA HÀNG (SHOPPING FLOW)

```
┌─────────┐
│  USER   │
└────┬────┘
     │
     │ [GET /] ────────────────────────────────┐
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│Home      │                                    │
│Controller│                                    │
└────┬─────┘                                    │
     │                                          │
     │ Lấy top 20 sản phẩm bán chạy             │
     │ Lấy danh mục                             │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│Home Page │                                    │
│(index)   │                                    │
└────┬─────┘                                    │
     │                                          │
     │ [Click sản phẩm]                         │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│Product   │                                    │
│Detail    │                                    │
│Page      │                                    │
└────┬─────┘                                    │
     │                                          │
     │ ┌────────────────────────────────────┐  │
     │ │ [Thêm vào giỏ hàng]                │  │
     │ │                                    │  │
     │ ▼                                    │  │
     │ ┌──────────┐                         │  │
     │ │Cart      │                         │  │
     │ │Controller│                         │  │
     │ │/cart/add │                         │  │
     │ └────┬─────┘                         │  │
     │      │                               │  │
     │      ▼                               │  │
     │ ┌──────────┐                         │  │
     │ │CartService│                        │  │
     │ │addToCart │                         │  │
     │ └────┬─────┘                         │  │
     │      │                               │  │
     │      │ Tạo/Update CartItem           │  │
     │      │                               │  │
     │      ▼                               │  │
     │ ┌──────────┐                         │  │
     │ │ Cart DB  │                         │  │
     │ └──────────┘                         │  │
     │                                      │  │
     │ └────────────────────────────────────┘  │
     │                                          │
     │ [Xem giỏ hàng]                           │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│  Cart    │                                    │
│   Page   │                                    │
└────┬─────┘                                    │
     │                                          │
     │ [Thanh toán]                             │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│Checkout  │                                    │
│Controller│                                    │
│/checkout │                                    │
└────┬─────┘                                    │
     │                                          │
     │ Lấy cart items                           │
     │ Nhóm theo store                          │
     │ Lấy địa chỉ user                         │
     │ Lấy vouchers, promotions                 │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│Checkout  │                                    │
│   Page   │                                    │
└────┬─────┘                                    │
     │                                          │
     │ [Đặt hàng]                               │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│Checkout  │                                    │
│Controller│                                    │
│placeOrder│                                    │
└────┬─────┘                                    │
     │                                          │
     │ Validate thông tin                       │
     │ Áp dụng voucher/promotion                │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│Order     │                                    │
│Service   │                                    │
│createMulti│                                   │
│StoreOrders│                                   │
└────┬─────┘                                    │
     │                                          │
     │ Tạo Order cho mỗi store                  │
     │ Tạo OrderItems                           │
     │ Tính phí vận chuyển                      │
     │ Trừ số lượng sản phẩm                    │
     │                                          │
     ▼                                          │
┌──────────┐                                    │
│ Payment  │                                    │
│   Type?  │                                    │
│   │      │                                    │
│ ┌─┴───┐  │                                    │
│ │COD  │  │                                    │
│ │VNPAY│  │                                    │
│ │BANK │  │                                    │
│ │QR   │  │                                    │
│ └─┬───┘  │                                    │
│   │      │                                    │
│   ▼      │                                    │
┌──────────┐│                                    │
│ Payment  ││                                    │
│Processing││                                    │
└────┬─────┘│                                    │
     │      │                                    │
     ▼      │                                    │
┌──────────┐│                                    │
│ Order    ││                                    │
│Success   ││                                    │
│  Page    ││                                    │
└──────────┘│                                    │
     │      │                                    │
     │      │                                    │
     ▼      │                                    │
┌──────────┐                                    │
│ Order    │                                    │
│ Tracking │                                    │
│  Page    │                                    │
└──────────┘                                    │
```

## 4. LUỒNG AI CHAT (DB-FIRST STRATEGY)

```
┌─────────┐
│  USER   │
└────┬────┘
     │
     │ [Gõ câu hỏi: "vợt"]
     │
     ▼
┌──────────┐
│ Footer   │
│ Widget   │
│ (JS)     │
└────┬─────┘
     │
     │ POST /api/ai-chat
     │ { "message": "vợt" }
     │
     ▼
┌──────────┐
│AiChat    │
│Controller│
└────┬─────┘
     │
     ▼
┌──────────┐
│AiChat    │
│Service   │
│.chat()   │
└────┬─────┘
     │
     │ Bước 1: Infer Category từ message
     │
     ▼
┌──────────┐
│inferCategoryId│
│          │
│ - Tìm từ khóa "vợt"                        │
│ - Query CategoryRepository                  │
│ - Tìm category có name chứa "vợt"          │
│ - Trả về category ID thực tế               │
└────┬─────┘
     │
     │ Bước 2: Infer Budget (nếu có)
     │
     ▼
┌──────────┐
│inferMaxPrice│
│          │
│ - Parse "50k", "50000" từ message          │
│ - Trả về BigDecimal                        │
└────┬─────┘
     │
     │ Bước 3: DB-FIRST - Tìm sản phẩm
     │
     ▼
┌──────────┐
│tryDbFirst│
│Suggestion│
└────┬─────┘
     │
     │ Có budget?
     │   │
     │ ┌─┴──┐
     │ │YES │ NO
     │ │    │
     │ ▼    ▼
     │ ┌──────────┐  ┌──────────────────┐
     │ │Tìm theo  │  │Lấy top sản phẩm  │
     │ │budget    │  │trong category    │
     │ │          │  │                  │
     │ │- Under   │  │- Sắp xếp theo    │
     │ │  budget  │  │  sold (bán chạy) │
     │ │- Nearest │  │- Sắp xếp theo    │
     │ │  ±20%    │  │  rating          │
     │ │          │  │- Lấy top 5       │
     │ └────┬─────┘  └──────┬───────────┘
     │      │               │
     │      └───────┬───────┘
     │              │
     │              │ Tìm thấy sản phẩm?
     │              │   │
     │              │ ┌─┴──┐
     │              │ │YES │ NO
     │              │ │    │
     │              ▼ ▼    ▼
     │       ┌──────────┐ ┌──────────┐
     │       │Format    │ │Return    │
     │       │Response  │ │null →    │
     │       │          │ │Call AI   │
     │       │- Tên SP  │ └────┬─────┘
     │       │- Giá     │      │
     │       │- Rating  │      │
     │       │- Link    │      │
     │       └────┬─────┘      │
     │            │            │
     │            └──────┬─────┘
     │                   │
     ▼                   │
┌──────────┐             │
│ Return   │             │
│ Response │◄────────────┘
│          │             │
│ "Chào bạn! Dưới đây│   │
│  là các vợt đang    │   │
│  bán chạy..."       │   │
└────┬─────┘           │
     │                 │
     │ [Nếu null]      │
     │                 │
     ▼                 │
┌──────────┐           │
│ Build    │           │
│ AI Prompt│           │
│          │           │
│ - System │           │
│   prompt │           │
│ - Product│           │
│   context│           │
│   (RAG)  │           │
└────┬─────┘           │
     │                 │
     ▼                 │
┌──────────┐           │
│ WebClient│           │
│ Call     │           │
│ OpenRouter│          │
│ /OpenAI  │           │
└────┬─────┘           │
     │                 │
     │ Retry nếu 429   │
     │ hoặc 5xx        │
     │                 │
     ▼                 │
┌──────────┐           │
│ AI       │           │
│ Response │           │
└────┬─────┘           │
     │                 │
     ▼                 │
┌──────────┐           │
│ Return   │           │
│ Response │           │
└──────────┘           │
     │                 │
     ▼                 │
┌──────────┐           │
│ Display  │           │
│ in Widget│           │
└──────────┘           │
```

## 5. LUỒNG THANH TOÁN (PAYMENT FLOW)

```
┌─────────┐
│  USER   │
└────┬────┘
     │
     │ [Đặt hàng từ Checkout]
     │
     ▼
┌──────────┐
│Checkout  │
│Controller│
│placeOrder│
└────┬─────┘
     │
     │ Tạo Order(s)
     │
     ▼
┌──────────┐
│ Payment  │
│  Method? │
│   │      │
│ ┌─┴────┐ │
│ │ COD  │ │
│ │VNPAY │ │
│ │BANKQR│ │
│ └─┬────┘ │
│   │      │
│   ▼      │
│ ┌────────┴───┐
│ │  [COD]     │
│ │            │
│ │ Return     │
│ │ success    │
│ └────┬───────┘
│      │
│      │ [VNPAY]
│      │
│      ▼
│ ┌──────────┐
│ │VNPay     │
│ │Service   │
│ │create    │
│ │PaymentUrl│
│ └────┬─────┘
│      │
│      │ Build URL với:
│      │ - Amount
│      │ - Order IDs
│      │ - Return URL
│      │
│      ▼
│ ┌──────────┐
│ │ Redirect │
│ │ to VNPay │
│ │ Gateway  │
│ └────┬─────┘
│      │
│      │ User thanh toán
│      │
│      ▼
│ ┌──────────┐
│ │ VNPay    │
│ │ Callback │
│ │ /payment │
│ │ /vnpay   │
│ │ /callback│
│ └────┬─────┘
│      │
│      │ Verify signature
│      │
│      ▼
│ ┌──────────┐
│ │ Payment  │
│ │Service   │
│ │verify    │
│ └────┬─────┘
│      │
│      │ Thành công?
│      │   │
│      │ ┌─┴──┐
│      │ │YES │ NO
│      │ │    │
│      ▼ ▼    ▼
│ ┌──────────┐ ┌──────────┐
│ │Update    │ │Show      │
│ │Order     │ │error     │
│ │Status    │ │          │
│ │PAID      │ │          │
│ └────┬─────┘ └──────────┘
│      │
│      │ [BANK QR]
│      │
│      ▼
│ ┌──────────┐
│ │Generate  │
│ │VietQR    │
│ │Image     │
│ └────┬─────┘
│      │
│      │ Show QR code
│      │ User chuyển khoản
│      │ (Chưa auto verify)
│      │
│      ▼
┌──────────┐
│ Order    │
│ Success  │
│  Page    │
└──────────┘
```

## 6. ENTITY RELATIONSHIP DIAGRAM

```
┌──────────────┐
│     USER     │
│──────────────│
│ id (PK)      │
│ email        │
│ password     │
│ fullName     │
│ role         │
│ status       │
└──────┬───────┘
       │
       │ 1
       │
       │ N
┌──────▼───────────────────┐
│        ORDER             │
│──────────────────────────│
│ id (PK)                  │
│ user_id (FK)             │
│ store_id (FK)            │
│ status                   │
│ amountFromUser           │
│ shippingFee              │
│ shippingProvider_id (FK) │
└──────┬───────────────────┘
       │
       │ 1
       │
       │ N
┌──────▼───────────────────┐
│      ORDER_ITEM          │
│──────────────────────────│
│ id (PK)                  │
│ order_id (FK)            │
│ product_id (FK)          │
│ quantity                 │
│ price                    │
│ styleValueIds            │
└──────┬───────────────────┘
       │
       │ N
       │
       │ 1
┌──────▼───────────────────┐
│       PRODUCT            │
│──────────────────────────│
│ id (PK)                  │
│ category_id (FK)         │
│ store_id (FK)            │
│ name                     │
│ price                    │
│ promotionalPrice         │
│ quantity                 │
│ sold                     │
│ rating                   │
│ isActive                 │
│ isSelling                │
└──────┬───────────────────┘
       │
       │ N         1
       │ ──────────┼─────────┐
       │           │         │
       │           │         │
┌──────▼───────┐ ┌▼──────┐ ┌▼──────────┐
│   CATEGORY   │ │ STORE │ │ CART_ITEM │
│──────────────│ │───────│ │───────────│
│ id (PK)      │ │ id    │ │ id (PK)   │
│ name         │ │ name  │ │ user_id   │
│ isActive     │ │ owner │ │ product_id│
└──────────────┘ └───────┘ │ quantity  │
                          └───────────┘

┌──────────────┐
│   CONVERSATION│
│──────────────│
│ id (PK)      │
│ user_id (FK) │
│ store_id (FK)│
└──────┬───────┘
       │
       │ 1
       │
       │ N
┌──────▼───────┐
│   MESSAGE    │
│──────────────│
│ id (PK)      │
│ conversation_│
│   id (FK)    │
│ sender_id    │
│ content      │
│ senderType   │
│ isRead       │
└──────────────┘

┌──────────────┐
│   REVIEW     │
│──────────────│
│ id (PK)      │
│ product_id   │
│ user_id      │
│ orderItem_id │
│ rating       │
│ comment      │
└──────┬───────┘
       │
       │ 1
       │
       │ N
┌──────▼──────────┐
│  REVIEW_IMAGE   │
│─────────────────│
│ id (PK)         │
│ review_id (FK)  │
│ url             │
└─────────────────┘

┌──────────────┐
│  PROMOTION   │
│──────────────│
│ id (PK)      │
│ store_id (FK)│
│ discountType │
│ discountValue│
│ startDate    │
│ endDate      │
└──────────────┘

┌──────────────┐
│   VOUCHER    │
│──────────────│
│ id (PK)      │
│ code         │
│ discountType │
│ discountValue│
│ minAmount    │
│ maxAmount    │
│ startDate    │
│ endDate      │
└──────────────┘
```

## 7. LUỒNG QUẢN LÝ VENDOR

```
┌─────────┐
│  USER   │
│(Role:   │
│ USER)   │
└────┬────┘
     │
     │ [Đăng ký làm Vendor]
     │
     ▼
┌──────────┐
│Vendor    │
│Controller│
│/register │
└────┬─────┘
     │
     │ Submit form:
     │ - Store name
     │ - Logo
     │ - Business license
     │
     ▼
┌──────────┐
│Vendor    │
│Registration│
│Service   │
└────┬─────┘
     │
     │ Tạo Store với:
     │ - status: PENDING
     │ - User vẫn là USER role
     │
     ▼
┌──────────┐
│ Store    │
│ (PENDING)│
└────┬─────┘
     │
     │ Admin được thông báo
     │
     ▼
┌──────────┐
│ Admin    │
│Controller│
│/vendor   │
│/registrations│
└────┬─────┘
     │
     │ [Phê duyệt]
     │
     ▼
┌──────────┐
│Admin     │
│Service   │
│approve   │
│Vendor    │
└────┬─────┘
     │
     │ Update:
     │ - Store.status = ACTIVE
     │ - User.role = VENDOR
     │ - Gửi email thông báo
     │
     ▼
┌──────────┐
│ Vendor   │
│ Dashboard│
│ Active   │
└────┬─────┘
     │
     │ ┌────────────────────────┐
     │ │ Quản lý sản phẩm      │
     │ │                        │
     │ ▼                        │
     │ ┌──────────┐             │
     │ │Vendor    │             │
     │ │Product   │             │
     │ │Service   │             │
     │ └────┬─────┘             │
     │      │                   │
     │      │ CRUD Products     │
     │      │ - Create          │
     │      │ - Update          │
     │      │ - Delete          │
     │      │ - Upload images   │
     │      │ - Set styles      │
     │      │                   │
     │ └────────────────────────┘
     │
     │ ┌────────────────────────┐
     │ │ Quản lý đơn hàng      │
     │ │                        │
     │ ▼                        │
     │ ┌──────────┐             │
     │ │Vendor    │             │
     │ │Order     │             │
     │ │Service   │             │
     │ └────┬─────┘             │
     │      │                   │
     │      │ - Xem đơn hàng    │
     │      │ - Xác nhận đơn    │
     │      │ - Cập nhật trạng  │
     │      │   thái giao hàng  │
     │      │                   │
     │ └────────────────────────┘
     │
     │ ┌────────────────────────┐
     │ │ Quản lý khuyến mãi    │
     │ │                        │
     │ ▼                        │
     │ ┌──────────┐             │
     │ │Vendor    │             │
     │ │Promotion │             │
     │ │Service   │             │
     │ └────┬─────┘             │
     │      │                   │
     │      │ - Tạo promotion   │
     │      │ - Set discount    │
     │      │ - Áp dụng cho SP  │
     │      │                   │
     │ └────────────────────────┘
     │
     │ ┌────────────────────────┐
     │ │ Thống kê & Doanh thu   │
     │ │                        │
     │ ▼                        │
     │ ┌──────────┐             │
     │ │Vendor    │             │
     │ │Analytics │             │
     │ │Service   │             │
     │ └────┬─────┘             │
     │      │                   │
     │      │ - Doanh thu       │
     │      │ - Số đơn hàng     │
     │      │ - SP bán chạy     │
     │      │                   │
     │ └────────────────────────┘
     │
     ▼
```

## 8. LUỒNG CHAT REALTIME (WEBSOCKET)

```
┌─────────┐                    ┌──────────┐
│  USER   │                    │  VENDOR  │
└────┬────┘                    └────┬─────┘
     │                              │
     │ [Mở trang chat]              │
     │                              │
     ▼                              ▼
┌──────────┐                    ┌──────────┐
│Chat Page │                    │Chat Page │
│(User)    │                    │(Vendor)  │
└────┬─────┘                    └────┬─────┘
     │                              │
     │ WebSocket Connect            │
     │ /ws-chat                     │
     │                              │
     ▼                              ▼
┌──────────┐                    ┌──────────┐
│WebSocket │                    │WebSocket │
│Config    │                    │Config    │
└────┬─────┘                    └────┬─────┘
     │                              │
     │ Subscribe to:                │
     │ - /user/{userId}/queue/      │
     │   notifications              │
     │ - /topic/conversation.{id}   │
     │                              │
     │                              │
     │ [Gửi tin nhắn]               │
     │                              │
     ▼                              ▼
┌──────────┐                    ┌──────────┐
│Chat      │                    │Chat      │
│Controller│                    │Controller│
│@Message  │                    │@Message  │
│Mapping   │                    │Mapping   │
│/chat.send│                    │/chat.send│
└────┬─────┘                    └────┬─────┘
     │                              │
     │ Get/Create Conversation      │
     │                              │
     ▼                              ▼
┌──────────┐                    ┌──────────┐
│Chat      │                    │Chat      │
│Service   │                    │Service   │
│sendMessage│                   │sendMessage│
└────┬─────┘                    └────┬─────┘
     │                              │
     │ Save Message to DB           │
     │                              │
     ▼                              ▼
┌──────────┐                    ┌──────────┐
│ Message  │                    │ Message  │
│   DB     │                    │   DB     │
└────┬─────┘                    └────┬─────┘
     │                              │
     │                              │
     ▼                              ▼
┌──────────┐                    ┌──────────┐
│Simp      │                    │Simp      │
│Messaging │                    │Messaging │
│Template  │                    │Template  │
└────┬─────┘                    └────┬─────┘
     │                              │
     │ Broadcast to:                │
     │ - /topic/conversation.{id}   │
     │ - /user/{recipientId}/queue/ │
     │   notifications              │
     │                              │
     ▼                              ▼
┌──────────┐                    ┌──────────┐
│ Real-time│                    │ Real-time│
│ Display  │                    │ Display  │
│ Message  │                    │ Message  │
└──────────┘                    └──────────┘
```

## 9. PHÂN QUYỀN (ROLE-BASED ACCESS)

```
                    ┌──────────────┐
                    │    USER      │
                    │ (Anonymous)  │
                    └──────┬───────┘
                           │
                           │ Đăng ký/Đăng nhập
                           │
            ┌──────────────┼──────────────┐
            │              │              │
     ┌──────▼──────┐ ┌─────▼──────┐ ┌─────▼──────┐
     │    USER     │ │   VENDOR   │ │   ADMIN    │
     │   ROLE      │ │   ROLE     │ │   ROLE     │
     └──────┬──────┘ └─────┬──────┘ └─────┬──────┘
            │              │              │
     ┌──────┴──────────────┴──────────────┴──────┐
     │                                            │
     │ Quyền truy cập:                            │
     │                                            │
     │ PUBLIC (Không cần đăng nhập):              │
     │ - /, /products, /stores                    │
     │ - /register, /login                        │
     │ - /api/ai-chat                             │
     │                                            │
     │ USER:                                      │
     │ - /user/**, /cart/**, /favorites/**       │
     │ - /orders/**                               │
     │ - /checkout/**                             │
     │ - /api/chat/**, /ws-chat/**                │
     │                                            │
     │ VENDOR:                                    │
     │ - /vendor/** (trừ /register)               │
     │ - Quản lý sản phẩm, đơn hàng               │
     │ - /vendor/chat                             │
     │                                            │
     │ ADMIN:                                     │
     │ - /admin/**                                │
     │ - Quản lý users, stores, categories        │
     │ - Phê duyệt vendor                         │
     │ - Xem tất cả đơn hàng                      │
     │                                            │
     │ SHIPPER:                                   │
     │ - /shipper/**                              │
     │ - Quản lý vận chuyển                       │
     │                                            │
     └────────────────────────────────────────────┘
```

## 10. LUỒNG ĐÁNH GIÁ SẢN PHẨM

```
┌─────────┐
│  USER   │
└────┬────┘
     │
     │ [Xem đơn hàng đã giao]
     │
     ▼
┌──────────┐
│Order     │
│Detail    │
│Page      │
└────┬─────┘
     │
     │ [Click "Đánh giá"]
     │
     ▼
┌──────────┐
│Review    │
│Controller│
│/reviews/ │
│submit-   │
│review    │
└────┬─────┘
     │
     │ Load OrderItems
     │ (chưa được đánh giá)
     │
     ▼
┌──────────┐
│Review    │
│Form      │
│Page      │
└────┬─────┘
     │
     │ Submit:
     │ - Rating (1-5)
     │ - Comment
     │ - Images (tối đa 5)
     │
     ▼
┌──────────┐
│Review    │
│Controller│
│POST      │
│/reviews  │
└────┬─────┘
     │
     │ Validate:
     │ - Rating trong khoảng 1-5
     │ - Comment < 1000 ký tự
     │ - Order đã DELIVERED
     │
     ▼
┌──────────┐
│Review    │
│Service   │
│(trong    │
│Controller)│
└────┬─────┘
     │
     │ Tạo/Update Review
     │
     ▼
┌──────────┐
│Cloudinary│
│Service   │
│(Upload   │
│Images)   │
└────┬─────┘
     │
     │ Lưu ReviewImage
     │
     ▼
┌──────────┐
│Update    │
│Product   │
│Rating    │
│          │
│ - Tính   │
│   rating │
│   trung  │
│   bình   │
│ - Update │
│   product│
│   .rating│
└────┬─────┘
     │
     │ Update Store Rating
     │ (tính từ tất cả products)
     │
     ▼
┌──────────┐
│Redirect  │
│to Order  │
│Detail    │
│with      │
│success   │
│message   │
└──────────┘
```

## 11. LUỒNG XỬ LÝ GIẢM GIÁ (VOUCHER & PROMOTION)

```
┌─────────┐
│  USER   │
└────┬────┘
     │
     │ [Vào trang Checkout]
     │
     ▼
┌──────────┐
│Checkout  │
│Controller│
│GET /     │
│checkout  │
└────┬─────┘
     │
     │ Load:
     │ - Available Vouchers (toàn sàn)
     │ - Promotions (theo từng store)
     │
     ▼
┌──────────┐
│Checkout  │
│Page      │
│          │
│ Hiển thị:│
│ - Voucher│
│   input  │
│ - Danh   │
│   sách   │
│   promotions│
│   theo   │
│   store  │
└────┬─────┘
     │
     │ [Nhập voucher code]
     │ [Chọn promotion]
     │
     ▼
┌──────────┐
│Discount  │
│Controller│
│POST      │
│/api/     │
│discount/ │
│validate  │
└────┬─────┘
     │
     │ Validate:
     │ - Voucher còn hiệu lực?
     │ - Đủ điều kiện minAmount?
     │ - Promotion còn active?
     │
     ▼
┌──────────┐
│Discount  │
│Service   │
│validate  │
│Discount  │
└────┬─────┘
     │
     │ Tính giảm giá:
     │ - Voucher: toàn đơn hàng
     │ - Promotion: theo từng store
     │
     │ Áp dụng:
     │ - PERCENTAGE: % * amount
     │ - FIXED_AMOUNT: giảm trực tiếp
     │
     ▼
┌──────────┐
│Return    │
│          │
│ {        │
│   discount:│
│   amount,  │
│   finalTotal│
│ }        │
└────┬─────┘
     │
     │ [Đặt hàng]
     │
     ▼
┌──────────┐
│Order     │
│Service   │
│create    │
│Orders    │
└────┬─────┘
     │
     │ Áp dụng giảm giá:
     │ - amountFromUser = total - discount
     │ - Lưu voucher/promotion đã dùng
     │
     ▼
┌──────────┐
│Order     │
│Created   │
│with      │
│Discount  │
│Applied   │
└──────────┘
```

## 12. TÓM TẮT CÁC SERVICE CHÍNH

```
┌─────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                        │
├─────────────────────────────────────────────────────────┤
│                                                          │
│ USER SERVICES:                                           │
│ ┌──────────────────┐  ┌──────────────────┐             │
│ │  UserService     │  │  OtpService      │             │
│ │  - registerUser  │  │  - createOtp     │             │
│ │  - findByEmail   │  │  - verifyOtp     │             │
│ │  - updateUser    │  │  - sendEmail     │             │
│ └──────────────────┘  └──────────────────┘             │
│                                                          │
│ PRODUCT SERVICES:                                        │
│ ┌──────────────────┐  ┌──────────────────┐             │
│ │ ProductService   │  │ CategoryService  │             │
│ │  - getAllActive  │  │  - getActive     │             │
│ │  - searchByName  │  │  - getById       │             │
│ │  - filterProducts│  │                  │             │
│ └──────────────────┘  └──────────────────┘             │
│                                                          │
│ ORDER SERVICES:                                          │
│ ┌──────────────────┐  ┌──────────────────┐             │
│ │ OrderService     │  │ CheckoutService  │             │
│ │  - createOrders  │  │  - getCheckout   │             │
│ │  - getUserOrders │  │  - setCheckout   │             │
│ │  - updateStatus  │  │  - clearCheckout │             │
│ └──────────────────┘  └──────────────────┘             │
│                                                          │
│ CART SERVICES:                                           │
│ ┌──────────────────┐  ┌──────────────────┐             │
│ │ CartService      │  │ FavoriteService  │             │
│ │  - addToCart     │  │  - toggleFavorite│             │
│ │  - updateQty     │  │  - getFavorites  │             │
│ │  - removeItem    │  │  - isFavorite    │             │
│ └──────────────────┘  └──────────────────┘             │
│                                                          │
│ PAYMENT SERVICES:                                        │
│ ┌──────────────────┐  ┌──────────────────┐             │
│ │ VNPayService     │  │ DiscountService  │             │
│ │  - createPayment │  │  - validateVoucher│            │
│ │  - verifyPayment │  │  - validatePromo │             │
│ └──────────────────┘  └──────────────────┘             │
│                                                          │
│ AI & CHAT SERVICES:                                      │
│ ┌──────────────────┐  ┌──────────────────┐             │
│ │ AiChatService    │  │ ChatService      │             │
│ │  - chat()        │  │  - sendMessage   │             │
│ │  - inferCategory │  │  - getConversations│           │
│ │  - tryDbFirst    │  │  - markAsRead    │             │
│ └──────────────────┘  └──────────────────┘             │
│                                                          │
│ VENDOR SERVICES:                                         │
│ ┌──────────────────┐  ┌──────────────────┐             │
│ │ VendorProduct    │  │ VendorOrder      │             │
│ │ VendorStore      │  │ VendorAnalytics  │             │
│ │ VendorPromotion  │  │ VendorRegistration│            │
│ └──────────────────┘  └──────────────────┘             │
│                                                          │
│ ADMIN SERVICES:                                          │
│ ┌──────────────────┐  ┌──────────────────┐             │
│ │ AdminService     │  │ ActivityLogService│            │
│ │  - getStats      │  │  - logActivity   │             │
│ │  - approveVendor │  │                  │             │
│ │  - manageUsers   │  │                  │             │
│ └──────────────────┘  └──────────────────┘             │
│                                                          │
│ UTILITY SERVICES:                                        │
│ ┌──────────────────┐  ┌──────────────────┐             │
│ │ CloudinaryService│  │ MailService      │             │
│ │  - uploadFile    │  │  - sendEmail     │             │
│ │  - uploadFiles   │  │  - sendOtp       │             │
│ └──────────────────┘  └──────────────────┘             │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## KẾT LUẬN

Hệ thống Badminton Marketplace được xây dựng theo kiến trúc MVC (Model-View-Controller) với Spring Boot:
- **Controller Layer**: Xử lý HTTP requests và responses
- **Service Layer**: Chứa business logic
- **Repository Layer**: Truy cập database qua JPA
- **Entity Layer**: Định nghĩa các bảng trong database

Các luồng chính:
1. **Authentication**: OTP-based registration và Spring Security login
2. **Shopping Flow**: Browse → Cart → Checkout → Payment → Order
3. **AI Chat**: DB-first strategy với fallback to AI model
4. **Payment**: Hỗ trợ COD, VNPay, và Bank QR
5. **Vendor Management**: Registration → Approval → Product/Order Management
6. **Realtime Chat**: WebSocket-based messaging giữa User và Vendor
7. **Review System**: Rating và review với image upload
8. **Discount System**: Voucher (toàn sàn) và Promotion (theo store)

