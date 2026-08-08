# AutoTrade — Fabric mod cho Minecraft 1.21.11

Mod client-side: tự động tìm dân làng (Villager) gần nhất, đi tới, mở giao diện trade,
thực hiện trade (tương đương bấm phím **X**), đóng GUI, rồi tìm villager tiếp theo — lặp lại.

## ⚠️ Lưu ý quan trọng trước khi build

1.21.11 vừa (05/12/2025) chuyển hẳn từ Yarn sang **Mojang Mappings chính thức**
(Minecraft trở thành bản không obfuscate). Đây là thay đổi rất mới, vì vậy:

- Code trong repo này viết bằng **tên class/method chính thức của Mojang**
  (`Minecraft`, `LocalPlayer`, `Villager`, `MerchantScreen`, `MerchantMenu`...),
  chứ không phải tên Yarn cũ (`MinecraftClient`, `PlayerEntity`...).
- Mình **không có kết nối mạng để tự `./gradlew build` thử** trong lúc tạo code này,
  nên chưa thể đảm bảo 100% compile sạch ngay lần đầu. Nếu gặp lỗi tên method/class
  (do version Fabric API/Loom mới hơn số mình ghi trong `gradle.properties`), hãy:
  1. Vào https://fabricmc.net/develop để lấy đúng số bản Loom / Fabric Loader / Fabric API mới nhất cho `1.21.11`.
  2. Cập nhật lại `gradle.properties`.
  3. Nếu tên method lệch (ví dụ `handleInventoryMouseClick`, `interact`, `setSelectionHint`),
     mở class tương ứng trong IDE (IntelliJ + plugin Minecraft Development) để xem tên chính xác — IDE sẽ tự gợi ý.

## Cách build

```bash
./gradlew build
```

File `.jar` sẽ nằm trong `build/libs/`. Copy vào thư mục `mods` của Minecraft (đã cài Fabric Loader + Fabric API).

## Cách dùng trong game

- **Phím X**: trade ngay lập tức nếu đang mở giao diện trade với dân làng (chọn offer đầu tiên còn hàng).
- **Phím `]`** (dấu ngoặc vuông phải): bật/tắt chế độ **auto** — tự tìm villager gần nhất (bán kính 24 block),
  tự đi tới, tự mở trade, tự bấm X, tự đóng, rồi tìm villager kế tiếp.
- Có thể đổi cả 2 phím trong **Settings > Controls > AutoTrade**.

## Giới hạn hiện tại (bạn có thể cải tiến thêm)

- **Di chuyển là "steer" đơn giản** (xoay mặt + đẩy tới), không phải pathfinding thật.
  Player có thể bị kẹt ở hàng rào, nước, hố, tường... Nếu cần chính xác hơn, tích hợp
  thêm thư viện pathfinding như **Baritone API**.
- Mặc định chọn **offer đầu tiên chưa hết hàng** để trade — muốn ưu tiên loại trade cụ thể
  (ví dụ chỉ mua Emerald bằng lúa mì) thì sửa logic chọn `offerIndex` trong `AutoTradeManager.tryTradeCurrentScreen()`.
- Đây là mod **client-side, dùng cho singleplayer/LAN**. Nếu dùng trên server nhiều người chơi,
  hãy kiểm tra luật của server — nhiều server coi việc auto-click/auto-walk là hành vi cheat/macro
  và có thể ban tài khoản.

## Cấu trúc project

```
autotrade-mod/
├─ build.gradle
├─ gradle.properties
├─ settings.gradle
└─ src/main/
   ├─ java/com/example/autotrade/
   │  ├─ AutoTradeMod.java       # entrypoint chung
   │  ├─ AutoTradeClient.java    # đăng ký phím tắt + vòng lặp tick
   │  └─ AutoTradeManager.java   # logic tìm - đi tới - trade - lặp
   └─ resources/
      ├─ fabric.mod.json
      └─ assets/autotrade/lang/en_us.json
```

## Đẩy lên GitHub

```bash
cd autotrade-mod
git init
git add .
git commit -m "Init AutoTrade mod"
git branch -M main
git remote add origin https://github.com/<username>/<repo>.git
git push -u origin main
```
