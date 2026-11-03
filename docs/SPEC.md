# SPEC: Ứng dụng Quản lý Chi tiêu Cá nhân

---

## 1. Mục tiêu và phạm vi

**Bài toán**: người dùng (sinh viên, người đi làm trẻ) muốn ghi nhanh các khoản thu chi, biết mình đã tiêu bao nhiêu trong tháng, tiêu vào đâu, và có vượt hạn mức hay chưa.

**Trong phạm vi**
- Ghi, sửa, xóa giao dịch thu và chi; quản lý danh mục.
- Xem theo tháng, tìm kiếm và lọc.
- Hạn mức chi tiêu hằng tháng và cảnh báo khi gần hoặc vượt hạn mức.
- Thống kê bằng biểu đồ.
- Xuất CSV, sao lưu và khôi phục JSON.

**Ngoài phạm vi**: Firebase hoặc bất kỳ máy chủ nào, đăng nhập, phân quyền người dùng, camera, OCR hóa đơn, nhiều ví, nhiều loại tiền, widget, đồng bộ giữa các máy.

**Không cần xin quyền hệ thống nào** trong bản chính: lưu và mở tệp dùng Storage Access Framework (hộp thoại chọn tệp của hệ thống) nên không cần quyền đọc ghi bộ nhớ.

---

## 2. Công nghệ (lựa chọn phổ biến hiện nay)

| Hạng mục | Chọn | Lý do ngắn |
| --- | --- | --- |
| Ngôn ngữ | Kotlin | Chuẩn của Android |
| Giao diện | Jetpack Compose + Material 3 | Cách làm giao diện chính thức hiện nay, ít tệp hơn XML |
| Điều hướng | Navigation Compose, một Activity | Không cần Fragment |
| Kiến trúc | MVVM: `ViewModel` + `StateFlow` | Tách giao diện khỏi logic, dễ chia việc |
| Lưu trữ | Room (dùng KSP) | Đề yêu cầu dữ liệu lưu bền vững |
| Cài đặt người dùng | DataStore Preferences | Thay SharedPreferences |
| Bất đồng bộ | Coroutines + Flow | Room trả về `Flow` để giao diện tự cập nhật |
| Biểu đồ | MPAndroidChart (đề gợi ý), nhúng bằng `AndroidView` | Quen thuộc, nhiều tài liệu |
| JSON | kotlinx.serialization | Dùng cho sao lưu và khôi phục |
| Ngày giờ | `java.time` (`LocalDate`, `YearMonth`) | Có sẵn từ minSdk 26 |
| SDK | minSdk 26, targetSdk theo mẫu project của Android Studio | |
| DI | **Không dùng Hilt**; tạo `AppContainer` thủ công trong lớp `Application` | Giảm học thêm trong 2 tuần |

Tạo project bằng mẫu **Empty Activity** (Compose) của Android Studio, dùng Version Catalog (`libs.versions.toml`) với bản ổn định mới nhất.

---

## 3. Chức năng

Ưu tiên: **MVP** là bắt buộc để nộp bài; **Mở rộng** chỉ làm sau khi MVP xong và đã kiểm thử.

| Mã | Chức năng | Ưu tiên |
| --- | --- | --- |
| F01 | Thêm, sửa, xóa giao dịch | MVP |
| F02 | Danh sách giao dịch theo tháng, nhóm theo ngày, tổng thu, tổng chi, số dư | MVP |
| F03 | Quản lý danh mục (thêm, sửa, xóa) và bộ danh mục mặc định | MVP |
| F04 | Tìm kiếm và lọc kết hợp | MVP |
| F05 | Hạn mức hằng tháng (tổng và theo danh mục) kèm cảnh báo | MVP |
| F06 | Thống kê: biểu đồ tròn theo danh mục, biểu đồ cột theo ngày | MVP |
| F07 | Xuất CSV | MVP |
| F08 | Sao lưu và khôi phục JSON | MVP |
| F09 | Giao dịch lặp lại hằng tháng (tiền nhà, internet) | Mở rộng |
| F10 | Giao diện sáng, tối, theo hệ thống | Mở rộng |
| F11 | So sánh chi tiêu tháng này với tháng trước | Mở rộng |
| F12 | Nhắc nhập chi tiêu mỗi ngày bằng thông báo | Mở rộng |

### Tiêu chí nghiệm thu

**F01 Giao dịch**
- Form gồm: loại (thu hoặc chi), số tiền, danh mục, ngày, ghi chú (không bắt buộc).
- Số tiền: chỉ nhận chữ số, tự thêm dấu chấm ngăn cách khi gõ, phải lớn hơn 0 và không quá 100.000.000.000 đ; sai thì báo lỗi ngay dưới ô nhập, không lưu.
- Danh mục lọc theo loại đã chọn; đổi loại thì danh mục đã chọn được đặt lại.
- Ngày mặc định là hôm nay, chọn bằng `DatePicker`.
- Sửa: bấm vào một dòng trong danh sách mở form có sẵn dữ liệu. Xóa: có hộp thoại xác nhận.
- Lưu xong quay lại danh sách; nếu giao dịch chi làm danh mục đạt từ 80% hạn mức thì hiện cảnh báo (xem F05).

**F02 Danh sách**
- Nút chuyển tháng trước, sau; mặc định tháng hiện tại.
- Giao dịch nhóm theo ngày (mới nhất trước), mỗi nhóm có tiêu đề ngày và tổng chi trong ngày.
- Đầu danh sách hiện tổng thu, tổng chi, số dư của tháng.
- Tháng không có giao dịch: hiện màn hình rỗng kèm nút "Thêm giao dịch".

**F03 Danh mục**
- Tạo sẵn khi cài lần đầu: Chi (Ăn uống, Di chuyển, Mua sắm, Nhà ở, Giải trí, Học tập, Sức khỏe, Khác), Thu (Lương, Thưởng, Trợ cấp, Khác).
- Thêm, sửa tên, biểu tượng, màu; tên không được rỗng và không trùng trong cùng loại.
- Hai danh mục "Khác" không xóa được.
- Xóa danh mục đang có giao dịch: hỏi và chuyển các giao dịch đó sang "Khác" cùng loại rồi mới xóa; hạn mức gắn với danh mục cũng bị xóa.

**F04 Tìm kiếm và lọc**
- Ô tìm kiếm theo ghi chú (không phân biệt hoa thường, có dấu hoặc không dấu đều được là điểm cộng).
- Bộ lọc dạng bottom sheet: loại, danh mục, khoảng ngày, khoảng số tiền. Các điều kiện dùng đồng thời.
- Có chip hiển thị bộ lọc đang bật, nút "Xóa bộ lọc".
- Không có kết quả: hiện trạng thái rỗng riêng ("Không tìm thấy giao dịch phù hợp").

**F05 Hạn mức**
- Hạn mức lặp lại mỗi tháng: một hạn mức tổng và tùy chọn hạn mức cho từng danh mục chi.
- Màn hình hiện từng hạn mức với thanh tiến độ: đã chi, còn lại, phần trăm.
- Mức độ: dưới 80% là bình thường, từ 80% đến dưới 100% là cảnh báo (màu vàng), từ 100% trở lên là vượt (màu đỏ).
- Sau khi lưu một giao dịch chi, nếu danh mục hoặc tổng chuyển lên mức cảnh báo hoặc vượt thì hiện `Snackbar` thông báo.
- Hạn mức phải lớn hơn 0; để trống hoặc xóa nghĩa là không đặt hạn mức.

**F06 Thống kê**
- Chọn tháng; chuyển giữa Chi và Thu.
- Biểu đồ tròn: tỉ lệ theo danh mục, chú thích có số tiền và phần trăm.
- Biểu đồ cột: tổng theo từng ngày trong tháng.
- Tháng không có dữ liệu: màn hình rỗng, không vẽ biểu đồ trống.

**F07 Xuất CSV**
- Xuất toàn bộ giao dịch của tháng đang chọn hoặc tất cả, chọn nơi lưu bằng `CreateDocument`.
- Cột: Ngày, Loại, Danh mục, Số tiền, Ghi chú. Mã hóa UTF-8 có BOM để Excel hiển thị đúng tiếng Việt.
- Ghi chú chứa dấu phẩy, dấu ngoặc kép hoặc xuống dòng phải được bọc và thoát đúng chuẩn CSV.
- Hủy hộp thoại chọn tệp thì không báo lỗi.

**F08 Sao lưu và khôi phục**
- Sao lưu: ghi một tệp JSON gồm danh mục, giao dịch, hạn mức, kèm số phiên bản (`"version": 1`).
- Khôi phục: chọn tệp bằng `OpenDocument`, **kiểm tra toàn bộ nội dung trước**, hỏi xác nhận rồi mới thay dữ liệu hiện tại; ghi trong một transaction của Room để lỗi giữa chừng không làm hỏng dữ liệu.
- Tệp không phải JSON, thiếu trường, sai phiên bản, số tiền âm: báo "Tệp sao lưu không hợp lệ" và giữ nguyên dữ liệu.

**F09 Giao dịch lặp** (mở rộng)
- Quy tắc: loại, số tiền, danh mục, ghi chú, ngày trong tháng.
- Khi mở app, tạo các giao dịch còn thiếu cho mọi tháng từ lần chạy trước đến tháng này. Chạy lặp lại nhiều lần không được tạo trùng (lưu `lastGeneratedMonth`).
- Ngày 29 đến 31 rơi vào tháng ngắn hơn thì dùng ngày cuối tháng.

---

## 4. Màn hình

Một Activity, thanh điều hướng dưới có 5 mục. Có 7 màn hình chính, nhiều hơn mức tối thiểu 4 của đề.

| # | Màn hình | Nội dung |
| --- | --- | --- |
| 1 | Tổng quan (tab) | Thu, chi, số dư tháng này; tiến độ hạn mức tổng; 5 giao dịch gần nhất; nút nổi "+" |
| 2 | Giao dịch (tab) | F02 và F04: chuyển tháng, tìm kiếm, bộ lọc, danh sách |
| 3 | Thêm / sửa giao dịch | Form F01, mở từ nút "+" hoặc từ một dòng |
| 4 | Thống kê (tab) | F06 |
| 5 | Hạn mức (tab) | F05 |
| 6 | Cài đặt (tab) | Lối vào Danh mục, Xuất CSV, Sao lưu, Khôi phục, Giao diện, Nạp dữ liệu mẫu, Xóa toàn bộ dữ liệu |
| 7 | Danh mục | F03: danh sách theo loại, hộp thoại thêm, sửa |

Mở rộng: màn hình Giao dịch lặp (F09, TV3).

---

## 5. Dữ liệu (Room)

Tiền lưu bằng `Long` (đơn vị đồng), **không dùng `Double`**. Ngày lưu dạng `LocalDate` (qua `TypeConverter`, lưu `epochDay`).

```kotlin
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TxType,            // INCOME | EXPENSE
    val iconKey: String,
    val colorHex: String,
    val isDefault: Boolean = false   // true = "Khác", không xóa được
)

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(CategoryEntity::class, ["id"], ["categoryId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("categoryId"), Index("date")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TxType,
    val amount: Long,            // > 0
    val categoryId: Long,
    val date: LocalDate,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val categoryId: Long,   // 0 = hạn mức tổng (không có khóa ngoại)
    val limitAmount: Long
)

// Mở rộng F09
@Entity(tableName = "recurring_rules")
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TxType, val amount: Long, val categoryId: Long,
    val note: String, val dayOfMonth: Int,
    val lastGeneratedMonth: String?    // "yyyy-MM"
)
```

**Truy vấn cần có** (DAO trả về `Flow`):
- Giao dịch theo khoảng ngày, có join tên và màu danh mục.
- Giao dịch lọc kết hợp (từ khóa, loại, danh mục, khoảng ngày, khoảng tiền; tham số rỗng nghĩa là bỏ qua điều kiện).
- Tổng theo loại trong khoảng ngày.
- Tổng theo danh mục trong khoảng ngày (cho biểu đồ tròn và hạn mức).
- Tổng theo ngày trong khoảng ngày (cho biểu đồ cột).

**Cài đặt trong DataStore**: giao diện (sáng, tối, theo hệ thống), cờ đã tạo danh mục mặc định.

---

## 6. Cấu trúc mã nguồn

Chia gói theo tính năng để ba người ít sửa chung một tệp.

```
com.example.chitieu
├── MainActivity.kt
├── ChiTieuApp.kt                 // Application, giữ AppContainer
├── AppContainer.kt               // tạo Database, Repository thủ công
├── data/
│   ├── local/                    // AppDatabase, dao/, entity/, Converters
│   ├── repository/               // TransactionRepository, CategoryRepository, BudgetRepository
│   ├── backup/                   // BackupManager (JSON), CsvExporter
│   └── settings/                 // SettingsStore (DataStore)
├── domain/                       // BudgetChecker, RecurringProcessor, model BudgetStatus...
├── ui/
│   ├── navigation/               // AppNavHost, Routes
│   ├── theme/
│   ├── components/               // AmountField, EmptyState, ConfirmDialog, MonthSwitcher
│   ├── home/  transactions/  transactionform/
│   ├── stats/  budget/  settings/  categories/
└── util/                         // MoneyFormat, DateUtils
```

Quy ước:
- Mỗi màn hình gồm `XxxScreen` (Compose), `XxxViewModel`, `XxxUiState`. Screen chỉ quan sát `StateFlow<UiState>` và gọi hàm của ViewModel.
- Tiền hiển thị theo VND bằng `MoneyFormat` (ví dụ `1.250.000 đ`).
- Mọi chuỗi giao diện đặt trong `strings.xml`.
- Danh sách dùng `LazyColumn` với `key = { it.id }`.

---

## 7. Xử lý lỗi và trường hợp biên

| Tình huống | Hành vi |
| --- | --- |
| Số tiền rỗng, bằng 0, âm, quá giới hạn | Báo lỗi dưới ô nhập, nút Lưu không hoạt động |
| Chưa chọn danh mục | Báo lỗi, không lưu |
| Tên danh mục rỗng hoặc trùng | Báo lỗi trong hộp thoại |
| Danh sách giao dịch, kết quả tìm kiếm, thống kê, hạn mức, danh mục rỗng | Mỗi nơi có màn hình rỗng kèm gợi ý hoặc nút hành động |
| Xóa danh mục đang có giao dịch | Hỏi, chuyển sang "Khác", rồi xóa |
| Tệp sao lưu hỏng hoặc sai định dạng | Báo lỗi, giữ nguyên dữ liệu hiện có |
| Hủy hộp thoại chọn tệp | Không làm gì, không báo lỗi |
| Xoay màn hình, đổi giao diện sáng tối | Dữ liệu đang nhập trong form không mất |
| Xóa toàn bộ dữ liệu | Hai bước xác nhận |
| (Mở rộng F12) Từ chối quyền thông báo trên Android 13 trở lên | Giải thích ngắn, tắt nhắc nhở, app vẫn dùng bình thường |

---

## 8. Phân công

### Phân công

| | TV1: Danh mục, Cài đặt và dữ liệu | TV2: Giao dịch và Tổng quan | TV3: Hạn mức, Thống kê, Giao dịch lặp |
| --- | --- | --- | --- |
| Chức năng | F03, F07, F08, F10 | F01, F02, F04, F12 | F05, F06, F09, F11 |
| Màn hình | 6, 7 | 1, 2, 3 | 4, 5 |
| DAO tự viết | `CategoryDao`, `BackupDao` | `TransactionDao` | `BudgetDao`, `StatsDao`, `RecurringDao` |
| Logic riêng | Xóa danh mục thì chuyển giao dịch sang "Khác" và xóa hạn mức liên quan; CSV và JSON đọc ghi mọi bảng | Form và kiểm tra dữ liệu; lọc kết hợp; truy vấn danh mục cho form | `BudgetChecker` (hàm thuần), `RecurringProcessor`, `BudgetSummaryCard` |

### Cách làm song song

1. **Làm bộ khung chung**, sau đó không ai sửa các tệp này nữa:
    - Project, theme, khung điều hướng có sẵn 5 tab trống.
    - Tất cả entity, `AppDatabase`, `Converters`, tự tạo danh mục mặc định khi tạo database.
    - Khai báo sẵn mọi DAO rỗng trong `AppDatabase` để sau này không ai phải sửa tệp này.
    - `MoneyFormat` và `SampleData` (bản debug tự nạp dữ liệu mẫu ở lần chạy đầu, để mỗi người có dữ liệu thử ngay).
2. **Mỗi người tự viết DAO riêng**, kể cả khi truy vấn cùng một bảng; Room cho phép nhiều DAO đọc cùng bảng nên không ai phải chờ mã của người khác.
3. **Không đổi trường của entity**. Nếu bắt buộc phải đổi, báo cả nhóm để cùng cập nhật. Trong giai đoạn phát triển dùng `fallbackToDestructiveMigration`.
4. **Mỗi người chỉ sửa trong thư mục tính năng của mình**; cần sửa tệp của người khác thì nhắn họ làm.

### Ba điểm ghép

| Điểm ghép | Cách ghép |
| --- | --- |
| Cảnh báo sau khi lưu giao dịch | TV3 cung cấp `BudgetChecker.check(categoryId, month): BudgetStatus` (hàm thuần); TV2 thêm khoảng 3 dòng gọi sau khi lưu. Trước đó TV2 bỏ qua phần này |
| Tiến độ hạn mức trên Tổng quan | TV3 làm sẵn composable `BudgetSummaryCard()`; TV2 thả vào màn Tổng quan bằng 1 dòng |
| Thành phần giao diện dùng chung (ví dụ chuyển tháng) | Mỗi người tự làm bản của mình; gộp lại sau mốc 2 nếu còn thời gian |

Nếu trễ: bỏ chức năng Mở rộng trước, theo thứ tự F12, F11, F10, F09.

---

## 9. Làm việc nhóm

- Một repo GitHub chung (riêng tư), nhánh `main` luôn chạy được. Mỗi chức năng một nhánh, ví dụ `feature/F01-form-giao-dich`.
- Commit ghi mã chức năng, ví dụ `F01: thêm kiểm tra số tiền`.
- Pull Request ngắn, mỗi PR một người khác xem nhanh trước khi merge.
- Chức năng được coi là xong khi: chạy trên máy ảo, đúng tiêu chí ng[SPEC (1).md](SPEC%20%281%29.md)hiệm thu ở mục 3, có xử lý lỗi theo mục 7, PR đã merge.
- Mỗi người viết vài unit test JUnit cho logic của mình: `MoneyFormat`, `BudgetChecker` (ba mức độ), `RecurringProcessor` (không tạo trùng, tháng ngắn), bộ phân tích CSV và JSON.

---

## 10. Đối chiếu yêu cầu đề bài

- [ ] **Ít nhất 4 màn hình**: 7 màn hình (mục 4)
- [ ] **Thực thể dữ liệu lưu bền vững, thêm xem sửa xóa**: giao dịch, danh mục, hạn mức trong Room; có kiểm tra đầu vào và xử lý danh sách rỗng (mục 3, 7)
- [ ] **Xử lý nghiệp vụ ngoài CRUD**: tính số dư, hạn mức và cảnh báo theo ngưỡng, thống kê theo danh mục và theo ngày, tìm kiếm kết hợp bộ lọc, giao dịch lặp
- [ ] **Tính năng đặc thù Android**: Storage Access Framework để lưu và mở tệp CSV, JSON; biểu đồ MPAndroidChart; (mở rộng) thông báo
- [ ] **Chạy trên máy ảo hoặc thiết bị; xử lý thao tác không hợp lệ, dữ liệu lỗi**: mục 7 (không có API nên không có tình huống mất mạng; không xin quyền nên không có tình huống từ chối quyền trong bản chính)
- [ ] **Bàn giao**: mã nguồn, README cài đặt và chạy, dữ liệu thử (nút "Nạp dữ liệu mẫu" và tệp `sample_backup.json`), video các luồng chính
- [ ] **Dịch vụ ngoài có cách trình diễn khả thi**: không dùng dịch vụ ngoài

---

