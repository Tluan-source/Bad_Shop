# Badminton Marketplace

## Cập nhật tính năng đăng ký:
- Phiên bản đầu tiên là đăng ký sẽ dùng Bcrypt và Salt để mã hóa password, BCrypt không cần dùng salt riêng vì mỗi lần hash thì nó tự tạo salt ngẫu nhiên và lưu cả salt và hash vào 1 string.
- Khi gọi passwordEncoder spring security không biết gì về salt và chỉ nhận hashedPassword "passwordEncoder.matches(rawPasswordFromForm, user.getHashedPassword())"
- Nói chung khi đăng ký: "String hash = BCrypt.encode("123456" + "abc-def-salt");" đoạn này tạo pass + salt 
- Và khi đăng nhập: "passwordEncoder.matches("123456", "$2a$10$xyz...")" thiếu salt trước 123456~