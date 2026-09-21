package vn.edu.huce.iic.bts_ops_platform.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.dto.vuongmac.HopDongOpenCountProjection;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.entity.VuongMac;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface VuongMacToolRepository extends JpaRepository<VuongMac, UUID> {

    /**
     * Tìm vướng mắc theo NỘI DUNG chữ (mã, mô tả, người báo cáo — keyword dạng %lower%) kết hợp bộ lọc id đã resolve
     * (hợp đồng, nhà thầu, đối tượng). Hợp đồng/nhà thầu/đối tượng không còn khớp bằng chữ tìm kiếm.
     * Join {@code hop_dong_doi_tuong} là 1-1 theo khoá chính nên không cần DISTINCT.
     */
    @Query(value = """
            SELECT v.*
            FROM vuong_mac v
            LEFT JOIN hop_dong_doi_tuong d ON d.id = v.du_lieu_doi_tuong_id AND d.ngay_xoa IS NULL
            WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
              AND (CAST(:keyword AS text) IS NULL
                   OR LOWER(v.ma) LIKE :keyword ESCAPE '\\'
                   OR LOWER(v.mo_ta) LIKE :keyword ESCAPE '\\'
                   OR LOWER(COALESCE(v.ten_nguoi_bao_cao, '')) LIKE :keyword ESCAPE '\\')
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR v.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR v.hop_dong_id IN (SELECT hl.id FROM hop_dong hl WHERE hl.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
              AND (CAST(:giaiDoan AS text) IS NULL OR LOWER(v.giai_doan) = LOWER(:giaiDoan))
              AND (CAST(:trangThai AS text) IS NULL OR v.trang_thai = :trangThai)
              AND (CAST(:kieuVuongMac AS text) IS NULL OR v.kieu_vuong_mac = :kieuVuongMac)
              AND (CAST(:canBoId AS uuid) IS NULL OR v.nguoi_xu_ly_id = CAST(:canBoId AS uuid))
              AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR v.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz))
              AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR v.ngay_tao < CAST(:ngayTaoTo AS timestamptz))
              AND (CAST(:dangMoOnly AS boolean) IS NOT TRUE OR v.trang_thai IN ('pending', 'in_progress'))
            ORDER BY v.ngay_tao DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM vuong_mac v
            LEFT JOIN hop_dong_doi_tuong d ON d.id = v.du_lieu_doi_tuong_id AND d.ngay_xoa IS NULL
            WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
              AND (CAST(:keyword AS text) IS NULL
                   OR LOWER(v.ma) LIKE :keyword ESCAPE '\\'
                   OR LOWER(v.mo_ta) LIKE :keyword ESCAPE '\\'
                   OR LOWER(COALESCE(v.ten_nguoi_bao_cao, '')) LIKE :keyword ESCAPE '\\')
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR v.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR v.hop_dong_id IN (SELECT hl.id FROM hop_dong hl WHERE hl.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
              AND (CAST(:giaiDoan AS text) IS NULL OR LOWER(v.giai_doan) = LOWER(:giaiDoan))
              AND (CAST(:trangThai AS text) IS NULL OR v.trang_thai = :trangThai)
              AND (CAST(:kieuVuongMac AS text) IS NULL OR v.kieu_vuong_mac = :kieuVuongMac)
              AND (CAST(:canBoId AS uuid) IS NULL OR v.nguoi_xu_ly_id = CAST(:canBoId AS uuid))
              AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR v.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz))
              AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR v.ngay_tao < CAST(:ngayTaoTo AS timestamptz))
              AND (CAST(:dangMoOnly AS boolean) IS NOT TRUE OR v.trang_thai IN ('pending', 'in_progress'))
            """,
            nativeQuery = true)
    Page<VuongMac> search(@Param("keyword") String keyword, @Param("nhaThauId") UUID nhaThauId,
                           @Param("hopDongId") UUID hopDongId, @Param("doiTuongId") UUID doiTuongId,
                           @Param("tinhThanhId") UUID tinhThanhId, @Param("khuVucId") UUID khuVucId,
                           @Param("giaiDoan") String giaiDoan, @Param("trangThai") String trangThai,
                           @Param("kieuVuongMac") String kieuVuongMac, @Param("dangMoOnly") Boolean dangMoOnly,
                           @Param("loaiHopDongId") UUID loaiHopDongId, @Param("canBoId") UUID canBoId, @Param("ngayTaoFrom") Instant ngayTaoFrom, @Param("ngayTaoTo") Instant ngayTaoTo, Pageable pageable);

    /** row: [id, maHopDong, tenHopDong, tenLoaiHopDong, tenKhuVuc, tenTinh, tenNhaThau, tenNguoiXuLy]. */
    @Query(value = """
            SELECT v.id, h.ma_hop_dong, h.ten, lhd.ten, kv.ten, tt.ten, nt.ho_ten, nd.ho_ten
            FROM vuong_mac v
            LEFT JOIN hop_dong h ON h.id = v.hop_dong_id AND h.ngay_xoa IS NULL
            LEFT JOIN loai_hop_dong lhd ON lhd.id = h.loai_hop_dong_id
            LEFT JOIN hop_dong_doi_tuong d ON d.id = v.du_lieu_doi_tuong_id AND d.ngay_xoa IS NULL
            LEFT JOIN khu_vuc kv ON kv.id = d.khu_vuc_id
            LEFT JOIN tinh_thanh tt ON tt.id = d.tinh_thanh_id
            LEFT JOIN nguoi_dung nt ON nt.id = d.nha_thau_id
            LEFT JOIN nguoi_dung nd ON nd.id = v.nguoi_xu_ly_id AND nd.ngay_xoa IS NULL
            WHERE v.id IN :ids
            """, nativeQuery = true)
    List<Object[]> enrichForIds(@Param("ids") List<UUID> ids);





    @Query(value = """
            SELECT v.kieu_vuong_mac, COUNT(*)
            FROM vuong_mac v
            WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
            GROUP BY v.kieu_vuong_mac
            """, nativeQuery = true)
    List<Object[]> countGroupByKieu();

    /** Biến thể có filter của {@link #countGroupByKieu()}. */
    @Query(value = """
            SELECT v.kieu_vuong_mac, COUNT(DISTINCT v.id)
            FROM vuong_mac v
            LEFT JOIN hop_dong_doi_tuong d ON d.id = v.du_lieu_doi_tuong_id AND d.ngay_xoa IS NULL
            WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
              AND (CAST(:hopDongId AS uuid) IS NULL OR v.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR v.hop_dong_id IN (SELECT hl.id FROM hop_dong hl WHERE hl.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
              AND (CAST(:giaiDoan AS text) IS NULL OR LOWER(v.giai_doan) = LOWER(:giaiDoan))
              AND (CAST(:canBoId AS uuid) IS NULL OR v.nguoi_xu_ly_id = CAST(:canBoId AS uuid))
              AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR v.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz))
              AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR v.ngay_tao < CAST(:ngayTaoTo AS timestamptz))
            GROUP BY v.kieu_vuong_mac
            """, nativeQuery = true)
    List<Object[]> countGroupByKieuFiltered(@Param("hopDongId") UUID hopDongId, @Param("nhaThauId") UUID nhaThauId,
                                            @Param("tinhThanhId") UUID tinhThanhId, @Param("doiTuongId") UUID doiTuongId,
                                            @Param("khuVucId") UUID khuVucId, @Param("giaiDoan") String giaiDoan,
                                            @Param("loaiHopDongId") UUID loaiHopDongId, @Param("canBoId") UUID canBoId, @Param("ngayTaoFrom") Instant ngayTaoFrom, @Param("ngayTaoTo") Instant ngayTaoTo);

    @Query(value = """
            SELECT COALESCE(h.ma_hop_dong, h.ten, '—') AS label, h.ten AS name, COUNT(*) AS value
            FROM vuong_mac v
            INNER JOIN hop_dong h ON h.id = v.hop_dong_id AND h.ngay_xoa IS NULL
            LEFT JOIN hop_dong_doi_tuong d ON d.id = v.du_lieu_doi_tuong_id AND d.ngay_xoa IS NULL
            WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
              AND v.trang_thai IN ('pending', 'in_progress')
              AND v.hop_dong_id IS NOT NULL
              AND (CAST(:hopDongId AS uuid) IS NULL OR v.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
              AND (CAST(:giaiDoan AS text) IS NULL OR LOWER(v.giai_doan) = LOWER(:giaiDoan))
              AND (CAST(:kieuVuongMac AS text) IS NULL OR v.kieu_vuong_mac = :kieuVuongMac)
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR v.hop_dong_id IN (SELECT hl.id FROM hop_dong hl WHERE hl.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
              AND (CAST(:canBoId AS uuid) IS NULL OR v.nguoi_xu_ly_id = CAST(:canBoId AS uuid))
              AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR v.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz))
              AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR v.ngay_tao < CAST(:ngayTaoTo AS timestamptz))
            GROUP BY v.hop_dong_id, h.ma_hop_dong, h.ten
            ORDER BY value DESC, label
            """, nativeQuery = true)
    List<HopDongOpenCountProjection> rankHopDongByOpenCount(Pageable pageable, @Param("hopDongId") UUID hopDongId,
                                                           @Param("nhaThauId") UUID nhaThauId,
                                                           @Param("tinhThanhId") UUID tinhThanhId,
                                                           @Param("doiTuongId") UUID doiTuongId,
                                                           @Param("khuVucId") UUID khuVucId,
                                                           @Param("giaiDoan") String giaiDoan,
                                                           @Param("kieuVuongMac") String kieuVuongMac,
                                                           @Param("loaiHopDongId") UUID loaiHopDongId, @Param("canBoId") UUID canBoId, @Param("ngayTaoFrom") Instant ngayTaoFrom, @Param("ngayTaoTo") Instant ngayTaoTo);

    @Query(value = """
            SELECT v.*
            FROM vuong_mac v
            WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
              AND v.trang_thai IN ('pending', 'in_progress')
              AND v.ngay_tao < :nguongNgay
            ORDER BY v.ngay_tao ASC
            """,
            countQuery = """
            SELECT COUNT(*) FROM vuong_mac v
            WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
              AND v.trang_thai IN ('pending', 'in_progress')
              AND v.ngay_tao < :nguongNgay
            """,
            nativeQuery = true)
    Page<VuongMac> findQuaHan(@Param("nguongNgay") Instant nguongNgay, Pageable pageable);

    /** Xếp hạng khu vực (qua đối tượng của vướng mắc) theo số vướng mắc đang mở. */
    @Query(value = """
            SELECT COALESCE(kv.ten, '—') AS label, COUNT(*) AS value
            FROM vuong_mac v
            INNER JOIN hop_dong_doi_tuong d ON d.id = v.du_lieu_doi_tuong_id AND d.ngay_xoa IS NULL
            LEFT JOIN khu_vuc kv ON kv.id = d.khu_vuc_id
            WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
              AND v.trang_thai IN ('pending', 'in_progress')
              AND (CAST(:hopDongId AS uuid) IS NULL OR v.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
              AND (CAST(:giaiDoan AS text) IS NULL OR LOWER(v.giai_doan) = LOWER(:giaiDoan))
              AND (CAST(:kieuVuongMac AS text) IS NULL OR v.kieu_vuong_mac = :kieuVuongMac)
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR v.hop_dong_id IN (SELECT hl.id FROM hop_dong hl WHERE hl.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
              AND (CAST(:canBoId AS uuid) IS NULL OR v.nguoi_xu_ly_id = CAST(:canBoId AS uuid))
              AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR v.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz))
              AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR v.ngay_tao < CAST(:ngayTaoTo AS timestamptz))
            GROUP BY kv.ten
            ORDER BY value DESC, label
            """, nativeQuery = true)
    List<HopDongOpenCountProjection> rankKhuVucByOpenCount(Pageable pageable, @Param("hopDongId") UUID hopDongId,
                                                         @Param("nhaThauId") UUID nhaThauId,
                                                         @Param("tinhThanhId") UUID tinhThanhId,
                                                         @Param("doiTuongId") UUID doiTuongId,
                                                         @Param("giaiDoan") String giaiDoan,
                                                         @Param("kieuVuongMac") String kieuVuongMac,
                                                         @Param("loaiHopDongId") UUID loaiHopDongId, @Param("canBoId") UUID canBoId, @Param("ngayTaoFrom") Instant ngayTaoFrom, @Param("ngayTaoTo") Instant ngayTaoTo);

    /**
     * Toàn bộ số đếm của khối tổng quan trong 1 câu (thay cho 7 câu {@code count*}): tổng, theo trạng thái, quá hạn 30 ngày
     * (còn mở) và số đã xử lý trong kỳ [tuFrom, denTo) (bỏ qua nếu tuFrom null). Lọc hợp đồng/nhà thầu nếu truyền.
     * row: [total, pending, in_progress, resolved, rejected, overdue30, resolvedTrongKy].
     */
    @Query(value = """
            SELECT COUNT(*) FILTER (WHERE (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR v.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz)) AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR v.ngay_tao < CAST(:ngayTaoTo AS timestamptz))),
                   COUNT(*) FILTER (WHERE v.trang_thai = 'pending' AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR v.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz)) AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR v.ngay_tao < CAST(:ngayTaoTo AS timestamptz))),
                   COUNT(*) FILTER (WHERE v.trang_thai = 'in_progress' AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR v.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz)) AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR v.ngay_tao < CAST(:ngayTaoTo AS timestamptz))),
                   COUNT(*) FILTER (WHERE v.trang_thai = 'resolved' AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR v.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz)) AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR v.ngay_tao < CAST(:ngayTaoTo AS timestamptz))),
                   COUNT(*) FILTER (WHERE v.trang_thai = 'rejected' AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR v.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz)) AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR v.ngay_tao < CAST(:ngayTaoTo AS timestamptz))),
                   COUNT(*) FILTER (WHERE v.qua_han_30_ngay = TRUE AND v.trang_thai IN ('pending', 'in_progress') AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR v.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz)) AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR v.ngay_tao < CAST(:ngayTaoTo AS timestamptz))),
                   COUNT(*) FILTER (WHERE v.trang_thai = 'resolved' AND CAST(:tuFrom AS timestamptz) IS NOT NULL
                                      AND v.ngay_cap_nhat >= CAST(:tuFrom AS timestamptz)
                                      AND v.ngay_cap_nhat < CAST(:denTo AS timestamptz))
            FROM vuong_mac v
            LEFT JOIN hop_dong_doi_tuong d ON d.id = v.du_lieu_doi_tuong_id AND d.ngay_xoa IS NULL
            WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
              AND (CAST(:hopDongId AS uuid) IS NULL OR v.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR v.hop_dong_id IN (SELECT hl.id FROM hop_dong hl WHERE hl.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
              AND (CAST(:giaiDoan AS text) IS NULL OR LOWER(v.giai_doan) = LOWER(:giaiDoan))
              AND (CAST(:trangThai AS text) IS NULL OR v.trang_thai = :trangThai)
              AND (CAST(:kieuVuongMac AS text) IS NULL OR v.kieu_vuong_mac = :kieuVuongMac)
              AND (CAST(:canBoId AS uuid) IS NULL OR v.nguoi_xu_ly_id = CAST(:canBoId AS uuid))
              AND (CAST(:dangMoOnly AS boolean) IS NOT TRUE OR v.trang_thai IN ('pending', 'in_progress'))
            """, nativeQuery = true)
    List<Object[]> thongKeTongQuan(@Param("tuFrom") Instant tuFrom, @Param("denTo") Instant denTo,
                                   @Param("hopDongId") UUID hopDongId, @Param("nhaThauId") UUID nhaThauId,
                                   @Param("tinhThanhId") UUID tinhThanhId, @Param("doiTuongId") UUID doiTuongId,
                                   @Param("khuVucId") UUID khuVucId, @Param("giaiDoan") String giaiDoan,
                                   @Param("trangThai") String trangThai, @Param("kieuVuongMac") String kieuVuongMac,
                                   @Param("dangMoOnly") Boolean dangMoOnly, @Param("loaiHopDongId") UUID loaiHopDongId, @Param("canBoId") UUID canBoId, @Param("ngayTaoFrom") Instant ngayTaoFrom, @Param("ngayTaoTo") Instant ngayTaoTo);
}
