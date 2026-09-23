package vn.edu.huce.iic.bts_ops_platform.mcp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.tramton.ThieuNhieuDieuKienRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.tramton.XepHangKhuVucRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.doituong.HopDongDoiTuong;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Trạm tồn — bám đúng định nghĩa của REST {@code TramTonServiceImpl.tongQuan} (trang "Trạm tồn" của FE):
 * <ul>
 *   <li><b>Chờ quyết toán / Quá hạn</b>: đối tượng đang hoạt động, hợp đồng đủ pháp lý (trang_thai_phap_ly = 2),
 *       KHÔNG có vướng mắc mở, CHƯA quyết toán, và ĐÃ hoàn thành thi công (có ngày mốc {@code resolved_ngay_ht}).
 *       Số ngày tồn = hôm nay (múi giờ Asia/Ho_Chi_Minh) - ngày mốc; &gt;= ngưỡng {@code thresholdDays} là quá hạn.</li>
 *   <li><b>Chưa pháp lý</b>: đối tượng đang hoạt động thuộc hợp đồng chưa đủ pháp lý.</li>
 *   <li><b>Vướng mắc</b>: vướng mắc pending/in_progress đã tạo TRƯỚC mốc (hôm nay - ngưỡng), gộp mỗi đối tượng
 *       1 dòng (lâu nhất), bỏ đối tượng đã nằm ở nhóm chưa pháp lý.</li>
 * </ul>
 */
public interface TramTonToolRepository extends JpaRepository<HopDongDoiTuong, UUID> {

    /** Id đối tượng quản lý "Hạng mục thi công" — loại khỏi số trạm khi chia đều giá trị hợp đồng (khớp REST). */
    String HANG_MUC_DOI_TUONG_ID = "d77c2f9d-b5ca-48f6-95fe-6574c9fda5b2";

    /** Ngày hôm nay theo múi giờ nghiệp vụ (REST dùng LocalDate.now(Asia/Ho_Chi_Minh)). */
    String TODAY_HCM = "(NOW() AT TIME ZONE 'Asia/Ho_Chi_Minh')::date";

    /** Số trạm (đối tượng hoạt động, không tính hạng mục/BOQ) của từng hợp đồng — mẫu số chia đều giá trị hợp đồng. */
    String SO_TRAM_THEO_HOP_DONG = """
            SELECT x.hop_dong_id AS hop_dong_id, COUNT(*) AS so_tram
            FROM hop_dong_doi_tuong x
            JOIN hop_dong hx ON hx.id = x.hop_dong_id AND hx.ngay_xoa IS NULL AND hx.hoat_dong = TRUE
            JOIN doi_tuong_quan_ly q ON q.id = x.doi_tuong_quan_ly_id AND q.ngay_xoa IS NULL
            WHERE x.ngay_xoa IS NULL AND x.hoat_dong = TRUE
              AND x.doi_tuong_quan_ly_id <> CAST('d77c2f9d-b5ca-48f6-95fe-6574c9fda5b2' AS uuid)
              AND LOWER(CONCAT(COALESCE(q.ten, ''), ' ', COALESCE(q.ma, ''))) NOT LIKE '%boq%'
              AND LOWER(CONCAT(COALESCE(q.ten, ''), ' ', COALESCE(q.ma, ''))) NOT LIKE '%hang muc%'
              AND LOWER(CONCAT(COALESCE(q.ten, ''), ' ', COALESCE(q.ma, ''))) NOT LIKE '%hạng mục%'
            GROUP BY x.hop_dong_id
            """;

    /** Giá trị 1 trạm: sản lượng hiệu lực nếu &gt; 0, ngược lại chia đều giá trị hợp đồng (khớp REST resolveGiaTri/chiaDeuGiaTriHd). */
    String GIA_TRI_TRAM = """
            CASE
                WHEN COALESCE(d.san_luong_hieu_luc, 0) > 0 THEN d.san_luong_hieu_luc
                WHEN COALESCE(oc.so_tram, 0) > 0 AND COALESCE(h.gia_tri_hd, 0) > 0
                    THEN ROUND(CAST(h.gia_tri_hd AS numeric) / oc.so_tram, 2)
                ELSE 0
            END
            """;

    /**
     * Ứng viên "đã hoàn thành thi công, đủ pháp lý, chưa quyết toán, không vướng mắc" — tập cha của chờ quyết toán
     * và quá hạn (tách theo ngưỡng ngày ở truy vấn {@code thongKeChoQuyetToanVaQuaHan}).
     */
    String CHO_QUYET_TOAN_CANDIDATE = """
            SELECT d.id AS id, d.hop_dong_id AS hop_dong_id, d.doi_tuong_quan_ly_id AS doi_tuong_quan_ly_id,
                   d.khu_vuc_id AS khu_vuc_id, d.tinh_thanh_id AS tinh_thanh_id, d.nha_thau_id AS nha_thau_id, d.san_luong_hieu_luc AS san_luong_hieu_luc,
                   h.loai_hop_dong_id AS loai_hop_dong_id, h.ma_hop_dong AS ma_hop_dong, h.ten AS ten_hop_dong,
                   """ + GIA_TRI_TRAM + """
                   AS gia_tri,
                   CASE
                       WHEN d.ngay_ht_tc IS NOT NULL THEN d.ngay_ht_tc
                       WHEN (
                             UPPER(COALESCE(t.ma, '')) = 'HT'
                             OR UPPER(COALESCE(t.ma, '')) = 'QT'
                             OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYET TOAN%'
                             OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYẾT TOÁN%'
                             OR UPPER(COALESCE(t.ma, '')) LIKE '%HOAN THANH%'
                             OR UPPER(COALESCE(t.ma, '')) LIKE '%HOÀN THÀNH%'
                            ) AND d.ngay_cap_nhat IS NOT NULL
                       THEN (d.ngay_cap_nhat AT TIME ZONE 'Asia/Ho_Chi_Minh')::date
                       ELSE NULL
                   END AS resolved_ngay_ht
            FROM hop_dong_doi_tuong d
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.hoat_dong = TRUE AND h.ngay_xoa IS NULL
            LEFT JOIN trang_thai_hop_dong t ON t.id = d.trang_thai_hop_dong_id
            LEFT JOIN (""" + SO_TRAM_THEO_HOP_DONG + """
            ) oc ON oc.hop_dong_id = d.hop_dong_id
            WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
              AND d.co_vuong_mac_mo = FALSE
              AND h.trang_thai_phap_ly = 2
              AND NOT (
                    d.quyet_toan_thuc IS NOT NULL
                    OR UPPER(COALESCE(t.ma, '')) = 'QT'
                    OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYET TOAN%'
                    OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYẾT TOÁN%'
                  )
            """;

    /** Bộ lọc dùng chung cho các truy vấn tổng quan, theo ID đã resolve (null = không lọc); alias bảng ứng viên là {@code c}. */
    String LOC_UNG_VIEN = """
            WHERE (CAST(:khuVucId AS uuid) IS NULL OR c.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR c.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR c.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR c.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR c.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (c.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR c.id = CAST(:doiTuongId AS uuid)))
            """;

    /** Nhóm nhóm tuổi tồn (khớp TramTonTinhToanHelper.agingBucketId: 7/14/28/90 ngày). */
    String NHOM_TUOI = """
            CASE
                WHEN GREATEST(0, """ + TODAY_HCM + """
                     - c.resolved_ngay_ht) < 7 THEN 'lt_1w'
                WHEN GREATEST(0, """ + TODAY_HCM + """
                     - c.resolved_ngay_ht) < 14 THEN '1_2w'
                WHEN GREATEST(0, """ + TODAY_HCM + """
                     - c.resolved_ngay_ht) < 28 THEN '2_4w'
                WHEN GREATEST(0, """ + TODAY_HCM + """
                     - c.resolved_ngay_ht) < 90 THEN '1_3m'
                ELSE 'gt_3m'
            END
            """;

    /**
     * Chờ quyết toán vs Quá hạn, theo nhóm tuổi. row: [nhom ('cho_qt'|'qua_han'), tuoi, số trạm, tổng giá trị, tổng giá trị đã cắt phần thập phân từng dòng (cách REST cộng nhóm)].
     * Quá hạn khi số ngày tồn &gt;= {@code thresholdDays} (khớp REST: resolved_ngay_ht &lt;= hôm nay - ngưỡng).
     */
    @Query(value = """
            SELECT CASE WHEN (""" + TODAY_HCM + """
                        - c.resolved_ngay_ht) >= CAST(:thresholdDays AS integer) THEN 'qua_han' ELSE 'cho_qt' END AS nhom,
                   """ + NHOM_TUOI + """
                   AS tuoi,
                   COUNT(*) AS cnt,
                   COALESCE(SUM(c.gia_tri), 0) AS gia_tri,
                   COALESCE(SUM(TRUNC(c.gia_tri)), 0) AS gia_tri_nguyen
            FROM (""" + CHO_QUYET_TOAN_CANDIDATE + """
            ) c
            """ + LOC_UNG_VIEN + """
              AND c.resolved_ngay_ht IS NOT NULL
              AND (CAST(:fromDate AS date) IS NULL OR c.resolved_ngay_ht >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL OR c.resolved_ngay_ht <= CAST(:toDate AS date))
            GROUP BY 1, 2
            """, nativeQuery = true)
    List<Object[]> thongKeChoQuyetToanVaQuaHan(@Param("thresholdDays") int thresholdDays,
                                                @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId, @Param("nhaThauId") UUID nhaThauId,
                                                @Param("hopDongId") UUID hopDongId, @Param("doiTuongId") UUID doiTuongId,
                                                @Param("loaiHopDongId") UUID loaiHopDongId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    /** Chưa pháp lý (hợp đồng chưa đủ pháp lý). row: [số trạm, tổng giá trị, tổng giá trị đã cắt phần thập phân]. */
    @Query(value = """
            SELECT COUNT(*) AS cnt, COALESCE(SUM(c.gia_tri), 0) AS gia_tri,
                   COALESCE(SUM(TRUNC(c.gia_tri)), 0) AS gia_tri_nguyen
            FROM (
                SELECT d.id AS id, d.hop_dong_id AS hop_dong_id, d.doi_tuong_quan_ly_id AS doi_tuong_quan_ly_id,
                       d.khu_vuc_id AS khu_vuc_id, d.tinh_thanh_id AS tinh_thanh_id, d.nha_thau_id AS nha_thau_id,
                       h.loai_hop_dong_id AS loai_hop_dong_id,
                       """ + GIA_TRI_TRAM + """
                       AS gia_tri
                FROM hop_dong_doi_tuong d
                JOIN hop_dong h ON h.id = d.hop_dong_id AND h.hoat_dong = TRUE AND h.ngay_xoa IS NULL
                LEFT JOIN (""" + SO_TRAM_THEO_HOP_DONG + """
                ) oc ON oc.hop_dong_id = d.hop_dong_id
                WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
                  AND (h.trang_thai_phap_ly IS NULL OR h.trang_thai_phap_ly <> 2)
            ) c
            """ + LOC_UNG_VIEN, nativeQuery = true)
    List<Object[]> thongKeChuaPhapLy(@Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId, @Param("nhaThauId") UUID nhaThauId,
                                     @Param("hopDongId") UUID hopDongId, @Param("doiTuongId") UUID doiTuongId,
                                                @Param("loaiHopDongId") UUID loaiHopDongId);

    /**
     * Vướng mắc đang mở đã quá ngưỡng, mỗi đối tượng 1 dòng (vướng mắc lâu nhất), bỏ đối tượng thuộc nhóm chưa pháp lý.
     * row: [số ngày tồn (từ ngày tạo vướng mắc), giá trị (sản lượng hiệu lực của đối tượng), giá trị đã cắt phần thập phân].
     */
    @Query(value = """
            SELECT GREATEST(0, """ + TODAY_HCM + """
                        - (c.ngay_tao AT TIME ZONE 'Asia/Ho_Chi_Minh')::date) AS so_ngay,
                   CASE WHEN COALESCE(c.san_luong_hieu_luc, 0) > 0 THEN c.san_luong_hieu_luc ELSE 0 END AS gia_tri,
                   CASE WHEN COALESCE(c.san_luong_hieu_luc, 0) > 0 THEN TRUNC(c.san_luong_hieu_luc) ELSE 0 END AS gia_tri_nguyen
            FROM (
                SELECT v.id AS vid, d.id AS id, v.ngay_tao AS ngay_tao, d.san_luong_hieu_luc AS san_luong_hieu_luc,
                       d.khu_vuc_id AS khu_vuc_id, d.tinh_thanh_id AS tinh_thanh_id, d.nha_thau_id AS nha_thau_id,
                       d.doi_tuong_quan_ly_id AS doi_tuong_quan_ly_id, v.hop_dong_id AS hop_dong_id, h.loai_hop_dong_id AS loai_hop_dong_id,
                       ROW_NUMBER() OVER (PARTITION BY COALESCE(v.du_lieu_doi_tuong_id, v.id)
                                          ORDER BY v.ngay_tao ASC, v.id ASC) AS rn
                FROM vuong_mac v
                LEFT JOIN hop_dong h ON h.id = v.hop_dong_id AND h.ngay_xoa IS NULL
                LEFT JOIN hop_dong_doi_tuong d ON d.id = v.du_lieu_doi_tuong_id AND d.ngay_xoa IS NULL
                WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
                  AND v.trang_thai IN ('pending', 'in_progress')
                  AND v.ngay_tao < ((""" + TODAY_HCM + """
                        - CAST(:thresholdDays AS integer))::timestamp AT TIME ZONE 'Asia/Ho_Chi_Minh')
                  AND NOT (d.id IS NOT NULL AND d.hoat_dong = TRUE AND h.hoat_dong = TRUE
                           AND (h.trang_thai_phap_ly IS NULL OR h.trang_thai_phap_ly <> 2))
            ) c
            """ + LOC_UNG_VIEN + """
              AND c.rn = 1
            """, nativeQuery = true)
    List<Object[]> thongKeVuongMacQuaNguong(@Param("thresholdDays") int thresholdDays,
                                            @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId, @Param("nhaThauId") UUID nhaThauId,
                                            @Param("hopDongId") UUID hopDongId, @Param("doiTuongId") UUID doiTuongId,
                                                @Param("loaiHopDongId") UUID loaiHopDongId);

    /** Danh sách đối tượng tồn (đã hoàn thành thi công, chờ quyết toán — cùng tập với tổng quan) theo bộ lọc id đã resolve (nhà thầu/hợp đồng/khu vực/đối tượng). row khớp CHO_QUYET_TOAN_CANDIDATE + enrich tên. */
    @Query(value = """
            SELECT c.id, c.hop_dong_id, c.ma_hop_dong, c.ten_hop_dong, COALESCE(dt.ten, dt.ma, '—') AS ma_tram,
                   COALESCE(kv.ten, '—') AS khu_vuc, COALESCE(nd.ho_ten, '—') AS nha_thau,
                   c.san_luong_hieu_luc, c.resolved_ngay_ht
            FROM (""" + CHO_QUYET_TOAN_CANDIDATE + """
            ) c
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = c.doi_tuong_quan_ly_id AND dt.ngay_xoa IS NULL
            LEFT JOIN khu_vuc kv ON kv.id = c.khu_vuc_id
            LEFT JOIN nguoi_dung nd ON nd.id = c.nha_thau_id
            WHERE (CAST(:khuVucId AS uuid) IS NULL OR c.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR c.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (c.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR c.id = CAST(:doiTuongId AS uuid)))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR c.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR c.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR c.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND c.resolved_ngay_ht IS NOT NULL
              AND (CAST(:fromDate AS date) IS NULL OR c.resolved_ngay_ht >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL OR c.resolved_ngay_ht <= CAST(:toDate AS date))
              AND (CAST(:tab AS text) IS NULL OR (CAST(:tab AS text) = 'qua_han') = (((NOW() AT TIME ZONE 'Asia/Ho_Chi_Minh')::date
                    - c.resolved_ngay_ht) >= CAST(:thresholdDays AS integer)))
            ORDER BY c.resolved_ngay_ht ASC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM (""" + CHO_QUYET_TOAN_CANDIDATE + """
            ) c
            WHERE (CAST(:khuVucId AS uuid) IS NULL OR c.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR c.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (c.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR c.id = CAST(:doiTuongId AS uuid)))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR c.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR c.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR c.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND c.resolved_ngay_ht IS NOT NULL
              AND (CAST(:fromDate AS date) IS NULL OR c.resolved_ngay_ht >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL OR c.resolved_ngay_ht <= CAST(:toDate AS date))
              AND (CAST(:tab AS text) IS NULL OR (CAST(:tab AS text) = 'qua_han') = (((NOW() AT TIME ZONE 'Asia/Ho_Chi_Minh')::date
                    - c.resolved_ngay_ht) >= CAST(:thresholdDays AS integer)))
            """,
            nativeQuery = true)
    Page<Object[]> search(@Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
                           @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId, @Param("doiTuongId") UUID doiTuongId,
                           @Param("loaiHopDongId") UUID loaiHopDongId, @Param("tab") String tab, @Param("thresholdDays") int thresholdDays, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
                           Pageable pageable);

    /** Danh sách tab "Chưa pháp lý": đối tượng hoạt động thuộc hợp đồng chưa đủ pháp lý (cùng tập với tổng quan). Dòng cùng dạng với {@code search}, ngày mốc luôn null. */
    @Query(value = """
            SELECT c.id, c.hop_dong_id, c.ma_hop_dong, c.ten_hop_dong, COALESCE(dt.ten, dt.ma, '—') AS ma_tram,
                   COALESCE(kv.ten, '—') AS khu_vuc, COALESCE(nd.ho_ten, '—') AS nha_thau,
                   c.gia_tri, CAST(NULL AS date) AS ngay_moc
            FROM (
                SELECT d.id AS id, d.hop_dong_id AS hop_dong_id, d.doi_tuong_quan_ly_id AS doi_tuong_quan_ly_id,
                       d.khu_vuc_id AS khu_vuc_id, d.tinh_thanh_id AS tinh_thanh_id, d.nha_thau_id AS nha_thau_id,
                       h.loai_hop_dong_id AS loai_hop_dong_id, h.ma_hop_dong AS ma_hop_dong, h.ten AS ten_hop_dong,
                       """ + GIA_TRI_TRAM + """
                       AS gia_tri
                FROM hop_dong_doi_tuong d
                JOIN hop_dong h ON h.id = d.hop_dong_id AND h.hoat_dong = TRUE AND h.ngay_xoa IS NULL
                LEFT JOIN (""" + SO_TRAM_THEO_HOP_DONG + """
                ) oc ON oc.hop_dong_id = d.hop_dong_id
                WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
                  AND (h.trang_thai_phap_ly IS NULL OR h.trang_thai_phap_ly <> 2)
            ) c
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = c.doi_tuong_quan_ly_id AND dt.ngay_xoa IS NULL
            LEFT JOIN khu_vuc kv ON kv.id = c.khu_vuc_id
            LEFT JOIN nguoi_dung nd ON nd.id = c.nha_thau_id
            """ + LOC_UNG_VIEN + """
            ORDER BY c.ma_hop_dong, c.id
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM (
                SELECT d.id AS id, d.hop_dong_id AS hop_dong_id, d.doi_tuong_quan_ly_id AS doi_tuong_quan_ly_id,
                       d.khu_vuc_id AS khu_vuc_id, d.tinh_thanh_id AS tinh_thanh_id, d.nha_thau_id AS nha_thau_id,
                       h.loai_hop_dong_id AS loai_hop_dong_id
                FROM hop_dong_doi_tuong d
                JOIN hop_dong h ON h.id = d.hop_dong_id AND h.hoat_dong = TRUE AND h.ngay_xoa IS NULL
                WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
                  AND (h.trang_thai_phap_ly IS NULL OR h.trang_thai_phap_ly <> 2)
            ) c
            """ + LOC_UNG_VIEN, nativeQuery = true)
    Page<Object[]> searchChuaPhapLy(@Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
                                     @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId, @Param("doiTuongId") UUID doiTuongId,
                                     @Param("loaiHopDongId") UUID loaiHopDongId, Pageable pageable);

    /** Danh sách tab "Vướng mắc": vướng mắc đang mở quá ngưỡng, mỗi đối tượng 1 dòng (cùng tập với tổng quan). Ngày mốc = ngày tạo vướng mắc. */
    @Query(value = """
            SELECT c.id, c.hop_dong_id, c.ma_hop_dong, c.ten_hop_dong, COALESCE(dt.ten, dt.ma, '—') AS ma_tram,
                   COALESCE(kv.ten, '—') AS khu_vuc, COALESCE(nd.ho_ten, '—') AS nha_thau,
                   CASE WHEN COALESCE(c.san_luong_hieu_luc, 0) > 0 THEN c.san_luong_hieu_luc ELSE 0 END AS gia_tri,
                   (c.ngay_tao AT TIME ZONE 'Asia/Ho_Chi_Minh')::date AS ngay_moc
            FROM (
                SELECT COALESCE(d.id, v.id) AS id, v.ngay_tao AS ngay_tao, d.san_luong_hieu_luc AS san_luong_hieu_luc,
                       d.khu_vuc_id AS khu_vuc_id, d.tinh_thanh_id AS tinh_thanh_id, d.nha_thau_id AS nha_thau_id,
                       d.doi_tuong_quan_ly_id AS doi_tuong_quan_ly_id, v.hop_dong_id AS hop_dong_id, h.loai_hop_dong_id AS loai_hop_dong_id,
                       h.ma_hop_dong AS ma_hop_dong, h.ten AS ten_hop_dong,
                       ROW_NUMBER() OVER (PARTITION BY COALESCE(v.du_lieu_doi_tuong_id, v.id)
                                          ORDER BY v.ngay_tao ASC, v.id ASC) AS rn
                FROM vuong_mac v
                LEFT JOIN hop_dong h ON h.id = v.hop_dong_id AND h.ngay_xoa IS NULL
                LEFT JOIN hop_dong_doi_tuong d ON d.id = v.du_lieu_doi_tuong_id AND d.ngay_xoa IS NULL
                WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
                  AND v.trang_thai IN ('pending', 'in_progress')
                  AND v.ngay_tao < ((""" + TODAY_HCM + """
                        - CAST(:thresholdDays AS integer))::timestamp AT TIME ZONE 'Asia/Ho_Chi_Minh')
                  AND NOT (d.id IS NOT NULL AND d.hoat_dong = TRUE AND h.hoat_dong = TRUE
                           AND (h.trang_thai_phap_ly IS NULL OR h.trang_thai_phap_ly <> 2))
            ) c
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = c.doi_tuong_quan_ly_id AND dt.ngay_xoa IS NULL
            LEFT JOIN khu_vuc kv ON kv.id = c.khu_vuc_id
            LEFT JOIN nguoi_dung nd ON nd.id = c.nha_thau_id
            """ + LOC_UNG_VIEN + """
              AND c.rn = 1
            ORDER BY c.ngay_tao ASC, c.id
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM (
                SELECT COALESCE(d.id, v.id) AS id, d.khu_vuc_id AS khu_vuc_id, d.tinh_thanh_id AS tinh_thanh_id, d.nha_thau_id AS nha_thau_id,
                       d.doi_tuong_quan_ly_id AS doi_tuong_quan_ly_id, v.hop_dong_id AS hop_dong_id, h.loai_hop_dong_id AS loai_hop_dong_id,
                       ROW_NUMBER() OVER (PARTITION BY COALESCE(v.du_lieu_doi_tuong_id, v.id)
                                          ORDER BY v.ngay_tao ASC, v.id ASC) AS rn
                FROM vuong_mac v
                LEFT JOIN hop_dong h ON h.id = v.hop_dong_id AND h.ngay_xoa IS NULL
                LEFT JOIN hop_dong_doi_tuong d ON d.id = v.du_lieu_doi_tuong_id AND d.ngay_xoa IS NULL
                WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
                  AND v.trang_thai IN ('pending', 'in_progress')
                  AND v.ngay_tao < ((""" + TODAY_HCM + """
                        - CAST(:thresholdDays AS integer))::timestamp AT TIME ZONE 'Asia/Ho_Chi_Minh')
                  AND NOT (d.id IS NOT NULL AND d.hoat_dong = TRUE AND h.hoat_dong = TRUE
                           AND (h.trang_thai_phap_ly IS NULL OR h.trang_thai_phap_ly <> 2))
            ) c
            """ + LOC_UNG_VIEN + """
              AND c.rn = 1
            """, nativeQuery = true)
    Page<Object[]> searchVuongMac(@Param("thresholdDays") int thresholdDays,
                                   @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
                                   @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId, @Param("doiTuongId") UUID doiTuongId,
                                   @Param("loaiHopDongId") UUID loaiHopDongId, Pageable pageable);

    /** Đã khởi công (ngay_thi_cong_gan_nhat set) nhưng chưa hoàn thành, quá N ngày không cập nhật sản lượng, trong phạm vi bộ lọc. */
    @Query(value = """
            SELECT d.id, d.hop_dong_id, h.ma_hop_dong, h.ten AS ten_hop_dong,
                   COALESCE(dt.ten, dt.ma, '—') AS ma_tram,
                   COALESCE(kv.ten, '—') AS khu_vuc, COALESCE(nd.ho_ten, '—') AS nha_thau,
                   d.ngay_thi_cong_gan_nhat
            FROM hop_dong_doi_tuong d
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.hoat_dong = TRUE AND h.ngay_xoa IS NULL
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id AND dt.ngay_xoa IS NULL
            LEFT JOIN khu_vuc kv ON kv.id = d.khu_vuc_id
            LEFT JOIN nguoi_dung nd ON nd.id = d.nha_thau_id
            WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
              AND d.ngay_thi_cong_gan_nhat IS NOT NULL
              AND d.ngay_ht_tc IS NULL
              AND d.ngay_thi_cong_gan_nhat < :nguongNgay
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
            ORDER BY d.ngay_thi_cong_gan_nhat ASC
            LIMIT 20
            """, nativeQuery = true)
    List<Object[]> findThieuCapNhat(@Param("nguongNgay") LocalDate nguongNgay,
                                    @Param("nhaThauId") UUID nhaThauId,
                                    @Param("hopDongId") UUID hopDongId,
                                    @Param("khuVucId") UUID khuVucId,
                                    @Param("tinhThanhId") UUID tinhThanhId,
                                    @Param("doiTuongId") UUID doiTuongId,
                                    @Param("loaiHopDongId") UUID loaiHopDongId);

    /**
     * Trạm có sản lượng hiệu lực vượt ngưỡng của từng trạm — CÙNG công thức với {@code NganHoToolRepository.tongQuanAggregate}
     * (cột {@code soTramBatThuong} khớp FE): ngưỡng hợp đồng = tổng thành tiền thi công × hệ số (GCCC: hệ số riêng của hợp đồng,
     * mặc định theo danh mục loại GCCC, không có thì 2.0; loại khác: hệ số của loại hợp đồng), chia đều cho số trạm đang hoạt động của hợp đồng.
     * Chỉ lọc danh sách trạm hiển thị theo bộ lọc; ngưỡng vẫn tính trên toàn bộ trạm của hợp đồng (không đổi theo bộ lọc).
     * row: [id, hopDongId, maHopDong, tenHopDong, giaTriHd, ngayHtTc, sanLuongHieuLuc].
     */
    @Query(value = """
            WITH tram AS (
                SELECT hop_dong_id, COUNT(*) AS tram_tong
                FROM hop_dong_doi_tuong WHERE ngay_xoa IS NULL AND hoat_dong = TRUE
                GROUP BY hop_dong_id
            ), nguong AS (
                SELECT h.id, h.ma_hop_dong, h.ten, h.gia_tri_hd, h.loai_hop_dong_id,
                       CASE WHEN h.loai_hop_dong_id = CAST('cfe9839a-3b7e-43d4-bd72-0b90e67e0e00' AS uuid)
                              THEN h.tong_thanh_tien_thi_cong * COALESCE(h.heso_nguong,
                                   (SELECT heso_nguong FROM loai_hop_dong WHERE id = CAST('cfe9839a-3b7e-43d4-bd72-0b90e67e0e00' AS uuid)), 2.0)
                            ELSE h.tong_thanh_tien_thi_cong * (
                                SELECT heso_nguong FROM loai_hop_dong WHERE id = CAST('368d1d28-737a-4d1e-b779-05f4375c3065' AS uuid))
                       END AS nguong_hd
                FROM hop_dong h WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            )
            SELECT d.id, d.hop_dong_id, n.ma_hop_dong, n.ten, n.gia_tri_hd, d.ngay_ht_tc, d.san_luong_hieu_luc
            FROM hop_dong_doi_tuong d
            JOIN nguong n ON n.id = d.hop_dong_id
            JOIN tram t ON t.hop_dong_id = d.hop_dong_id
            WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
              AND n.nguong_hd IS NOT NULL AND n.nguong_hd > 0 AND t.tram_tong > 0
              AND d.san_luong_hieu_luc > n.nguong_hd / t.tram_tong
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR n.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
            ORDER BY d.san_luong_hieu_luc DESC, d.id
            LIMIT 20
            """, nativeQuery = true)
    List<Object[]> findSanLuongBatThuong(@Param("nhaThauId") UUID nhaThauId,
                                         @Param("hopDongId") UUID hopDongId,
                                         @Param("khuVucId") UUID khuVucId,
                                         @Param("tinhThanhId") UUID tinhThanhId,
                                         @Param("doiTuongId") UUID doiTuongId,
                                         @Param("loaiHopDongId") UUID loaiHopDongId);

    /** Xếp hạng khu vực theo số đối tượng tồn "chờ quyết toán" trong phạm vi lọc hiện tại. */
    @Query(value = """
            SELECT COALESCE(kv.ten, '—') AS khuVuc, COUNT(*) AS soDoiTuong,
                   COALESCE(SUM(c.san_luong_hieu_luc), 0) AS giaTriTon
            FROM (""" + CHO_QUYET_TOAN_CANDIDATE + """
            ) c
            LEFT JOIN khu_vuc kv ON kv.id = c.khu_vuc_id
            WHERE (CAST(:nhaThauId AS uuid) IS NULL OR c.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR c.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (c.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR c.id = CAST(:doiTuongId AS uuid)))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR c.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR c.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
            GROUP BY kv.ten
            ORDER BY COUNT(*) DESC, kv.ten
            """, nativeQuery = true)
    List<XepHangKhuVucRow> rankKhuVuc(Pageable pageable, @Param("nhaThauId") UUID nhaThauId,
                                      @Param("hopDongId") UUID hopDongId,
                                      @Param("doiTuongId") UUID doiTuongId,
                                      @Param("tinhThanhId") UUID tinhThanhId,
                                      @Param("loaiHopDongId") UUID loaiHopDongId);

    /**
     * Đối tượng chưa quyết toán, thiếu từ 2 điều kiện trở lên trong 3 điều kiện:
     * pháp lý hợp đồng (trang_thai_phap_ly=2), không vướng mắc mở, đã có sản lượng hiệu lực > 0.
     */
    @Query(value = """
            SELECT dt.ma AS maDoiTuong, h.ma_hop_dong AS maHopDong,
                   COALESCE(kv.ten, '—') AS khuVuc, COALESCE(nd.ho_ten, '—') AS nhaThau,
                   (h.trang_thai_phap_ly IS NULL OR h.trang_thai_phap_ly <> 2) AS thieuPhapLy,
                   d.co_vuong_mac_mo AS dangVuongMac,
                   (d.san_luong_hieu_luc IS NULL OR d.san_luong_hieu_luc <= 0) AS chuaCoSanLuong
            FROM hop_dong_doi_tuong d
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.hoat_dong = TRUE AND h.ngay_xoa IS NULL
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id AND dt.ngay_xoa IS NULL
            LEFT JOIN khu_vuc kv ON kv.id = d.khu_vuc_id
            LEFT JOIN nguoi_dung nd ON nd.id = d.nha_thau_id
            WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL AND d.quyet_toan_thuc IS NULL
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND ((CASE WHEN h.trang_thai_phap_ly IS NULL OR h.trang_thai_phap_ly <> 2 THEN 1 ELSE 0 END)
                 + (CASE WHEN d.co_vuong_mac_mo THEN 1 ELSE 0 END)
                 + (CASE WHEN d.san_luong_hieu_luc IS NULL OR d.san_luong_hieu_luc <= 0 THEN 1 ELSE 0 END)) >= 2
            ORDER BY d.san_luong_hieu_luc ASC NULLS FIRST
            LIMIT 20
            """, nativeQuery = true)
    List<ThieuNhieuDieuKienRow> findThieuNhieuDieuKien(@Param("nhaThauId") UUID nhaThauId,
                                                       @Param("hopDongId") UUID hopDongId,
                                                       @Param("khuVucId") UUID khuVucId,
                                                       @Param("tinhThanhId") UUID tinhThanhId,
                                                       @Param("doiTuongId") UUID doiTuongId,
                                                       @Param("loaiHopDongId") UUID loaiHopDongId);

}
