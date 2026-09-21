# TODO — Map các field mã (status/kiểu/enum) sang ngôn ngữ tự nhiên

Bối cảnh: `StatusLabels` (`common/tools/support/StatusLabels.java`) dùng để đổi mã nội bộ DB
(vd `"cho_duyet"`, `"CHUA_TC"`, `"giai_phong_mat_bang"`) sang nhãn tiếng Việt cho output của tool AI,
để LLM đọc response không phải tự đoán ý nghĩa mã. Đã áp dụng cho: `bienBan()`, `vuongMac()`,
`kieuVuongMac()`, `trangThaiThiCong()`, `vatTuA()`, `vatTuB()`.

Quét toàn bộ `common/tools/handler/impls/*.java` phát hiện các chỗ còn set field mã thẳng từ
entity/row vào DTO mà **chưa** qua `StatusLabels`. Việc cần làm: thêm method mapping mới vào
`StatusLabels` (theo đúng pattern hiện có) rồi áp dụng tại nơi set DTO.

## 1. HopDongToolHandlerImpl — ưu tiên cao, chỉ cần áp dụng lại hàm đã có

- File: `HopDongToolHandlerImpl.java` dòng ~160-163 (build map), dùng ở
  `HopDongTongQuanResponse.builder().tienDoTheoTrangThai(...)` dòng ~188.
- Field: `HopDongTongQuanResponse.tienDoTheoTrangThai` (`Map<String, Long>`, **key** chưa map).
- Nguồn: `hopDongToolRepository.countGroupByTrangThaiThiCong()` → cột `hop_dong.trang_thai_thi_cong`.
- Mã: `CHUA_TC`, `DANG_TC`, `HOAN_THANH`, `HUY`.
- Fix: `StatusLabels.trangThaiThiCong()` **đã tồn tại** và đã dùng cho `HopDongTimKiemItem.trangThaiThiCong`
  (dòng ~217) trong cùng file — chỉ cần bọc thêm khi put vào map ở dòng ~160-163.

## 2. TramTonToolHandlerImpl

- File: `TramTonToolHandlerImpl.java` dòng ~170.
- Field: `TramTonToolItemDto.trangThai` — hiện đang set literal thô `"cho_quyet_toan"`.
- Ghi chú: field liền kề `lyDoTon` (dòng ~171) đã có text tiếng Việt `"Chờ quyết toán"` cho cùng
  khái niệm này → tình trạng thiếu nhất quán.
- Fix: thêm `StatusLabels.tramTonTrangThai()` (hoặc tái dùng luôn text `"Chờ quyết toán"`) thay vì
  emit mã thô `cho_quyet_toan`.

## 3. NguonViecToolHandlerImpl (module Nguồn việc)

- File: `NguonViecToolHandlerImpl.java`, method `mapRow()` dòng ~102-135.
- Chưa có dictionary tiếng Việt nào có sẵn trong code — phải tự soạn nhãn mới, dựa theo logic sinh
  mã tại `NguonViecTinhToanHelper` (`modules/business/nguonluc/helpers`).
- Các field cần map:
  - `status` (dòng ~111) — mã: `dang_trien_khai`, `vuong_phap_ly`, `da_hoan_thanh`, `gan_hoan_thanh`.
  - `phapLyStage` (dòng ~124) — mã: `nhap`, `chua_co`, `da_co`.
  - `loaiCV` (dòng ~109) — mã: `TK`, `KSTK`, `TC`.
  - `linhVuc` (dòng ~108) — mã: `tu-van`, `kiem-dinh`, `gphtvt`, `do-luong`, `ict`, `dan-dung`, `tong`, `""`.
- Fix: thêm 4 method mới vào `StatusLabels`: `nguonViecStatus()`, `nguonViecPhapLyStage()`,
  `nguonViecLoaiCv()`, `nguonViecLinhVuc()`.

## 4. NganHoToolHandlerImpl (module Ngân sách)

- File: `NganHoToolHandlerImpl.java`.
- Field: `NganHoChiTietHopDongToolDto.trangThai` (dòng ~110) — nguồn
  `VolumeTinhToanHelper.resolveStatus()` (`modules/core/hopdong/base/helpers/VolumeTinhToanHelper.java`).
  Mã: `binh_thuong`, `canh_bao`, `vuot_nguong`.
- Field: `NganHoProvinceAlertToolItem.type` (dòng ~183) — nguồn `VolumeProvinceAlert.getType()`
  (set tại `VolumeServiceImpl` và `VolumeTinhToanHelper.resolveProvinceVariant()/resolveTramVariant()`).
  Mã (tiếng Anh): `abnormal`, `shortage`, `surplus`, `warning`, `normal`.
- Chưa có dictionary sẵn — cần soạn mới. Gợi ý nhãn:
  - `binh_thuong` → "Bình thường", `canh_bao` → "Cảnh báo", `vuot_nguong` → "Vượt ngưỡng".
  - `shortage` → "Thiếu định mức", `surplus` → "Vượt định mức", `warning` → "Cảnh báo",
    `normal` → "Bình thường", `abnormal` → "Bất thường".
- Fix: thêm `StatusLabels.volumeTrangThai()` và `StatusLabels.volumeAlertType()`.

## 5. BienBanToolHandlerImpl

- Field: `BienBanTheoTrangThaiToolItem.loaiBienBan` (dòng ~123).
- Field: `BienBanTongQuanToolItem.countsByLoai` (map key, dòng ~100-103, build từ
  `bienBanToolRepository.countGroupByLoai(...)`).
- Mã: `BIEN_BAN_SO_1`, `NHAT_KY_THI_CONG`, `BIEN_BAN_SO_2`, `BAO_CAO_KHAO_SAT`, `BAN_GIAO_MAT_BANG`,
  `YEU_CAU_VAT_TU`, `NHAN_VAT_TU`.
- **Đã có dictionary sẵn** (private, khác package, không gọi thẳng được) tại
  `BienBanExportServiceImpl.tenLoaiBienBan(String)`
  (`modules/core/hopdong/bienban/service/impl/BienBanExportServiceImpl.java:960-971`):
  ```
  BIEN_BAN_SO_1      -> "Biên bản nghiệm thu khối lượng"
  NHAT_KY_THI_CONG   -> "Nhật ký thi công"
  BIEN_BAN_SO_2      -> "Biên bản nghiệm thu tổng hợp"
  BAO_CAO_KHAO_SAT   -> "Báo cáo khảo sát (Bìa + Thuyết minh)"
  BAN_GIAO_MAT_BANG  -> "Biên bản bàn giao mặt bằng"
  YEU_CAU_VAT_TU     -> "Biên bản yêu cầu cung cấp vật tư"
  NHAN_VAT_TU        -> "Biên bản nhận vật tư"
  default            -> trả về mã gốc
  ```
- Fix: thêm `StatusLabels.loaiBienBan()` copy y nguyên switch trên (không sửa
  `BienBanExportServiceImpl`, chỉ nhân bản logic sang `StatusLabels` theo đúng pattern các hàm khác).

## Không cần sửa (đã kiểm tra, không có vấn đề)

- `PhanCongToolHandlerImpl` / `VuongMacToolHandlerImpl`: `giaiDoan` là cột free-text người dùng nhập,
  không phải mã enum cố định — không cần dictionary.
- `SanLuongToolHandlerImpl`, `NhaThauToolHandlerImpl`: không có field mã nào, chỉ số liệu/rollup.
- `VuongMacToolHandlerImpl`: đã dùng đúng `StatusLabels.kieuVuongMac()` / `vuongMac()`.
- `HoSoDoiTuongToolHandlerImpl`: đã dùng đúng `StatusLabels.vatTuA()` / `vatTuB()`.
- `NganHoToolHandlerImpl` — field `color`/`barColor`: mã màu hex, không phải mã nghiệp vụ.

## 6. Các map "tổng quan theo loại" phải liệt kê ĐỦ mọi loại, kể cả giá trị 0

Vấn đề: các map dạng `countsByXxx` / `tienDoTheoTrangThai` hiện build bằng cách loop kết quả
`GROUP BY` từ DB rồi `put` trực tiếp — nếu một loại/trạng thái nào đó trong kỳ không có bản ghi nào
thì GROUP BY không trả về row cho loại đó, nên map bị thiếu key (LLM đọc vào tưởng loại đó "không tồn
tại" thay vì "bằng 0"). Cần init sẵn map với đủ tất cả các loại đã biết = 0, rồi mới overlay số đếm
thật từ DB đè lên.

Áp dụng cho tất cả các map liệt kê theo loại/kiểu/trạng thái cố định (danh sách hữu hạn, biết trước):

- `VuongMacTongQuanDto.countsByKieu` (`VuongMacToolHandlerImpl.computeTongQuan()`, ~dòng 92-95) —
  init đủ 5 kiểu: `giai_phong_mat_bang`, `thiet_ke`, `thu_tuc_phap_ly`, `nguon_vat_lieu`, `thi_cong`
  (map qua `StatusLabels.kieuVuongMac()`).
- `HopDongTongQuanResponse.tienDoTheoTrangThai` (`HopDongToolHandlerImpl.computeTongQuan()`,
  ~dòng 160-163) — init đủ 4 trạng thái: `CHUA_TC`, `DANG_TC`, `HOAN_THANH`, `HUY`
  (map qua `StatusLabels.trangThaiThiCong()`).
- `BienBanTongQuanToolItem.countsByLoai` (`BienBanToolHandlerImpl.computeTongQuan()`,
  ~dòng 100-103) — init đủ 7 loại biên bản (xem mục 5 ở trên, map qua `StatusLabels.loaiBienBan()`
  sau khi hàm này được thêm).
- Rà thêm các map tương tự khác nếu có khi làm các mục 2-4 ở trên (vd nếu `NguonViecToolHandlerImpl`
  hay `NganHoToolHandlerImpl` có chỗ nào group-by rồi trả map theo loại cố định thì áp dụng luôn
  cách làm này).

Dùng `LinkedHashMap` (không phải `HashMap`) để giữ thứ tự liệt kê ổn định, dễ đọc.

## Việc cần làm (checklist)

- [x] Áp `StatusLabels.trangThaiThiCong()` cho key của `tienDoTheoTrangThai` trong `HopDongToolHandlerImpl`,
      đồng thời init đủ 4 trạng thái = 0 trước khi overlay (mục 6).
- [x] Thêm `StatusLabels.tramTonTrangThai()`, áp cho `TramTonToolItemDto.trangThai`.
- [x] Thêm `StatusLabels.nguonViecStatus()/nguonViecPhapLyStage()/nguonViecLoaiCv()/nguonViecLinhVuc()`,
      áp trong `NguonViecToolHandlerImpl.mapRow()`.
- [x] Thêm `StatusLabels.volumeTrangThai()/volumeAlertType()`, áp trong `NganHoToolHandlerImpl`.
- [x] Thêm `StatusLabels.loaiBienBan()`, áp cho `BienBanTheoTrangThaiToolItem.loaiBienBan` và key của
      `BienBanTongQuanToolItem.countsByLoai`, đồng thời init đủ 7 loại = 0 trước khi overlay (mục 6).
- [x] Sửa `VuongMacToolHandlerImpl.computeTongQuan()` init đủ 5 kiểu vướng mắc = 0 trước khi overlay
      kết quả `countGroupByKieu()` (mục 6) — `kieuVuongMac()` label đã có sẵn, chỉ thiếu bước init.
- [ ] Rà lại mô tả tool trong `McpToolDefinitions.java` nếu có chỗ nào còn ghi ví dụ bằng mã cũ.
