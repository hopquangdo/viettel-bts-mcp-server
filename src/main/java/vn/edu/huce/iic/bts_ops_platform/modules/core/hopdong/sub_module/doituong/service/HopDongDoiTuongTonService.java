package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service;

import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.HopDongDoiTuongSnapshot;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.HopDongDoiTuongTonStatRow;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.dto.response.HopDongDoiTuongTonItemResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Đối tượng HĐ đã HTTC + có pháp lý + chưa quyết toán — tách "chờ quyết toán" (chưa quá hạn) và
 * "quá hạn" theo ngưỡng số ngày tồn. CHỈ dùng cột thật của hop_dong_doi_tuong/hop_dong (core) —
 * không lọc "đang vướng mắc" (thuộc module vuongmac) và "giaTri" trả về ở đây chỉ là fallback
 * chia đều giá trị HĐ. Tầng gộp business.tramton tự trừ tập "đang vướng mắc" (gọi API riêng
 * của module vuongmac) và tự resolve giá trị thật (ưu tiên sản lượng báo cáo) sau khi gộp.
 */
public interface HopDongDoiTuongTonService {

    PageResponse<HopDongDoiTuongTonItemResponse> choQuyetToan(
            UUID loaiHopDongId,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer quaHanNgay,
            LocalDate ngayBaoCao,
            Integer page,
            Integer size);

    PageResponse<HopDongDoiTuongTonItemResponse> quaHan(
            UUID loaiHopDongId,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer quaHanNgay,
            LocalDate ngayBaoCao,
            Integer page,
            Integer size);

    /** Bản không phân trang của choQuyetToan() — dùng để tổng hợp KPI (business.tramton.tongQuan). */
    List<HopDongDoiTuongTonItemResponse> choQuyetToanAll(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay, LocalDate ngayBaoCao);

    /** Bản không phân trang của quaHan() — dùng để tổng hợp KPI (business.tramton.tongQuan). */
    List<HopDongDoiTuongTonItemResponse> quaHanAll(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay, LocalDate ngayBaoCao);

    /**
     * Bản "stats-only" của choQuyetToanAll() — dùng cho TramTonServiceImpl.tongQuan(), nơi chỉ
     * cần đếm/bucket tuổi tồn/tổng giá trị, KHÔNG cần mã trạm/khu vực/nhà thầu. Bỏ hẳn bước
     * resolve HopDongDoiTuongSnapshot (batched nhưng vẫn tốn round-trip cache/DB cho từng id) —
     * chỉ còn 2 query SQL (candidate rows + đếm nhóm theo hợp đồng để chia đều giá trị).
     */
    List<HopDongDoiTuongTonStatRow> choQuyetToanStatsAll(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay, LocalDate ngayBaoCao);

    /** Bản "stats-only" của quaHanAll() — xem javadoc choQuyetToanStatsAll(). */
    List<HopDongDoiTuongTonStatRow> quaHanStatsAll(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay, LocalDate ngayBaoCao);

    /** Đối tượng thuộc HĐ chưa có pháp lý — danh sách cơ bản module trạm tồn. */
    PageResponse<HopDongDoiTuongTonItemResponse> chuaPhapLy(
            UUID loaiHopDongId,
            LocalDate ngayBaoCao,
            Integer page,
            Integer size);

    List<HopDongDoiTuongTonItemResponse> chuaPhapLyAll(UUID loaiHopDongId, LocalDate ngayBaoCao);

    /** Bản lite — không resolve snapshot (mã trạm/khu vực/nhà thầu = "—"), dùng sort/lọc/pagination. */
    List<HopDongDoiTuongTonItemResponse> choQuyetToanLiteAll(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay, LocalDate ngayBaoCao);

    List<HopDongDoiTuongTonItemResponse> quaHanLiteAll(
            UUID loaiHopDongId, LocalDate dateFrom, LocalDate dateTo, Integer quaHanNgay, LocalDate ngayBaoCao);

    List<HopDongDoiTuongTonItemResponse> chuaPhapLyLiteAll(UUID loaiHopDongId, LocalDate ngayBaoCao);

    List<HopDongDoiTuongTonStatRow> chuaPhapLyStatsAll(UUID loaiHopDongId, LocalDate ngayBaoCao);

    /**
     * Tra cứu 1 đối tượng thuộc tập tồn core (chưa PL / quá hạn / chờ QT) — dùng cho chi tiết,
     * tránh quét toàn bộ danh sách.
     */
    java.util.Optional<HopDongDoiTuongTonItemResponse> findTonCoreItemByDoiTuongId(
            UUID doiTuongId,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer quaHanNgay,
            LocalDate ngayBaoCao);

    /** Tra cứu nhanh 1 dòng "chưa pháp lý" cho modal chi tiết trạm tồn. */
    java.util.Optional<HopDongDoiTuongTonItemResponse> findChuaPhapLyChiTietByDoiTuongId(
            UUID doiTuongId,
            LocalDate ngayBaoCao,
            HopDongDoiTuongSnapshot snapshot);

    /** Tra cứu nhanh quá hạn / chờ QT cho modal chi tiết trạm tồn (bỏ qua chưa pháp lý). */
    java.util.Optional<HopDongDoiTuongTonItemResponse> findQuaHanOrChoQtChiTietByDoiTuongId(
            UUID doiTuongId,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer quaHanNgay,
            LocalDate ngayBaoCao,
            HopDongDoiTuongSnapshot snapshot);
}
