# Phân trang đầy đủ + độc lập hoàn toàn cho package `common/tools`

## Context

Package `common/tools` (MCP tool cho AI) hiện có 10 handler. 4/10 (`HopDong`, `SanLuong`, `TramTon`, `VuongMac`) đã có `page/pageSize` + `PagedResult` (phân trang thật ở DB qua `PagingUtil`/`Pageable`). 6 handler còn lại (`BienBan`, `HoSoDoiTuong`, `NganHo`, `NhaThau`, `NguonViec`, `PhanCong`) chưa phân trang — một số dùng `LIMIT` cứng (20-30 dòng) hoặc cắt trong Java (`top`/`MAX_ROWS`), không cho client duyệt trang tiếp theo.

Ngoài ra, 4 handler đang gọi thẳng vào service/repository của module nghiệp vụ khác:
- `NganHoToolHandlerImpl` → `VolumeService` (module `business.volume`)
- `NguonViecToolHandlerImpl` → `NguonLucService` (module `business.nguonluc`) + `NguoiDungRepository`
- `NhaThauToolHandlerImpl` → `NguoiDungRepository`
- `PhanCongToolHandlerImpl` → `PhanCongRepository`, `VuongMacRepository`, `NguoiDungRepository`

Yêu cầu: package `tools` phải độc lập hoàn toàn — **không import bất kỳ Repository/Service nào bên ngoài `common.tools.*`** trong handler lẫn DTO. Entity JPA (dùng làm type param cho `JpaRepository<Entity, UUID>`, đúng pattern đã có ở `HopDongToolRepository`, `VuongMacToolRepository`...) vẫn được phép — đó là data model, không phải service/repository logic. Các DTO dùng chung nhẹ (`common.dto.HopDongRefResponse`, `DoiTuongRefResponse`, `GeoRefResponse`, `RankedItemResponse`...) và `common.util.EntityFilter` cũng giữ nguyên vì đã dùng xuyên suốt package và không phải service/repository.

Đã hỏi người dùng riêng về rủi ro của `NganHo`/`NguonViec` (logic gốc trong `VolumeService` ~1200 dòng và `NguonLucService` ~400 dòng khá phức tạp, tách ra sẽ trùng lặp logic) — người dùng xác nhận **vẫn tách độc lập hoàn toàn**, chấp nhận việc phải port/nhân bản logic.

## Nguyên tắc chung áp dụng cho mọi handler

- Thêm `Integer page, Integer pageSize` vào method `query(...)` của handler nào có ít nhất 1 danh sách "không giới hạn" (contract chưa biết trước sẽ trả bao nhiêu dòng), dùng `PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE)` (mặc định 3, tối đa 100 — xem `support/PagingUtil.java`), field tương ứng trong response DTO đổi từ `List<X>` sang `PagedResult<X>` (`dto/common/PagedResult.java`), và repository trả `Page<Entity>` thay vì `List` với `LIMIT` cứng — đúng mẫu đã làm ở `VuongMacToolHandlerImpl`/`VuongMacToolRepository.search(...)`.
- Danh sách xếp hạng "top N" (`xepHang*`, tham số `top`, tối đa 10-20) **giữ nguyên `List`**, không phân trang — đây là kiểu dữ liệu khác (rank, không phải duyệt trang).
- Danh sách bị chặn cứng bởi số đơn vị hành chính (khu vực/tỉnh, tối đa ~70 dòng: `NganHo.theoKhuVucTinh.duLieu/chiTiet`, `NhaThau.theoTinh`) **giữ nguyên `List`** — không thể "vô hạn" nên phân trang không có giá trị thực tế.
- Mọi repository mới của `tools` implement kiểu `JpaRepository<Entity, UUID>` với `@Query(nativeQuery = true)`, dùng interface projection, đúng 100% phong cách đã có ở `HopDongToolRepository`/`VuongMacToolRepository`/`PhanCongToolRepository` — không tạo tầng service riêng.

## Phần 1 — Chỉ cần thêm phân trang (repository đã độc lập sẵn)

### 1a. `BienBanToolHandler`
File: `handler/BienBanToolHandler.java`, `handler/impls/BienBanToolHandlerImpl.java`, `repository/BienBanToolRepository.java`, `dto/bienban/BienBanQueryResponse.java`.

- Thêm `Integer page, Integer pageSize` vào `query(...)`.
- `theoTrangThai` (hiện `findTheoTrangThai` có `LIMIT 20`) → đổi field response thành `PagedResult<BienBanTheoTrangThaiToolItem>`; sửa `findTheoTrangThai` thành `Page<BienBanTheoTrangThaiRow>` với `@Query` + `countQuery` + `Pageable` (bỏ `LIMIT 20`).
- `thieuKhaoSat` (hiện `findTramThieuKhaoSat` có `LIMIT 20`) → `PagedResult<BienBanThieuKhaoSatToolItem>`, tương tự thêm `countQuery` + `Pageable`.
- `thieuHoSo` (tính trong Java từ `findHopDongForChecklist` LIMIT 20) → `PagedResult<BienBanThieuTheoTienDoToolItem>`. Vì đây là hợp đồng thiếu checklist (đã lọc qua vòng lặp Java, không group-by SQL đơn giản), đổi `findHopDongForChecklist` sang trả `Page<Object[]>` (bỏ `LIMIT 20`, thêm `Pageable` + count query), rồi build `thieuHoSo` chỉ trên đúng 1 trang hợp đồng đó (giữ nguyên logic lặp checklist/loaiBienBanDaDuyet mỗi hợp đồng như hiện tại).

### 1b. `HoSoDoiTuongToolHandler`
File: `handler/HoSoDoiTuongToolHandler.java`, `handler/impls/HoSoDoiTuongToolHandlerImpl.java`, `repository/HoSoDoiTuongToolRepository.java`, `dto/hosodoituong/HoSoDoiTuongQueryResponse.java`.

- Thêm `Integer page, Integer pageSize` vào `query(...)`.
- `chuaKhaoSat` và `khaoSatXongChuaCoSanLuong` (hiện mỗi cái `LIMIT 20`) → đổi thành `PagedResult<...>`; sửa 2 query trong `HoSoDoiTuongToolRepository` thành `Page<...>` + `countQuery` + `Pageable`, bỏ `LIMIT 20`.

## Phần 2 — Bỏ phụ thuộc ngoài + thêm phân trang

### 2a. `NhaThauToolHandler` (đơn giản)
Chỉ dùng `NguoiDungRepository` cho 2 việc: tìm id theo tên gần đúng, và lấy tên theo id — cả 2 đã có sẵn pattern y hệt trong các tool repo khác (`findNhaThauIdByTenGanDung` ở `BienBanToolRepository`/`VuongMacToolRepository`).

- Thêm vào `HopDongToolRepository` (đã được `NhaThauToolHandlerImpl` dùng sẵn cho `rankTinhTheoNhaThau`) 1 method mới `findTenNhaThauById(UUID id)` (native `SELECT ho_ten FROM nguoi_dung WHERE id = :id`) và tái dùng `findNhaThauIdByTenGanDung` đã có kiểu ở các repo khác — thêm luôn vào `HopDongToolRepository` nếu chưa có (hiện chưa có, cần thêm).
- Xoá import `NguoiDungRepository`/`NguoiDung` khỏi `NhaThauToolHandlerImpl`, dùng 2 method trên qua `hopDongToolRepository`.
- `theoTinh` giữ nguyên `List` (lý do: bị chặn bởi số tỉnh, xem nguyên tắc chung).
- Không cần thêm `page/pageSize` ở tầng `NhaThauToolHandler` — nó chỉ rollup, việc phân trang đã nằm ở các handler con nó gọi (`sanLuongToolHandler.query(...)` etc.) nhưng hiện đang truyền `null, null` cho `page/pageSize` — giữ nguyên (rollup 1 nhà thầu không cần duyệt trang các sub-danh sách qua tool này).

### 2b. `PhanCongToolHandler`
File: `handler/PhanCongToolHandler.java`, `handler/impls/PhanCongToolHandlerImpl.java`, `repository/PhanCongToolRepository.java`, `dto/phancong/PhanCongQueryResponse.java`.

Cần bỏ 3 dependency ngoài, port vào `PhanCongToolRepository` (entity hiện tại là `HopDongDoiTuong` — sẽ đổi sang dùng `PhanCong` entity làm root vì logic chính đọc bảng `phan_cong`, giữ các query hiện có bằng cách merge 2 interface hoặc thêm 1 repository con `PhanCongEntityToolRepository extends JpaRepository<PhanCong, UUID>` riêng — quyết định cụ thể lúc code dựa theo entity nào tiện hơn cho từng query):

- `phanCongRepository.findByNgayXoaIsNull()` → thêm method mới, native: `SELECT * FROM phan_cong WHERE ngay_xoa IS NULL` — hoặc derived query `findByNgayXoaIsNull()` trực tiếp nếu root entity là `PhanCong`.
- `phanCongRepository.findActiveForContractor(nguoiDungId, hoTen, tenDangNhap)` → port JPQL nguyên văn (xem `PhanCongRepository.java:23-35`) thành native SQL tương đương trên bảng `phan_cong`.
- `vuongMacRepository.findDoiTuongIdsCoVuongDangMo(ids)` → port JPQL (xem `VuongMacRepository.java:105-113`) thành native: `SELECT DISTINCT du_lieu_doi_tuong_id FROM vuong_mac WHERE ngay_xoa IS NULL AND hoat_dong=TRUE AND du_lieu_doi_tuong_id IN (:ids) AND trang_thai IN ('pending','in_progress')`.
- `nguoiDungRepository.findById(id)`, `.findByHoTenContainingIgnoreCaseAndNgayXoaIsNull(ten)` → thêm 2 native query mới vào `PhanCongToolRepository` (lấy `ho_ten`/`ten_dang_nhap`/`id` theo id hoặc theo tên gần đúng), thay `NguoiDung` entity bằng 1 projection nhỏ (`getId/getHoTen/getTenDangNhap`).

Sau khi port xong, xoá 3 import repository ngoài khỏi `PhanCongToolHandlerImpl`.

Phân trang: thêm `Integer page, Integer pageSize` vào `query(...)`; đổi `danhSach` (hiện cắt cứng `.limit(20)` trong Java) và `theoCanBo` (hiện KHÔNG giới hạn gì — rủi ro trả về toàn bộ phân công của 1 cán bộ) sang `PagedResult<PhanCongToolItem>`; `chuaPhanCong` (hiện `LIMIT 30` trong native query) sang `PagedResult<PhanCongChuaPhanCongToolItem>` với `countQuery` + `Pageable`. Vì `danhSach`/`theoCanBo` hiện lọc bằng Java stream trên toàn bộ `phan_cong` (không phải SQL filter), đơn giản nhất là giữ nguyên cách lọc/sort trong Java rồi tự cắt trang bằng `List.subList` + tính `totalItems = filtered.size()` trước khi cắt — dùng `PagedResult.of(...)` trực tiếp (không cần `Pageable` ở DB cho riêng 2 field này, vì filter phụ thuộc nhiều điều kiện tổ hợp Java sẵn có, mã hoá thành SQL sẽ là thay đổi lớn hơn cần thiết cho tác vụ này).
`xepHangNhaThau` giữ `List` (top N).

## Phần 3 — Port toàn bộ `NguonLucService.danhSach`/`topHopDong` (rủi ro trung bình)

File: `handler/NguonViecToolHandler.java`, `handler/impls/NguonViecToolHandlerImpl.java`, `dto/nguonviec/NguonViecQueryResponse.java`, repository mới `repository/NguonViecToolRepository.java`.

Theo khảo sát `NguonLucServiceImpl.buildAllRows` (dùng cho cả `danhSach` và `topHopDong`), toàn bộ là JOIN/GROUP BY thật trên bảng thật, port được 1:1 sang native SQL:
- `sumThanhTienGroupByHopDongResolved` / `sumThanhTienDtGroupByHopDongResolved` (bảng `san_luong` + `hop_dong_doi_tuong`, đã có SQL mẫu trong khảo sát) — copy làm 2 method mới trong `NguonViecToolRepository`, gọi 3 lần (H1/H2/full) như bản gốc, hoặc gộp thành 1 query trả cả 3 khung thời gian cùng lúc bằng `SUM(...) FILTER (WHERE ...)` để giảm round-trip.
- `countOpenIssuesGroupByHopDong` (bảng `vuong_mac`) — copy y hệt.
- `countGroupByHopDongIdAndTrangThaiMa` lọc `HUY` (bảng `hop_dong_doi_tuong`+`hop_dong`+`doi_tuong_quan_ly`+`trang_thai_hop_dong`) — copy y hệt điều kiện loại trừ đối tượng "hạng mục placeholder" (`doi_tuong_quan_ly_id <> :hangMucId AND ten/ma NOT LIKE '%boq%'/'%hang muc%'`).
- `kieu_hop_dong`/`loai_hop_dong` — 2 `SELECT id, ...` đơn giản, đã có pattern y hệt (`findAllKieuHopDongIdTen` ở `HopDongToolRepository`).
- Base list hợp đồng active (`hopDongService.listAll(...)`) → native `SELECT * FROM hop_dong WHERE ngay_xoa IS NULL AND hoat_dong=TRUE`.

**Trước khi code**: đọc `modules/business/nguonluc/helpers/NguonViecTinhToanHelper.java` để lấy đúng logic `resolveLoaiCv`, `resolveLinhVucFromLoai`, `mapPhapLyStage`, `resolveStatus`, `extractTrungTam`/`findThuocTinh` (đọc EAV `hop_dong_thuoc_tinh`), `toTy`, `matchesFilters`, `buildTabCounts`, `buildSummary` — port các hàm thuần string/số này thành static method mới trong `support/` (ví dụ `support/NguonViecCalc.java`), KHÔNG copy nguyên class helper cũ (helper cũ ở `modules.*` vẫn là "code ngoài").

- `riskScore` cho `topHopDong("rui_ro", ...)` — công thức đã rõ trong khảo sát, port thẳng thành Java thuần trong `NguonViecToolHandlerImpl` (đã là `private` method sẵn có, chỉ cần đổi input rows sang lấy từ repository mới).
- Xoá import `NguonLucService`, `NguoiDungRepository` khỏi `NguonViecToolHandlerImpl`; `NhaThauResolver` dùng lại pattern `findNhaThauIdByTenGanDung`/tên qua id đã thêm ở `NguonViecToolRepository` (copy như các repo khác).

Phân trang: thêm `Integer page, Integer pageSize`; `danhSach` (hiện cắt `MAX_ROWS=30` trong Java) → `PagedResult<NguonViecRowToolItem>` — vì lọc theo nhiều điều kiện tổ hợp (khuVuc/nhaThau/trangThai/query/ngày) đang thực hiện trong Java trên `allRows` đã build sẵn (không phải SQL), tiếp tục lọc trong Java rồi cắt trang bằng `subList` + `PagedResult.of(...)` (tương tự Phần 2b) — chấp nhận được vì tổng số hợp đồng "nguồn việc" không lớn (toàn bộ `hop_dong` đang hoạt động, không phải bảng triệu dòng).
`xepHangRuiRo`, `xepHangTrungTam`, `sapHetGiaTri` giữ `List` (đều là top-N/rank).

## Phần 4 — Port `VolumeService.tongQuan/getChiTiet/khuVucByHopDong` (rủi ro cao nhất)

File: `handler/NganHoToolHandler.java`, `handler/impls/NganHoToolHandlerImpl.java`, repository mới `repository/NganHoToolRepository.java`, có thể thêm `support/HangMucThanhTienCalc.java`.

Theo khảo sát, chia làm 3 phần độ khó khác nhau:

1. **`tongQuan`** — 2 native query thuần aggregate (`tongQuanAggregate` dùng CTE, `tongQuanTheoLoai`) đã có SQL đầy đủ trong khảo sát trên bảng `hop_dong`/`hop_dong_doi_tuong`/`loai_hop_dong` — copy gần như nguyên văn vào `NganHoToolRepository`, chỉ bỏ phần `overrides`/`heSoOverrides`/`contractorScope` (không dùng trong tool AI, `NganHoToolHandler.query()` không nhận các tham số này — luôn truyền `Map.of()`/no scope như code hiện tại).

2. **`getChiTiet`** — KHÔNG phải SQL đơn thuần, là formula engine trên cây `hang_muc_nhom → hang_muc_chi_tiet → hang_muc_cong_viec`. Việc port:
    - Đọc `modules/core/hangmuc/util/HangMucThanhTienCalculator.java` (đánh giá công thức `@MA.KL`/`@SUM.TT`) và cách `HangMucNhomServiceImpl.getTreeByHopDongId` load cây (bảng nào, cột nào, filter `ngay_xoa`/`hoat_dong`).
    - Viết `NganHoToolRepository` method load phẳng 3 bảng theo `hopDongId` (native `SELECT *` từng bảng, filter `ngay_xoa IS NULL`), dựng lại cây trong Java (`support/HangMucThanhTienCalc.java`), rồi áp code tính KL/DG/TT y hệt `HangMucThanhTienCalculator` (copy thuật toán, không import class gốc).
    - `trangThai` dùng `VolumeTinhToanHelper.resolveStatus(giaTriHd, tongThanhTien)` — đọc file này để port ngưỡng `vuot_nguong`/`canh_bao`/`binh_thuong` (đã thấy công thức tương tự trong `tongQuanAggregate` SQL ở khảo sát, dùng lại logic đó).

3. **`khuVucByHopDong`** — nhóm theo tỉnh/khu vực/nhà thầu của các dòng `hop_dong_doi_tuong` thuộc 1 hợp đồng. Điểm quan trọng: các cột `khu_vuc_id`, `tinh_thanh_id`, `nha_thau_id` là **cột FK thật** trên `hop_dong_doi_tuong` (đã xác nhận qua cách `HopDongToolRepository`/`PhanCongToolRepository`/`VuongMacToolRepository` đang query trực tiếp các cột này) — KHÔNG cần đụng tới `HopDongDoiTuongSnapshotService` (dịch vụ cache/EAV) như khảo sát ban đầu lo ngại. Viết native query GROUP BY theo `tinh_thanh_id`/`khu_vuc_id` trên `hop_dong_doi_tuong` (JOIN `khu_vuc`, `tinh_thanh`, `nguoi_dung` để lấy tên), tính `contractValue`/`constructionValue`/`shortageStations`/`surplusStations` bằng công thức đã mô tả trong khảo sát (`planPerStation = hop_dong.tong_thanh_tien_thi_cong / soTram`, so sánh với `san_luong_hieu_luc` từng dòng) — `hop_dong.tong_thanh_tien_thi_cong` là cột đã có sẵn (tính toán trước, chỉ cần `SELECT` thẳng, không cần dựng lại).
    - Đọc `modules/core/hopdong/base/helpers/VolumeTinhToanHelper.java` để port `resolveProvinceVariant`/ngưỡng cảnh báo thiếu/thừa dùng cho `canhBaoThieu`/`canhBaoThua`.

**Khuyến nghị thực hiện**: làm Phần 4 sau cùng, sau khi Phần 1-3 xong và có thể test được pattern chung. Trong lúc code, nếu phát hiện hợp đồng nào dùng `cong_thuc_khoi_luong`/`cong_thuc_don_gia`/`cong_thuc_thanh_tien` (cột có giá trị), phải áp evaluator công thức đầy đủ (không được bỏ qua) — kiểm tra bằng cách query thử `SELECT COUNT(*) FROM hang_muc_chi_tiet WHERE cong_thuc_khoi_luong IS NOT NULL` (và tương tự `hang_muc_cong_viec`) trước khi quyết định có thể đơn giản hoá thành `SUM(khoi_luong*don_gia)` hay bắt buộc phải port evaluator đầy đủ.

`NganHoToolHandler.query()` không có danh sách "vô hạn" ở top-level (chỉ có object lồng nhau, các list con bị chặn bởi số khu vực/tỉnh) → **không cần thêm `page/pageSize`** ở tầng này, theo nguyên tắc chung.

## Cập nhật đi kèm (mọi handler có thêm `page/pageSize` mới: BienBan, HoSoDoiTuong, NguonViec, PhanCong)

- `definitions/McpToolDefinitions.java`: thêm `@ToolParam(description = "Số trang...", required = false) Integer page` và `pageSize` vào method tool tương ứng, theo đúng câu mô tả đã dùng cho `HopDong`/`SanLuong`/`TramTon`/`VuongMac` (dòng 89-90, 130-131, 167-169, 203-204).
- Dòng `.bienBan(bienBanToolHandler.query(null, null, null, null, null))` ở tool tổng hợp (dòng 438) → thêm `null, null` cho page/pageSize.
- `common/tools/TOOL.md`: cập nhật mục mô tả từng tool để liệt kê field nào giờ là object phân trang (`items/page/pageSize/totalItems/totalPages`) thay vì mảng thẳng, và tham số `page/pageSize` mới.

## Thứ tự thực hiện đề xuất

1. Phần 1 (BienBan, HoSoDoiTuong) — an toàn, không đổi phụ thuộc, chỉ đổi kiểu list → PagedResult.
2. Phần 2a (NhaThau) — nhỏ nhất trong nhóm bỏ phụ thuộc ngoài.
3. Phần 2b (PhanCong) — 3 dependency nhưng đều là JPQL/derived-query đơn giản, port nhanh.
4. Phần 3 (NguonViec) — nhiều query hơn nhưng đều là SQL join/group-by thật, không có formula engine.
5. Phần 4 (NganHo) — làm sau cùng, cần đọc thêm 2 file helper (`VolumeTinhToanHelper`, `HangMucThanhTienCalculator`) trước khi code, rủi ro cao nhất vì có formula engine.
6. Cập nhật `McpToolDefinitions.java`/`TOOL.md` theo từng bước ở trên (có thể làm ngay sau mỗi phần, không cần dồn tới cuối).

## Kiểm tra

- Sau mỗi phần: `mvn -q compile` (hoặc build tool đang dùng của repo) để đảm bảo không còn import lỗi.
- Chạy `grep -rn "modules\." src/main/java/.../common/tools --include=*.java | grep -v "^.*://"` để xác nhận không còn import nào trỏ vào `modules.*.repository.*`/`modules.*.services.*` (entity trong `modules.*.entity.*` vẫn hợp lệ).
- Gọi thử endpoint MCP (`/api/v1/mcp/tools` để xem input schema đã có `page`/`pageSize`, và gọi trực tiếp tool qua chatbot/Postman với `page=0,pageSize=3` rồi `page=1,pageSize=3` để xác nhận dữ liệu khác nhau và `totalItems`/`totalPages` đúng) cho từng tool đã sửa.
- Với `NganHo`/`NguonViec`: so sánh kết quả tool mới với API cũ (`VolumeController`/tương đương của `NguonLucService`) trên cùng 1 `maHopDong`/filter để đối chiếu số liệu (`giaTriHopDong`, `tongThanhTienThiCong`, `soHopDongThieu`...) khớp nhau trước khi coi là xong.
