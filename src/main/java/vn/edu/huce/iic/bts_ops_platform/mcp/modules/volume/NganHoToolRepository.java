package vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.TongQuanAggregateRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.hopdong.HopDong;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Repo tool-riêng cho volume_tool — port lại VolumeService.tongQuan/getChiTiet/khuVucByHopDong
 * thành native SQL trực tiếp trên hop_dong/hop_dong_doi_tuong/hang_muc_*, tránh phụ thuộc
 * VolumeService (module business.volume, dùng chung snapshot/formula service phức tạp hơn nhiều
 * so với những gì tool AI cần).
 */
public interface NganHoToolRepository extends JpaRepository<HopDong, UUID> {

    UUID GCCC_LOAI_HOP_DONG_ID = UUID.fromString("cfe9839a-3b7e-43d4-bd72-0b90e67e0e00");
    UUID XAY_MOI_VA_CONLAI_LOAI_HOP_DONG_ID = UUID.fromString("368d1d28-737a-4d1e-b779-05f4375c3065");
    BigDecimal DEFAULT_HESO_GCCC = BigDecimal.valueOf(2.0);

    /**
     * Tính sẵn cho từng hợp đồng đang hoạt động (đã lọc từ khoá và loại hợp đồng): số trạm, sản lượng thực tế, trạng thái thiếu/thừa,
     * mức cảnh báo, trạng thái vượt ngưỡng và trạng thái quyết toán. Dùng chung cho tổng quan và danh sách để hai bên luôn cùng số.
     */
    String HD_COMPUTED_CTE = """
            WITH hd_base AS (
                SELECT h.id, h.gia_tri_hd, h.tong_thanh_tien_thi_cong AS plan_contract_total,
                       h.loai_hop_dong_id,
                       (h.loai_hop_dong_id = CAST(:gcccLoaiId AS uuid)) AS is_gccc,
                       CASE
                         WHEN h.loai_hop_dong_id = CAST(:gcccLoaiId AS uuid)
                           THEN h.tong_thanh_tien_thi_cong * COALESCE(h.heso_nguong, CAST(:heSoMacDinh AS numeric))
                         ELSE h.tong_thanh_tien_thi_cong * (
                             SELECT heso_nguong FROM loai_hop_dong WHERE id = CAST(:xayMoiConLaiLoaiId AS uuid))
                       END AS threshold_contract
                FROM hop_dong h
                WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                  AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
                  AND ((CAST(:khuVucId AS uuid) IS NULL AND CAST(:tinhThanhId AS uuid) IS NULL)
                       OR EXISTS (SELECT 1 FROM hop_dong_doi_tuong g
                                  WHERE g.hop_dong_id = h.id AND g.ngay_xoa IS NULL AND g.hoat_dong = TRUE
                                    AND (CAST(:khuVucId AS uuid) IS NULL OR g.khu_vuc_id = CAST(:khuVucId AS uuid))
                                    AND (CAST(:tinhThanhId AS uuid) IS NULL OR g.tinh_thanh_id = CAST(:tinhThanhId AS uuid))))
                  AND (CAST(:keyword AS text) IS NULL OR CAST(:keyword AS text) = ''
                       OR LOWER(COALESCE(h.ma_hop_dong, '')) LIKE CAST(:keyword AS text) ESCAPE '\\'
                       OR LOWER(COALESCE(h.ma, '')) LIKE CAST(:keyword AS text) ESCAPE '\\'
                       OR LOWER(COALESCE(h.ten, '')) LIKE CAST(:keyword AS text) ESCAPE '\\'
                       OR EXISTS (SELECT 1 FROM hop_dong_thuoc_tinh ht
                                  WHERE ht.hop_dong_id = h.id AND ht.ngay_xoa IS NULL
                                    AND LOWER(ht.gia_tri) LIKE CAST(:keyword AS text) ESCAPE '\\'))
            ),
            hd_scoped AS (
                SELECT b.*,
                       COALESCE(sc.tram_tong, 0) AS tram_tong,
                       CASE WHEN COALESCE(sc.tram_tong, 0) > 0 THEN b.plan_contract_total / sc.tram_tong ELSE 0 END AS plan_per_tram,
                       CASE WHEN COALESCE(sc.tram_tong, 0) > 0 AND b.threshold_contract IS NOT NULL AND b.threshold_contract > 0
                            THEN b.threshold_contract / sc.tram_tong ELSE NULL END AS threshold_per_tram
                FROM hd_base b
                LEFT JOIN (
                    SELECT hop_dong_id, COUNT(*) AS tram_tong FROM hop_dong_doi_tuong
                    WHERE ngay_xoa IS NULL AND hoat_dong = TRUE GROUP BY hop_dong_id
                ) sc ON sc.hop_dong_id = b.id
            ),
            obj_detail AS (
                SELECT d.hop_dong_id, d.san_luong_hieu_luc, d.quyet_toan_thuc, b.plan_per_tram, b.threshold_per_tram,
                       -- đã quyết toán thì giá trị thực tế = quyết toán, chưa thì = sản lượng hiệu lực (khớp business-api)
                       CASE WHEN COALESCE(d.quyet_toan_thuc, 0) > 0 THEN d.quyet_toan_thuc
                            ELSE COALESCE(d.san_luong_hieu_luc, 0) END AS gia_tri_thuc_te
                FROM hop_dong_doi_tuong d
                JOIN hd_scoped b ON b.id = d.hop_dong_id
                WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
            ),
            obj_agg AS (
                SELECT hop_dong_id,
                       COUNT(*) AS tram_tong,
                       SUM(gia_tri_thuc_te) AS tong_sl_thuc_te,
                       SUM(CASE WHEN plan_per_tram > 0 AND gia_tri_thuc_te < plan_per_tram THEN 1 ELSE 0 END) AS tram_thieu,
                       SUM(CASE WHEN plan_per_tram > 0 AND gia_tri_thuc_te > plan_per_tram THEN 1 ELSE 0 END) AS tram_thua,
                       SUM(CASE WHEN plan_per_tram > 0 AND gia_tri_thuc_te < plan_per_tram THEN plan_per_tram - gia_tri_thuc_te ELSE 0 END) AS gia_tri_thieu,
                       SUM(CASE WHEN plan_per_tram > 0 AND gia_tri_thuc_te > plan_per_tram THEN gia_tri_thuc_te - plan_per_tram ELSE 0 END) AS gia_tri_thua,
                       SUM(CASE WHEN threshold_per_tram IS NOT NULL AND threshold_per_tram > 0 AND COALESCE(san_luong_hieu_luc, 0) > threshold_per_tram THEN 1 ELSE 0 END) AS tram_bat_thuong,
                       COUNT(*) FILTER (WHERE quyet_toan_thuc IS NOT NULL) AS tram_da_qt,
                       SUM(COALESCE(quyet_toan_thuc, 0)) AS tong_quyet_toan
                FROM obj_detail
                GROUP BY hop_dong_id
            ),
            hd_computed AS (
                SELECT b.id, b.gia_tri_hd,
                       COALESCE(a.tram_tong, 0) AS tram_tong,
                       COALESCE(a.tram_bat_thuong, 0) AS tram_bat_thuong,
                       COALESCE(a.tong_sl_thuc_te, 0) AS tong_sl_thuc_te,
                       COALESCE(a.tong_quyet_toan, 0) AS tong_quyet_toan,
                       CASE WHEN b.gia_tri_hd > 0
                            THEN (CAST(b.gia_tri_hd AS numeric) - COALESCE(a.tong_sl_thuc_te, 0)) * 100.0 / b.gia_tri_hd
                            ELSE 0 END AS chenh_lech_percent,
                       -- so tổng giá trị thiếu với tổng giá trị thừa của cả hợp đồng (khớp business-api), không phải "có 1 trạm thiếu"
                       CASE WHEN COALESCE(a.gia_tri_thieu, 0) > COALESCE(a.gia_tri_thua, 0) THEN 'thieu'
                            WHEN COALESCE(a.gia_tri_thua, 0) > COALESCE(a.gia_tri_thieu, 0) THEN 'thua'
                            ELSE 'can_bang' END AS trang_thai_volume,
                       CASE WHEN COALESCE(a.tram_bat_thuong, 0) > 0 THEN 'danger'
                            WHEN COALESCE(a.tram_thieu, 0) > 0 OR (COALESCE(a.tram_thua, 0) - COALESCE(a.tram_bat_thuong, 0)) > 0 THEN 'warning'
                            ELSE 'ok' END AS alert_level,
                       CASE
                         WHEN (CASE WHEN b.is_gccc THEN CAST(b.gia_tri_hd AS numeric) ELSE b.threshold_contract END) IS NULL
                              OR (CASE WHEN b.is_gccc THEN CAST(b.gia_tri_hd AS numeric) ELSE b.threshold_contract END) <= 0
                           THEN 'binh_thuong'
                         WHEN COALESCE(a.tong_sl_thuc_te, 0) > (CASE WHEN b.is_gccc THEN CAST(b.gia_tri_hd AS numeric) ELSE b.threshold_contract END)
                           THEN 'vuot_nguong'
                         WHEN (COALESCE(a.tong_sl_thuc_te, 0) * 100.0 / (CASE WHEN b.is_gccc THEN CAST(b.gia_tri_hd AS numeric) ELSE b.threshold_contract END)) >= CAST(:nguongCanhBao AS numeric)
                           THEN 'canh_bao'
                         ELSE 'binh_thuong'
                       END AS trang_thai,
                       CASE WHEN COALESCE(a.tram_tong, 0) = 0 OR COALESCE(a.tram_da_qt, 0) = 0 THEN 'chua_qt'
                            WHEN COALESCE(a.tram_da_qt, 0) >= COALESCE(a.tram_tong, 0) THEN 'da_qt'
                            ELSE 'dang_qt' END AS trang_thai_qt
                FROM hd_scoped b
                LEFT JOIN obj_agg a ON a.hop_dong_id = b.id
            )
            """;

    /**
     * Port HopDongRepository.tongQuanAggregate — bỏ phần heSoOverrides/contractorScope (tool AI
     * luôn dùng hệ số mặc định, không giới hạn phạm vi nhà thầu).
     */
    @Query(value = HD_COMPUTED_CTE + """
            SELECT
                COUNT(*) AS soHopDong,
                COALESCE(SUM(gia_tri_hd), 0) AS giaTriHopDong,
                COALESCE(SUM(tong_sl_thuc_te), 0) AS tongThanhTienThiCong,
                COALESCE(SUM(tong_quyet_toan), 0) AS tongQuyetToan,
                COALESCE(SUM(tram_tong), 0) AS tongTram,
                COUNT(*) FILTER (WHERE tong_sl_thuc_te > 0) AS soHopDongDaThiCong,
                COALESCE(SUM(tram_tong) FILTER (WHERE tong_sl_thuc_te > 0), 0) AS tongTramDaThiCong,
                COALESCE(SUM(tram_bat_thuong), 0) AS soTramBatThuong,
                COUNT(*) FILTER (WHERE chenh_lech_percent > 10) AS soHopDongThieuLon,
                COUNT(*) FILTER (WHERE trang_thai = 'vuot_nguong') AS soVuotNguong,
                COUNT(*) FILTER (WHERE alert_level = 'danger') AS soCanhBao,
                COUNT(*) FILTER (WHERE trang_thai_volume = 'thieu') AS soThieu,
                COUNT(*) FILTER (WHERE trang_thai_volume = 'thua') AS soThua,
                COUNT(*) FILTER (WHERE trang_thai_volume = 'can_bang') AS soCanBang,
                COUNT(*) FILTER (WHERE trang_thai_qt = 'da_qt') AS soDaQuyetToan,
                COUNT(*) FILTER (WHERE trang_thai_qt = 'dang_qt') AS soDangQuyetToan,
                COUNT(*) FILTER (WHERE trang_thai_qt = 'chua_qt') AS soChuaQuyetToan,
                COALESCE(SUM(tram_tong) FILTER (WHERE trang_thai_qt = 'da_qt'), 0) AS tongTramDaQuyetToan,
                COALESCE(SUM(tram_tong) FILTER (WHERE trang_thai_qt = 'dang_qt'), 0) AS tongTramDangQuyetToan,
                COALESCE(SUM(tram_tong) FILTER (WHERE trang_thai_qt = 'chua_qt'), 0) AS tongTramChuaQuyetToan,
                COALESCE(SUM(tong_quyet_toan) FILTER (WHERE trang_thai_qt = 'da_qt'), 0) AS giaTriDaQuyetToan,
                COALESCE(SUM(tong_quyet_toan) FILTER (WHERE trang_thai_qt = 'dang_qt'), 0) AS giaTriDangQuyetToan,
                COALESCE(SUM(tong_quyet_toan) FILTER (WHERE trang_thai_qt = 'chua_qt'), 0) AS giaTriChuaQuyetToan
            FROM hd_computed
            """, nativeQuery = true)
    TongQuanAggregateRow tongQuanAggregate(@Param("keyword") String keyword,
                                            @Param("gcccLoaiId") UUID gcccLoaiId,
                                            @Param("xayMoiConLaiLoaiId") UUID xayMoiConLaiLoaiId,
                                            @Param("heSoMacDinh") BigDecimal heSoMacDinh,
                                            @Param("nguongCanhBao") BigDecimal nguongCanhBao,
                                            @Param("loaiHopDongId") UUID loaiHopDongId,
                                            @Param("khuVucId") UUID khuVucId,
                                            @Param("tinhThanhId") UUID tinhThanhId);

    @Query(value = """
            SELECT COALESCE(lh.ten, CAST(h.loai_hop_dong_id AS text)), COUNT(*)
            FROM hop_dong h
            LEFT JOIN loai_hop_dong lh ON lh.id = h.loai_hop_dong_id
            WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
              AND (CAST(:keyword AS text) IS NULL OR CAST(:keyword AS text) = ''
                   OR LOWER(COALESCE(h.ma_hop_dong, '')) LIKE CAST(:keyword AS text) ESCAPE '\\'
                   OR LOWER(COALESCE(h.ma, '')) LIKE CAST(:keyword AS text) ESCAPE '\\'
                   OR LOWER(COALESCE(h.ten, '')) LIKE CAST(:keyword AS text) ESCAPE '\\'
                   OR EXISTS (SELECT 1 FROM hop_dong_thuoc_tinh ht
                              WHERE ht.hop_dong_id = h.id AND ht.ngay_xoa IS NULL
                                AND LOWER(ht.gia_tri) LIKE CAST(:keyword AS text) ESCAPE '\\'))
              AND h.loai_hop_dong_id IS NOT NULL
              AND ((CAST(:khuVucId AS uuid) IS NULL AND CAST(:tinhThanhId AS uuid) IS NULL)
                   OR EXISTS (SELECT 1 FROM hop_dong_doi_tuong g
                              WHERE g.hop_dong_id = h.id AND g.ngay_xoa IS NULL AND g.hoat_dong = TRUE
                                AND (CAST(:khuVucId AS uuid) IS NULL OR g.khu_vuc_id = CAST(:khuVucId AS uuid))
                                AND (CAST(:tinhThanhId AS uuid) IS NULL OR g.tinh_thanh_id = CAST(:tinhThanhId AS uuid))))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
            GROUP BY 1
            """, nativeQuery = true)
    List<Object[]> tongQuanTheoLoai(@Param("keyword") String keyword, @Param("loaiHopDongId") UUID loaiHopDongId,
                                            @Param("khuVucId") UUID khuVucId,
                                            @Param("tinhThanhId") UUID tinhThanhId);

    /**
     * Danh sách hợp đồng theo trạng thái ngân sách, cùng tập và cùng cách tính với {@link #tongQuanAggregate} (khớp REST volume/hop-dong/danh-sach).
     * {@code statusFilter}: thieu | thua | can_bang | alert | da_qt | dang_qt | chua_qt (null = tất cả).
     * row: [id, maHopDong, ten, giaTriHd, tongSanLuongThucTe, soTram, trangThai, trangThaiVolume, alertLevel, trangThaiQt, chenhLechPercent].
     */
    @Query(value = HD_COMPUTED_CTE + """
            SELECT c.id, h.ma_hop_dong, h.ten, c.gia_tri_hd, c.tong_sl_thuc_te, c.tram_tong,
                   c.trang_thai, c.trang_thai_volume, c.alert_level, c.trang_thai_qt, c.chenh_lech_percent
            FROM hd_computed c
            JOIN hop_dong h ON h.id = c.id
            WHERE (CAST(:statusFilter AS text) IS NULL
                   OR (:statusFilter IN ('thieu', 'thua', 'can_bang') AND c.trang_thai_volume = :statusFilter)
                   OR (:statusFilter = 'alert' AND c.alert_level = 'danger')
                   OR (:statusFilter IN ('da_qt', 'dang_qt', 'chua_qt') AND c.trang_thai_qt = :statusFilter))
            ORDER BY h.ma_hop_dong, c.id
            """,
            countQuery = HD_COMPUTED_CTE + """
            SELECT COUNT(*)
            FROM hd_computed c
            WHERE (CAST(:statusFilter AS text) IS NULL
                   OR (:statusFilter IN ('thieu', 'thua', 'can_bang') AND c.trang_thai_volume = :statusFilter)
                   OR (:statusFilter = 'alert' AND c.alert_level = 'danger')
                   OR (:statusFilter IN ('da_qt', 'dang_qt', 'chua_qt') AND c.trang_thai_qt = :statusFilter))
            """, nativeQuery = true)
    org.springframework.data.domain.Page<Object[]> danhSachHopDong(@Param("keyword") String keyword,
                                            @Param("gcccLoaiId") UUID gcccLoaiId,
                                            @Param("xayMoiConLaiLoaiId") UUID xayMoiConLaiLoaiId,
                                            @Param("heSoMacDinh") BigDecimal heSoMacDinh,
                                            @Param("nguongCanhBao") BigDecimal nguongCanhBao,
                                            @Param("loaiHopDongId") UUID loaiHopDongId,
                                            @Param("statusFilter") String statusFilter,
                                            @Param("khuVucId") UUID khuVucId,
                                            @Param("tinhThanhId") UUID tinhThanhId,
                                            org.springframework.data.domain.Pageable pageable);

    /**
     * Hệ số GCCC mặc định cấu hình trên danh mục loại hợp đồng (loai_hop_dong.heso_nguong) — khớp
     * cách REST {@code VolumeServiceImpl.loadGcccHeSoFromDb()} đọc cấu hình trước khi fallback về
     * hằng số {@link #DEFAULT_HESO_GCCC}. Hệ số override RIÊNG theo từng hợp đồng cụ thể
     * (hop_dong.heso_nguong) đã được áp trực tiếp trong {@link #tongQuanAggregate}.
     */
    @Query(value = "SELECT heso_nguong FROM loai_hop_dong WHERE id = CAST(:loaiHopDongId AS uuid)", nativeQuery = true)
    BigDecimal findHeSoNguongByLoaiHopDongId(@Param("loaiHopDongId") UUID loaiHopDongId);

    // ---- getChiTiet: cây hạng mục (hang_muc_nhom -> hang_muc_chi_tiet -> hang_muc_cong_viec) ----

    /** row: [id, ma, ten, thu_tu]. */
    @Query(value = """
            SELECT id, ma, ten, thu_tu FROM hang_muc_nhom
            WHERE hop_dong_id = :hopDongId AND ngay_xoa IS NULL AND hoat_dong = TRUE
            ORDER BY thu_tu, ma
            """, nativeQuery = true)
    List<Object[]> findNhomByHopDongId(@Param("hopDongId") UUID hopDongId);

    /** row: [id, hang_muc_nhom_id, ma, ten, don_gia, khoi_luong, cong_thuc_khoi_luong, cong_thuc_don_gia, cong_thuc_thanh_tien]. */
    @Query(value = """
            SELECT id, hang_muc_nhom_id, ma, ten, don_gia, khoi_luong, cong_thuc_khoi_luong, cong_thuc_don_gia, cong_thuc_thanh_tien
            FROM hang_muc_chi_tiet
            WHERE hang_muc_nhom_id IN :nhomIds AND ngay_xoa IS NULL AND hoat_dong = TRUE
            ORDER BY ma
            """, nativeQuery = true)
    List<Object[]> findChiTietByNhomIds(@Param("nhomIds") Collection<UUID> nhomIds);

    /** row: [id, hang_muc_chi_tiet_id, ma, ten, don_gia, khoi_luong, cong_thuc_khoi_luong, cong_thuc_don_gia, cong_thuc_thanh_tien, thu_tu]. */
    @Query(value = """
            SELECT id, hang_muc_chi_tiet_id, ma, ten, don_gia, khoi_luong, cong_thuc_khoi_luong, cong_thuc_don_gia, cong_thuc_thanh_tien, thu_tu
            FROM hang_muc_cong_viec
            WHERE hang_muc_chi_tiet_id IN :chiTietIds AND ngay_xoa IS NULL
            ORDER BY thu_tu, ma
            """, nativeQuery = true)
    List<Object[]> findCongViecByChiTietIds(@Param("chiTietIds") Collection<UUID> chiTietIds);

    /**
     * row: [hang_muc_cong_viec_id, tongKhoiLuong] — KL từ sản lượng khi hạng mục công việc chưa nhập KL.
     * CỐ Ý không lọc {@code hoat_dong}: REST {@code /volume/hop-dong/{id}} (số FE hiển thị) cũng cộng cả bản ghi này;
     * thêm lọc sẽ làm tongThanhTienThiCong lệch FE (đã đo: hợp đồng 31032026VTK2025, 6,125 tỷ so với 6,074 tỷ).
     */
    @Query(value = """
            SELECT hang_muc_cong_viec_id, COALESCE(SUM(khoi_luong_hoan_thanh), 0)
            FROM san_luong
            WHERE ngay_xoa IS NULL AND hop_dong_id = :hopDongId AND hang_muc_cong_viec_id IS NOT NULL
            GROUP BY hang_muc_cong_viec_id
            """, nativeQuery = true)
    List<Object[]> sumKhoiLuongTheoCongViec(@Param("hopDongId") UUID hopDongId);

    /** row: [hang_muc_chi_tiet_id, tongKhoiLuong] — chỉ tính khi hạng mục KHÔNG có công việc con. */
    @Query(value = """
            SELECT hang_muc_chi_tiet_id, COALESCE(SUM(khoi_luong_hoan_thanh), 0)
            FROM san_luong
            WHERE ngay_xoa IS NULL AND hop_dong_id = :hopDongId
              AND hang_muc_chi_tiet_id IS NOT NULL AND hang_muc_cong_viec_id IS NULL
            GROUP BY hang_muc_chi_tiet_id
            """, nativeQuery = true)
    List<Object[]> sumKhoiLuongTheoChiTiet(@Param("hopDongId") UUID hopDongId);

    // ---- khuVucByHopDong: nhóm theo khu vực/tỉnh của hop_dong_doi_tuong ----

    /** row: [khuVucId, khuVucTen, tinhId, tinhTen, nhaThauTen, sanLuongHieuLuc, maTram]. */
    @Query(value = """
            SELECT kv.id, COALESCE(kv.ten, '—'), tt.id, COALESCE(tt.ten, '—'), nt.ho_ten,
                   d.san_luong_hieu_luc, COALESCE(dt.ma, dt.ten, '—')
            FROM hop_dong_doi_tuong d
            LEFT JOIN khu_vuc kv ON kv.id = d.khu_vuc_id
            LEFT JOIN tinh_thanh tt ON tt.id = d.tinh_thanh_id
            LEFT JOIN nguoi_dung nt ON nt.id = d.nha_thau_id
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id AND dt.ngay_xoa IS NULL
            WHERE d.hop_dong_id = :hopDongId AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
            """, nativeQuery = true)
    List<Object[]> findObjectGeoByHopDongId(@Param("hopDongId") UUID hopDongId);

    /** Như findChiTietByNhomIds nhưng theo hợp đồng (JOIN nhóm) — 1 lần khứ hồi, chạy song song với truy vấn nhóm. */
    @Query(value = """
            SELECT c.id, c.hang_muc_nhom_id, c.ma, c.ten, c.don_gia, c.khoi_luong, c.cong_thuc_khoi_luong, c.cong_thuc_don_gia, c.cong_thuc_thanh_tien
            FROM hang_muc_chi_tiet c
            JOIN hang_muc_nhom n ON n.id = c.hang_muc_nhom_id AND n.hop_dong_id = :hopDongId AND n.ngay_xoa IS NULL AND n.hoat_dong = TRUE
            WHERE c.ngay_xoa IS NULL AND c.hoat_dong = TRUE
            ORDER BY c.ma
            """, nativeQuery = true)
    List<Object[]> findChiTietByHopDongId(@Param("hopDongId") UUID hopDongId);

    /** Như findCongViecByChiTietIds nhưng theo hợp đồng (JOIN chi tiết, nhóm). */
    @Query(value = """
            SELECT v.id, v.hang_muc_chi_tiet_id, v.ma, v.ten, v.don_gia, v.khoi_luong, v.cong_thuc_khoi_luong, v.cong_thuc_don_gia, v.cong_thuc_thanh_tien, v.thu_tu
            FROM hang_muc_cong_viec v
            JOIN hang_muc_chi_tiet c ON c.id = v.hang_muc_chi_tiet_id AND c.ngay_xoa IS NULL AND c.hoat_dong = TRUE
            JOIN hang_muc_nhom n ON n.id = c.hang_muc_nhom_id AND n.hop_dong_id = :hopDongId AND n.ngay_xoa IS NULL AND n.hoat_dong = TRUE
            WHERE v.ngay_xoa IS NULL
            ORDER BY v.thu_tu, v.ma
            """, nativeQuery = true)
    List<Object[]> findCongViecByHopDongId(@Param("hopDongId") UUID hopDongId);


    /** Hợp đồng đang hoạt động có ít nhất 1 đối tượng đang hoạt động (phạm vi tính KPI dashboard); lọc hợp đồng/loại nếu truyền. */
    @Query(value = """
            SELECT h.id FROM hop_dong h
            WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND EXISTS (SELECT 1 FROM hop_dong_doi_tuong d
                          WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE)
            """, nativeQuery = true)
    List<UUID> findHopDongIdsCoDoiTuong(@Param("hopDongId") UUID hopDongId, @Param("loaiHopDongId") UUID loaiHopDongId);

    /**
     * Trạm có sản lượng vượt ngưỡng bình quân hợp đồng — port DashboardServiceImpl.sanLuongBatThuong (ticker "Trạm sản lượng
     * bất thường" trên Dashboard), KHÁC công thức soTramBatThuong của màn Kiểm soát Volume:
     * bình quân = giá trị HĐ / số trạm (đối tượng đang hoạt động, bỏ hạng mục/BOQ), ngưỡng = bình quân × hệ số
     * (hệ số HĐ; trống thì 2.0; ≤ 0 thì: GCCC bỏ qua, loại khác lấy hệ số loại → hệ số loại "Xây mới & còn lại" → 2.0),
     * vượt khi thành tiền sản lượng hoàn thành (done/survey/design) của trạm > ngưỡng. Xếp theo tỷ lệ so với bình quân giảm dần.
     * row: [maTram, maHopDong, tenHopDong, khuVuc, sanLuong, binhQuan, heSo, nguong, tyLe, daQuyetToan, daKiemTra, tong].
     */
    @Query(value = """
            WITH sl AS (
                SELECT d.id AS dt_id, d.hop_dong_id,
                       SUM(COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0)) AS san_luong
                FROM san_luong s
                JOIN hop_dong_doi_tuong d ON d.id = s.hop_dong_doi_tuong_id AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
                JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL
                WHERE s.ngay_xoa IS NULL AND s.hoat_dong = TRUE
                  AND LOWER(COALESCE(s.trang_thai, '')) IN ('done', 'survey', 'design')
                GROUP BY d.id, d.hop_dong_id
            ), tram AS (
                SELECT d.hop_dong_id, COUNT(*) AS so_tram
                FROM hop_dong_doi_tuong d
                JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                JOIN doi_tuong_quan_ly q ON q.id = d.doi_tuong_quan_ly_id AND q.ngay_xoa IS NULL
                WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
                  AND d.doi_tuong_quan_ly_id <> CAST('d77c2f9d-b5ca-48f6-95fe-6574c9fda5b2' AS uuid)
                  AND LOWER(CONCAT(COALESCE(q.ten, ''), ' ', COALESCE(q.ma, ''))) NOT LIKE '%boq%'
                  AND LOWER(CONCAT(COALESCE(q.ten, ''), ' ', COALESCE(q.ma, ''))) NOT LIKE '%hang muc%'
                  AND LOWER(CONCAT(COALESCE(q.ten, ''), ' ', COALESCE(q.ma, ''))) NOT LIKE '%hạng mục%'
                GROUP BY d.hop_dong_id
            ), hd AS (
                SELECT h.id, h.ma_hop_dong, h.ten, h.gia_tri_hd, h.loai_hop_dong_id,
                       CASE WHEN h.heso_nguong IS NULL THEN 2.0
                            WHEN h.heso_nguong > 0 THEN h.heso_nguong
                            WHEN h.loai_hop_dong_id = CAST(:gcccLoaiId AS uuid) THEN NULL
                            ELSE (SELECT CASE WHEN l.heso_nguong IS NULL THEN 2.0
                                              WHEN l.heso_nguong > 0 THEN l.heso_nguong
                                              ELSE (SELECT CASE WHEN x.heso_nguong IS NULL THEN 2.0
                                                                WHEN x.heso_nguong > 0 THEN x.heso_nguong END
                                                    FROM loai_hop_dong x WHERE x.id = CAST(:xayMoiConLaiLoaiId AS uuid)) END
                                  FROM loai_hop_dong l WHERE l.id = h.loai_hop_dong_id)
                       END AS he_so
                FROM hop_dong h
                WHERE h.ngay_xoa IS NULL AND h.gia_tri_hd IS NOT NULL AND h.gia_tri_hd > 0
                  AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
                  AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
            ), cand AS (
                SELECT sl.dt_id, hd.ma_hop_dong, hd.ten, sl.san_luong,
                       ROUND(CAST(hd.gia_tri_hd AS numeric) / t.so_tram, 2) AS binh_quan, hd.he_so
                FROM sl
                JOIN hd ON hd.id = sl.hop_dong_id
                JOIN tram t ON t.hop_dong_id = sl.hop_dong_id AND t.so_tram > 0
                WHERE hd.he_so IS NOT NULL
            ), vuot AS (
                SELECT c.*, c.binh_quan * c.he_so AS nguong,
                       CASE WHEN c.binh_quan = 0 THEN 0 ELSE ROUND(c.san_luong / c.binh_quan, 4) END AS ty_le
                FROM cand c
                WHERE c.san_luong > c.binh_quan * c.he_so
            )
            SELECT (SELECT g.gia_tri FROM hop_dong_doi_tuong_gia_tri g
                      JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.la_khoa_chinh = TRUE AND tt.ngay_xoa IS NULL
                     WHERE g.ngay_xoa IS NULL AND g.hop_dong_doi_tuong_id = v.dt_id LIMIT 1),
                   v.ma_hop_dong, v.ten, kv.ten, v.san_luong, v.binh_quan, v.he_so, v.nguong, v.ty_le,
                   (UPPER(TRIM(COALESCE(st.ma, ''))) = 'QT'),
                   EXISTS (SELECT 1 FROM san_luong_canh_bao_kiem_tra k WHERE k.hop_dong_doi_tuong_id = v.dt_id),
                   COUNT(*) OVER ()
            FROM vuot v
            JOIN hop_dong_doi_tuong d ON d.id = v.dt_id
            LEFT JOIN khu_vuc kv ON kv.id = d.khu_vuc_id
            LEFT JOIN trang_thai_hop_dong st ON st.id = d.trang_thai_hop_dong_id
            WHERE (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
            ORDER BY v.ty_le DESC, v.dt_id
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Object[]> findTramVuotNguongBinhQuan(@Param("gcccLoaiId") UUID gcccLoaiId,
                                              @Param("xayMoiConLaiLoaiId") UUID xayMoiConLaiLoaiId,
                                              @Param("hopDongId") UUID hopDongId, @Param("loaiHopDongId") UUID loaiHopDongId,
                                              @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId,
                                              @Param("limit") int limit, @Param("offset") int offset);
}
