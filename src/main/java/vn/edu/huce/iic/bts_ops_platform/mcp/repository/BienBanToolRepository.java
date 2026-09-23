package vn.edu.huce.iic.bts_ops_platform.mcp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban.BienBanTheoTrangThaiRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban.DemNhomRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.bienban.BienBan;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BienBanToolRepository extends JpaRepository<BienBan, UUID> {

    /**
     * Biên bản lưu theo TỪNG HỢP ĐỒNG (không theo từng trạm — hopDongDoiTuongIdsJson chỉ là
     * snapshot phục vụ "xuất lại", không truy vấn được theo từng trạm). Coi 1 hợp đồng là "chưa
     * có biên bản khảo sát đã duyệt" nếu KHÔNG có bản ghi BAO_CAO_KHAO_SAT đã duyệt nào — khi đó
     * liệt kê TẤT CẢ trạm thuộc hợp đồng đó (xấp xỉ ở cấp hợp đồng, không phải per-trạm thật).
     * row: [maTram, khuVuc, maHopDong]. CỐ Ý giữ điều kiện {@code b.hoat_dong = TRUE} trong subquery
     * (khác với các query đếm/liệt kê thuần hiển thị bên dưới) vì đây là kiểm tra TÍNH HIỆU LỰC
     * nghiệp vụ ("đã có báo cáo khảo sát ĐANG có hiệu lực") — khớp cách REST
     * {@code BienBanExportServiceImpl.latestPreThiCongForStation()} lọc
     * {@code Boolean.TRUE.equals(item.getHoatDong())} khi dùng biên bản để gating bước tiếp theo;
     * một biên bản đã "hủy hiệu lực" (hoat_dong=false) không nên tính là "đã khảo sát xong".
     */
    @Query(value = """
            SELECT COALESCE(dt.ten, dt.ma, '—') AS ma_tram, COALESCE(kv.ten, '—') AS khu_vuc, h.ma_hop_dong
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id AND dt.ngay_xoa IS NULL
            LEFT JOIN khu_vuc kv ON kv.id = d.khu_vuc_id
            LEFT JOIN loai_hop_dong l ON l.id = h.loai_hop_dong_id
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
              AND (CAST(:loaiHopDongMa AS text) IS NULL OR l.ma = :loaiHopDongMa)
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND NOT EXISTS (
                    SELECT 1 FROM bien_ban b
                    WHERE b.hop_dong_id = h.id AND b.ngay_xoa IS NULL AND b.hoat_dong = TRUE
                      AND b.loai_bien_ban = 'BAO_CAO_KHAO_SAT' AND b.trang_thai = 'da_duyet'
              )
                                                ORDER BY h.ma_hop_dong
                                                """,
                                                countQuery = """
                                                SELECT COUNT(*)
                                                FROM hop_dong_doi_tuong d
                                                INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                                                LEFT JOIN loai_hop_dong l ON l.id = h.loai_hop_dong_id
                                                WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
                                                        AND (CAST(:loaiHopDongMa AS text) IS NULL OR l.ma = :loaiHopDongMa)
                                                        AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
                                                        AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
                                                        AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
                                                        AND NOT EXISTS (
                                                                                SELECT 1 FROM bien_ban b
                                                                                WHERE b.hop_dong_id = h.id AND b.ngay_xoa IS NULL AND b.hoat_dong = TRUE
                                                                                        AND b.loai_bien_ban = 'BAO_CAO_KHAO_SAT' AND b.trang_thai = 'da_duyet'
                                                        )
                                                """, nativeQuery = true)
                Page<Object[]> findTramThieuKhaoSat(@Param("loaiHopDongMa") String loaiHopDongMa, @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId,
                                                                                                                                                                 @Param("nhaThauId") UUID nhaThauId, Pageable pageable);

    /** row: [hopDongId, maHopDong, ten]. Hợp đồng cần xét checklist — nếu hopDongId null thì xét toàn hệ thống. */
    @Query(value = """
            SELECT h.id, h.ma_hop_dong, h.ten
            FROM hop_dong h
            WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
                      AND d.nha_thau_id = CAST(:nhaThauId AS uuid)))
            ORDER BY h.ma_hop_dong
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM hop_dong h
            WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
                      AND d.nha_thau_id = CAST(:nhaThauId AS uuid)))
            """, nativeQuery = true)
    Page<Object[]> findHopDongForChecklist(@Param("hopDongId") UUID hopDongId, @Param("nhaThauId") UUID nhaThauId,
                                           Pageable pageable);

    /**
     * Đếm biên bản theo trạng thái duyệt trên toàn tập khớp filter. Chỉ lọc theo soft-delete
     * (ngay_xoa IS NULL) — KHÔNG lọc hoat_dong = TRUE, khớp REST BienBanExportServiceImpl.getLichSu()
     * (dùng BienBanRepository.findByHopDongIdAndNgayXoaIsNullOrderBy...), vốn vẫn liệt kê/đếm cả các
     * biên bản đã "hủy hiệu lực" (hoat_dong=false) cùng với trạng thái gốc của chúng — hoat_dong chỉ
     * được REST dùng làm điều kiện gating nghiệp vụ (vd chặn bước tiếp theo), không dùng để ẩn khỏi
     * lịch sử/thống kê hiển thị.
     */
    @Query(value = """
            SELECT b.trang_thai AS nhom, COUNT(*) AS soLuong
            FROM bien_ban b
            INNER JOIN hop_dong h ON h.id = b.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE b.ngay_xoa IS NULL
              AND (CAST(:doiTuongId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL
                      AND (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid))))
              AND (CAST(:fromDate AS date) IS NULL OR b.ngay_lap >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL OR b.ngay_lap <= CAST(:toDate AS date))
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.khu_vuc_id = CAST(:khuVucId AS uuid)))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.tinh_thanh_id = CAST(:tinhThanhId AS uuid)))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.nha_thau_id = CAST(:nhaThauId AS uuid)))
            GROUP BY b.trang_thai
            """, nativeQuery = true)
    List<DemNhomRow> countGroupByTrangThai(@Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId, @Param("hopDongId") UUID hopDongId,
                                           @Param("nhaThauId") UUID nhaThauId, @Param("doiTuongId") UUID doiTuongId,
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    /** Đếm biên bản theo loại (loai_bien_ban). Chỉ lọc soft-delete, không lọc hoat_dong — xem lý do ở countGroupByTrangThai. */
    @Query(value = """
            SELECT b.loai_bien_ban AS nhom, COUNT(*) AS soLuong
            FROM bien_ban b
            INNER JOIN hop_dong h ON h.id = b.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE b.ngay_xoa IS NULL
              AND (CAST(:doiTuongId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL
                      AND (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid))))
              AND (CAST(:fromDate AS date) IS NULL OR b.ngay_lap >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL OR b.ngay_lap <= CAST(:toDate AS date))
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.khu_vuc_id = CAST(:khuVucId AS uuid)))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.tinh_thanh_id = CAST(:tinhThanhId AS uuid)))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.nha_thau_id = CAST(:nhaThauId AS uuid)))
            GROUP BY b.loai_bien_ban
            """, nativeQuery = true)
    List<DemNhomRow> countGroupByLoai(@Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId, @Param("hopDongId") UUID hopDongId,
                                      @Param("nhaThauId") UUID nhaThauId, @Param("doiTuongId") UUID doiTuongId,
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    /** Danh sách biên bản theo trạng thái (lọc trangThai nếu truyền: cho_duyet | da_duyet | tu_choi). */
    @Query(value = """
            SELECT b.ma_bien_ban AS maBienBan, b.loai_bien_ban AS loaiBienBan, h.ma_hop_dong AS maHopDong,
                   h.ten AS tenHopDong, b.trang_thai AS trangThai, b.ngay_lap AS ngayLap,
                   b.nguoi_lap_ten AS nguoiLapTen, b.ly_do_tu_choi AS lyDoTuChoi,
                   b.nguoi_phe_duyet_ten AS nguoiPheDuyetTen, b.ngay_phe_duyet AS ngayPheDuyet
            FROM bien_ban b
            INNER JOIN hop_dong h ON h.id = b.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE b.ngay_xoa IS NULL
              AND (CAST(:doiTuongId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL
                      AND (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid))))
              AND (CAST(:fromDate AS date) IS NULL OR b.ngay_lap >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL OR b.ngay_lap <= CAST(:toDate AS date))
              AND (CAST(:trangThai AS text) IS NULL OR b.trang_thai = :trangThai)
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.khu_vuc_id = CAST(:khuVucId AS uuid)))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.tinh_thanh_id = CAST(:tinhThanhId AS uuid)))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.nha_thau_id = CAST(:nhaThauId AS uuid)))
                ORDER BY b.ngay_lap DESC NULLS LAST, b.ma_bien_ban
                """,
                countQuery = """
                SELECT COUNT(*)
                FROM bien_ban b
                INNER JOIN hop_dong h ON h.id = b.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                WHERE b.ngay_xoa IS NULL
              AND (CAST(:doiTuongId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL
                      AND (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid))))
              AND (CAST(:fromDate AS date) IS NULL OR b.ngay_lap >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL OR b.ngay_lap <= CAST(:toDate AS date))
                  AND (CAST(:trangThai AS text) IS NULL OR b.trang_thai = :trangThai)
                  AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
                  AND (CAST(:khuVucId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.khu_vuc_id = CAST(:khuVucId AS uuid)))
                  AND (CAST(:tinhThanhId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.tinh_thanh_id = CAST(:tinhThanhId AS uuid)))
                  AND (CAST(:nhaThauId AS uuid) IS NULL OR EXISTS (
                          SELECT 1 FROM hop_dong_doi_tuong d
                          WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.nha_thau_id = CAST(:nhaThauId AS uuid)))
                """, nativeQuery = true)
    Page<BienBanTheoTrangThaiRow> findTheoTrangThai(@Param("trangThai") String trangThai,
                                                                    @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId,
                                                                    @Param("hopDongId") UUID hopDongId,
                                                                    @Param("nhaThauId") UUID nhaThauId, @Param("doiTuongId") UUID doiTuongId,
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, Pageable pageable);

    /**
     * Checklist bắt buộc của NHIỀU hợp đồng trong 1 câu (thay cho việc gọi từng hợp đồng).
     * row: [hopDongId, maDanhMuc, tenDanhMuc], theo thứ tự hợp đồng rồi thứ tự danh mục như bản từng hợp đồng.
     */
    @Query(value = """
            SELECT h.id, dm.ma, dm.ten
            FROM kieu_hop_dong_danh_muc_bien_ban k
            INNER JOIN danh_muc_bien_ban dm ON dm.id = k.danh_muc_bien_ban_id AND dm.hoat_dong = TRUE
            INNER JOIN hop_dong h ON h.kieu_hop_dong_id = k.kieu_hop_dong_id
            WHERE h.id IN (:hopDongIds) AND k.hoat_dong = TRUE
            ORDER BY h.id, COALESCE(k.thu_tu, dm.thu_tu_mac_dinh, 0)
            """, nativeQuery = true)
    List<Object[]> checklistForHopDongs(@Param("hopDongIds") java.util.Collection<UUID> hopDongIds);

    /** Mã loại biên bản đã duyệt của NHIỀU hợp đồng trong 1 câu. row: [hopDongId, loaiBienBan]. */
    @Query(value = """
            SELECT DISTINCT b.hop_dong_id, b.loai_bien_ban
            FROM bien_ban b
            WHERE b.hop_dong_id IN (:hopDongIds) AND b.ngay_xoa IS NULL AND b.hoat_dong = TRUE
              AND b.trang_thai = 'da_duyet'
            """, nativeQuery = true)
    List<Object[]> loaiBienBanDaDuyetTheoHopDong(@Param("hopDongIds") java.util.Collection<UUID> hopDongIds);
}
