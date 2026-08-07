# AutoTrade — Fabric mod skeleton (Minecraft 1.21.11)

Client-side mod: bạn tự đi tới và nhắm vào villager như bình thường, bấm phím `X` để mod tự
mở GUI trade, lặp lại 1 ô trade đã cấu hình cho đến khi hết hàng/hết tiền, rồi tự đóng GUI.

## ⚠️ Trước khi dùng trên server công cộng

- Đa số server có luật cấm auto-click / auto-farm / macro, kể cả khi nó "chỉ" là trade villager.
- Anti-cheat hiện đại (Grim, Vulcan, NoCheatPlus...) rất dễ phát hiện chuyển động thẳng đều +
  click đều đặn theo tick như trong mod này -> nguy cơ ban cao.
- Việc này không vi phạm pháp luật, nhưng vi phạm ToS của server là rủi ro của người dùng.
- Khuyến nghị: chỉ dùng trên server riêng/singleplayer, hoặc hỏi admin server trước.

## Cấu trúc dự án

```
autotrade/
├── build.gradle
├── gradle.properties
├── settings.gradle
└── src/main/
    ├── java/com/example/autotrade/
    │   ├── AutoTradeClient.java   (đăng ký phím tắt + tick event)
    │   ├── AutoTradeManager.java  (logic: tìm villager, di chuyển, trade)
    │   └── AutoTradeConfig.java   (các thông số chỉnh được)
    └── resources/
        ├── fabric.mod.json
        └── assets/autotrade/lang/en_us.json
```

## Cách build

1. Cài Java 21 (JDK).
2. Cần Gradle wrapper — chạy lệnh sau trong thư mục dự án để tạo wrapper (cần mạng):
   ```
   gradle wrapper --gradle-version 8.10
   ```
3. Build:
   ```
   ./gradlew build
   ```
   File `.jar` sẽ nằm trong `build/libs/`.
4. Copy file `.jar` vào thư mục `mods` của Fabric Loader (Minecraft 1.21.11) cùng với
   `fabric-api-0.141.2+1.21.11.jar`.

**Lưu ý về mapping:** 1.21.11 là bản cuối cùng còn hỗ trợ Yarn mappings (Fabric sẽ chuyển sang
Mojang mappings chính thức sau bản này). Nếu Yarn không còn resolve được cho bản build cụ thể
của bạn, đổi dòng `mappings` trong `build.gradle` sang `loom.officialMojangMappings()` và đổi
tên class theo Mojang mapping (`MinecraftClient` → `Minecraft`, `ClientPlayerEntity` →
`LocalPlayer`, v.v.). Kiểm tra phiên bản Fabric Loader/API mới nhất tại
https://fabricmc.net/develop nếu các version trong `gradle.properties` đã lỗi thời.

## Cách dùng trong game

1. Tự đi lại gần villager, nhắm crosshair vào nó (giống như trade tay bình thường).
2. Bấm `X` (đổi phím trong Controls > AutoTrade Mod nếu muốn).
3. Mod sẽ:
   - Mở GUI trade với villager đang bị nhắm.
   - Tự lặp lại ô trade số `tradeSlotIndex` (mặc định 0 = trade đầu tiên trong danh sách của
     villager đó) cho đến khi villager hết hàng hoặc bạn hết nguyên liệu để trade.
   - Tự đóng GUI khi xong.
4. Muốn trade villager khác: nhắm sang villager đó và bấm `X` lại (chỉ hoạt động khi không có
   phiên trade nào đang chạy).

## Các thông số chỉnh trong `AutoTradeConfig.java`

| Trường | Ý nghĩa |
|---|---|
| `tradeSlotIndex` | Ô trade muốn lặp lại (0 = trade đầu tiên hiện trong GUI của villager đó) |
| `ticksBetweenClicks` | Độ trễ giữa các lần click (tăng lên để trông "người" hơn) |

## Giới hạn hiện tại

- Không có di chuyển/pathfinding tự động — bạn phải tự đứng gần và nhắm vào villager.
- Không tự động chọn "trade tốt nhất" — bạn cấu hình sẵn `tradeSlotIndex` muốn lặp; nếu villager
  đó không có đủ ô trade (index vượt quá số trade hiện có), mod sẽ tự đóng GUI.
- `X` chỉ kích hoạt được khi hiện không có phiên trade nào đang chạy (tránh spam interact).
- 
