package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.service;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanLichSuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanNguoiKyRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanThayTheResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BienBanExportService {

    /**
     * Xuất trọn bộ hồ sơ nghiệm thu (đóng gói .zip) cho các đối tượng đã chọn thuộc 1 hợp đồng:
     * mỗi đối tượng 1 "Biên bản số 1" + 1 "Nhật ký thi công", cộng thêm đúng 1 "Biên bản số 2"
     * tổng hợp cho cả lô đã chọn. Mỗi lần xuất được ghi lại vào lịch sử ở trạng thái "cho_duyet"
     * (xem getLichSu, pheDuyet, tuChoi).
     */
    byte[] xuatHangLoat(
            UUID hopDongId, List<UUID> hopDongDoiTuongIds, BienBanNguoiKyRequest nguoiKy, List<String> loaiBienBan);

    /** Tạo 1 biên bản không sinh file Word: loại trước thi công (bàn giao MB / vật tư — gate tuần
     * tự) hoặc bất kỳ mã danh mục biên bản đang hoạt động chưa có mẫu Word. Chỉ ghi lịch sử chờ
     * duyệt; ngayLap = ngày thực tế của mốc (null lấy hôm nay) — dữ liệu so KPI. */
    BienBanLichSuResponse xuatDonTruocThiCong(
            UUID hopDongId, List<UUID> hopDongDoiTuongIds, String loaiBienBan, LocalDate ngayLap,
            BienBanNguoiKyRequest nguoiKy);

    /** Lịch sử các lần xuất biên bản của 1 hợp đồng — mới nhất trước. */
    List<BienBanLichSuResponse> getLichSu(UUID hopDongId);

    /** Duyệt 1 biên bản đang ở trạng thái "cho_duyet". */
    BienBanLichSuResponse pheDuyet(UUID hopDongId, UUID bienBanId);

    /** Từ chối 1 biên bản đang ở trạng thái "cho_duyet" — bắt buộc có lý do. */
    BienBanLichSuResponse tuChoi(UUID hopDongId, UUID bienBanId, String lyDo);

    /** Duyệt hàng loạt — chỉ các biên bản thuộc đúng hợp đồng và đang "cho_duyet" mới được cập nhật. */
    List<BienBanLichSuResponse> pheDuyetHangLoat(UUID hopDongId, List<UUID> bienBanIds);

    /** Từ chối hàng loạt — dùng chung 1 lý do cho cả lô. */
    List<BienBanLichSuResponse> tuChoiHangLoat(UUID hopDongId, List<UUID> bienBanIds, String lyDo);

    /** Tải file biên bản trước thi công (.docx) theo bản ghi lịch sử đã xuất. */
    byte[] taiVeBienBan(UUID hopDongId, UUID bienBanId);

    /**
     * Xuất lại 1 biên bản đã bị từ chối — tạo lại đúng tài liệu từ danh sách đối tượng và người
     * ký đã lưu tại lần xuất gốc, ghi thành 1 bản ghi lịch sử mới (trạng thái "cho_duyet") trỏ về
     * bản gốc qua bienBanGocId.
     */
    byte[] xuatLai(UUID hopDongId, UUID bienBanGocId);

    /**
     * Hủy hiệu lực biên bản đã duyệt và tạo bản thay thế chờ duyệt — áp dụng mọi loại biên bản.
     * Trả kèm file .zip nếu loại có mẫu Word.
     */
    BienBanThayTheResult taoBanThayThe(UUID hopDongId, UUID bienBanId, String lyDo);

    /**
     * "Ghép hồ sơ hoàn chỉnh" (STT 20) — đóng gói .zip cho các trạm đã chọn, mỗi trạm 1 thư mục
     * riêng (tên = mã trạm), bên trong là các thư mục con theo từng loại hồ sơ: 4 loại đã có mẫu
     * Word (Báo cáo khảo sát, Biên bản số 1, Nhật ký thi công, Biên bản số 2 — chỉ lấy bản ghi đã
     * duyệt, tái tạo lại đúng nội dung/người ký đã lưu, KHÔNG ghi thêm lịch sử mới), biên bản phát
     * sinh đã duyệt kèm tệp đính kèm, và các danh mục tải lên thủ công (Bàn giao mặt bằng, Giao
     * nhiệm vụ giám sát/thi công, Nghiệm thu đầu vào vật liệu, Hồ sơ thiết kế, Thi công). Thư mục
     * nào không có tài liệu thì bỏ qua, không tạo thư mục rỗng.
     */
    byte[] xuatGopTheoTram(UUID hopDongId, List<UUID> hopDongDoiTuongIds);
}
