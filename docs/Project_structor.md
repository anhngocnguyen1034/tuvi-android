---
description: Quy định cấu trúc thư mục chuẩn cho các ứng dụng thuộc hệ sinh thái Anhnn.
globs: app/**/*
---
# Lưu ý: Đây là cấu trúc tham chiếu. AI cần thích nghi với tổ chức hiện tại của dự án Anhnn.

- Project Structure:

app/
src/
main/
java/com/anhnn/
core/               # Các thành phần dùng chung cho toàn hệ sinh thái (Base class, Constants)
di/                 # Hilt Modules (Network, Database, Repository modules)
data/               # Layer thực thi dữ liệu
repository/       # Triển khai thực tế của các Repository (Impl)
datasource/       # Remote (Static Service) & Local (Room/DataStore)
models/           # Data Transfer Objects (DTO) / Entities
domain/             # Layer chứa Business Logic (nguyên chất Kotlin)
usecases/         # Các nghiệp vụ cụ thể của hệ sinh thái
models/           # Domain Models (Dùng cho UI)
repository/       # Interfaces định nghĩa các Repository
presentation/       # Layer hiển thị (Jetpack Compose)
screens/          # Các màn hình chính (Feature-based)
components/       # Reusable Anhnn Components (Buttons, Gradients, Cards)
theme/            # AnhnnDesignSystem (Color.kt, Type.kt, Theme.kt)
viewmodels/       # Quản lý UI State & Events
utils/              # Các hàm tiện ích (Extensions, Formatters)
res/
values/             # strings.xml, colors.xml (Anhnn tokens)
drawable/           # Vector icons & Static drawables
raw/                # Chỉ chứa các config nhẹ, Lottie nên dùng URL
test/                   # Unit Tests (ViewModel, UseCase)
androidTest/            # UI Tests (Compose, Hilt)