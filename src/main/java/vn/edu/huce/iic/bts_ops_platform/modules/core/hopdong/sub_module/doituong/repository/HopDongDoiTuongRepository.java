package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuong;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

public interface HopDongDoiTuongRepository extends JpaRepository<HopDongDoiTuong, UUID> {

    List<HopDongDoiTuong> findByNgayXoaIsNull();

    @Query("""
            SELECT d FROM HopDongDoiTuong d
            WHERE d.ngayXoa IS NULL AND d.hoatDong = TRUE
              AND (d.ngayBanGiaoMatBang IS NOT NULL OR d.ngayYeuCauVatTuB IS NOT NULL)
            """)
    List<HopDongDoiTuong> findActiveForKpiCanhBao();

    long countByNgayXoaIsNull();

    /**
     * Đối tượng đã khởi công (có ngày thi công gần nhất) nhưng CHƯA hoàn thành (ngay_ht_tc IS NULL)
     * và quá N ngày không có bản ghi sản lượng mới — dùng cho tool AI "trạm thiếu cập nhật tiến độ".
     */
    @Query(value = """
            SELECT d.id
            FROM hop_dong_doi_tuong d
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.hoat_dong = TRUE AND h.ngay_xoa IS NULL
            WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
              AND d.ngay_ht_tc IS NULL
              AND d.ngay_thi_cong_gan_nhat IS NOT NULL
              AND d.ngay_thi_cong_gan_nhat < (CURRENT_DATE - CAST(:soNgay AS int))
            ORDER BY d.ngay_thi_cong_gan_nhat ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<UUID> findIdsThieuCapNhat(@Param("soNgay") int soNgay, @Param("limit") int limit);

    /**
     * Breakdown số trạm theo loại hợp đồng + khu vực (tổng/hoàn thành/đang thi công/vướng mắc).
     */
    @Query(value = """
            SELECT
                COUNT(*) AS tong,
                COUNT(*) FILTER (WHERE d.ngay_ht_tc IS NOT NULL) AS hoan_thanh,
                COUNT(*) FILTER (WHERE d.ngay_ht_tc IS NULL AND d.ngay_thi_cong_gan_nhat IS NOT NULL) AS dang_thi_cong,
                COUNT(*) FILTER (WHERE d.co_vuong_mac_mo = TRUE) AS vuong_mac
            FROM hop_dong_doi_tuong d
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.hoat_dong = TRUE AND h.ngay_xoa IS NULL
            WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
            """, nativeQuery = true)
    HopDongDoiTuongBreakdownRow breakdownTheoLoaiKhuVuc(
            @Param("loaiHopDongId") UUID loaiHopDongId, @Param("khuVucId") UUID khuVucId);

    /**
     * Trạm có sản lượng hiệu lực VƯỢT ngưỡng hệ số của hợp đồng — tái dùng đúng công thức threshold
     * ở HopDongRepository.tongQuanAggregate (is_gccc ? tong_thanh_tien_thi_cong * heSoMacDinh :
     * tong_thanh_tien_thi_cong * heso_nguong của loại "Xây mới & Còn lại"), nhưng trả về DANH SÁCH
     * trạm cụ thể thay vì chỉ đếm tổng. Không áp dụng override hệ số riêng lẻ theo từng hợp đồng
     * (nếu người dùng đã cấu hình) — dùng ngưỡng mặc định cho toàn hệ thống.
     */
    @Query(value = """
            WITH hd_base AS (
                SELECT h.id,
                       h.gia_tri_hd,
                       h.tong_thanh_tien_thi_cong,
                       CASE
                         WHEN h.loai_hop_dong_id = CAST(:gcccLoaiId AS uuid)
                           THEN h.tong_thanh_tien_thi_cong * CAST(:heSoMacDinh AS numeric)
                         ELSE h.tong_thanh_tien_thi_cong * (
                             SELECT heso_nguong FROM loai_hop_dong WHERE id = CAST(:xayMoiConLaiLoaiId AS uuid)
                         )
                       END AS threshold_contract,
                       (SELECT COUNT(*)
                        FROM hop_dong_doi_tuong d2
                        WHERE d2.hop_dong_id = h.id AND d2.ngay_xoa IS NULL AND d2.hoat_dong = TRUE) AS tram_tong
                FROM hop_dong h
                WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            ),
            hd_per_tram AS (
                SELECT id,
                       gia_tri_hd,
                       CASE
                         WHEN tram_tong > 0
                              AND threshold_contract IS NOT NULL
                              AND threshold_contract > 0
                           THEN threshold_contract / tram_tong
                         ELSE NULL
                       END AS threshold_per_tram
                FROM hd_base
            )
            SELECT d.id AS id,
                   d.hop_dong_id AS hop_dong_id,
                   h.ma_hop_dong AS ma_hop_dong,
                   b.gia_tri_hd AS gia_tri_hd,
                   d.san_luong_hieu_luc AS san_luong_hieu_luc,
                   d.ngay_thi_cong_gan_nhat AS resolved_ngay_ht
            FROM hop_dong_doi_tuong d
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.hoat_dong = TRUE AND h.ngay_xoa IS NULL
            JOIN hd_per_tram b ON b.id = d.hop_dong_id
            WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
              AND b.threshold_per_tram IS NOT NULL
              AND b.threshold_per_tram > 0
              AND d.san_luong_hieu_luc > b.threshold_per_tram
            ORDER BY d.san_luong_hieu_luc DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<HopDongDoiTuongTonCandidateRow> findTramSanLuongBatThuong(
            @Param("gcccLoaiId") UUID gcccLoaiId,
            @Param("xayMoiConLaiLoaiId") UUID xayMoiConLaiLoaiId,
            @Param("heSoMacDinh") java.math.BigDecimal heSoMacDinh,
            @Param("limit") int limit);

    @Query("""
            SELECT d.khuVucId, COUNT(d)
            FROM HopDongDoiTuong d
            WHERE d.ngayXoa IS NULL AND d.hoatDong = TRUE
              AND d.ngayHtTc BETWEEN :tuNgay AND :denNgay
            GROUP BY d.khuVucId
            """)
    List<Object[]> countHoanThanhGroupByKhuVuc(@Param("tuNgay") LocalDate tuNgay, @Param("denNgay") LocalDate denNgay);

    @Query("""
            SELECT d.nhaThauId, COUNT(d)
            FROM HopDongDoiTuong d
            WHERE d.ngayXoa IS NULL AND d.hoatDong = TRUE
              AND d.ngayHtTc BETWEEN :tuNgay AND :denNgay
            GROUP BY d.nhaThauId
            """)
    List<Object[]> countHoanThanhGroupByNhaThau(@Param("tuNgay") LocalDate tuNgay, @Param("denNgay") LocalDate denNgay);

    Optional<HopDongDoiTuong> findByIdAndNgayXoaIsNull(UUID id);

    List<HopDongDoiTuong> findByIdInAndNgayXoaIsNull(Collection<UUID> ids);

    @Query("""
            SELECT COUNT(d)
            FROM HopDongDoiTuong d
            WHERE d.hopDongId = :hopDongId
              AND d.hoatDong = TRUE
              AND d.ngayXoa IS NULL
            """)
    long countActiveObjectsByHopDongId(@Param("hopDongId") UUID hopDongId);

    List<HopDongDoiTuong> findByHopDongIdAndNgayXoaIsNull(UUID hopDongId);

    List<HopDongDoiTuong> findByHopDongIdInAndNgayXoaIsNull(Collection<UUID> hopDongIds);

    long countByHopDongNhomUuTienIdAndNgayXoaIsNull(UUID hopDongNhomUuTienId);

    List<HopDongDoiTuong> findByHopDongNhomUuTienIdAndNgayXoaIsNull(UUID hopDongNhomUuTienId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE HopDongDoiTuong d
            SET d.ngayThiCongGanNhat = (
                SELECT MAX(s.ngayThucHien)
                FROM SanLuong s
                WHERE s.hopDongDoiTuongId = d.id
                  AND s.ngayXoa IS NULL
                  AND s.hoatDong = TRUE
                  AND s.trangThai = 'done'
            )
            WHERE d.id = :id
            """)
    void recalculateNgayThiCongGanNhat(@Param("id") UUID id);

    /**
     * Denormalize — xem HopDongDoiTuong.sanLuongHieuLuc. Chỉ tính hạng mục "đã thi công"
     * (done) — khảo sát/thiết kế/chờ duyệt không tính vào thành tiền, xem
     * SanLuongServiceImpl.isBillableStatus().
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE HopDongDoiTuong d
            SET d.sanLuongHieuLuc = COALESCE((
                SELECT SUM(s.khoiLuongHoanThanh * s.donGia)
                FROM SanLuong s
                WHERE s.hopDongDoiTuongId = d.id
                  AND s.ngayXoa IS NULL
                  AND s.hoatDong = TRUE
                  AND s.trangThai = 'done'
            ), 0) + COALESCE(d.boSungSanLuong, 0)
            WHERE d.id = :id
            """)
    void recalculateSanLuongHieuLuc(@Param("id") UUID id);

    /**
     * Denormalize — xem HopDongDoiTuong.coVuongMacMo.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE HopDongDoiTuong d SET d.coVuongMacMo = :coVuongMacMo WHERE d.id = :id")
    void updateCoVuongMacMo(@Param("id") UUID id, @Param("coVuongMacMo") boolean coVuongMacMo);

    /**
     * Denormalize — xem HopDongDoiTuong.quyetToanThuc. Tổng = SUM các đợt quyết toán còn hoạt
     * động (xem HopDongDoiTuongQuyetToanDot); NULL nếu chưa có đợt nào (chưa quyết toán), khác
     * sanLuongHieuLuc luôn có giá trị mặc định 0 — quyết toán để NULL mới đúng nghĩa "chưa QT".
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE HopDongDoiTuong d
            SET d.quyetToanThuc = (
                SELECT SUM(q.soTien)
                FROM HopDongDoiTuongQuyetToanDot q
                WHERE q.hopDongDoiTuongId = d.id
                  AND q.ngayXoa IS NULL
                  AND q.hoatDong = TRUE
            )
            WHERE d.id = :id
            """)
    void recalculateQuyetToanThuc(@Param("id") UUID id);

    /**
     * Backfill lần đầu sau migration V007 — 1 câu bulk thay vì loop theo từng id.
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE HopDongDoiTuong d
            SET d.sanLuongHieuLuc = COALESCE((
                SELECT SUM(s.khoiLuongHoanThanh * s.donGia)
                FROM SanLuong s
                WHERE s.hopDongDoiTuongId = d.id
                  AND s.ngayXoa IS NULL
                  AND s.hoatDong = TRUE
                  AND s.trangThai = 'done'
            ), 0) + COALESCE(d.boSungSanLuong, 0)
            WHERE d.ngayXoa IS NULL AND d.sanLuongHieuLuc IS NULL
            """)
    int backfillSanLuongHieuLuc();

    /**
     * Recompute lại san_luong_hieu_luc cho TOÀN BỘ hop_dong_doi_tuong (không chỉ NULL, khác
     * backfillSanLuongHieuLuc) — dùng khi rule tính tiền theo trạng thái đổi (vd: bỏ
     * survey/design ra khỏi thành tiền), cần vá lại số liệu cũ đã tính sai theo rule trước.
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE HopDongDoiTuong d
            SET d.sanLuongHieuLuc = COALESCE((
                SELECT SUM(s.khoiLuongHoanThanh * s.donGia)
                FROM SanLuong s
                WHERE s.hopDongDoiTuongId = d.id
                  AND s.ngayXoa IS NULL
                  AND s.hoatDong = TRUE
                  AND s.trangThai = 'done'
            ), 0) + COALESCE(d.boSungSanLuong, 0)
            WHERE d.ngayXoa IS NULL
            """)
    int recalculateSanLuongHieuLucAll();

    /**
     * Backfill lần đầu sau migration V007 — chỉ cần set TRUE cho id đang có vướng mắc mở.
     */
    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE hop_dong_doi_tuong d
            SET co_vuong_mac_mo = TRUE
            WHERE d.ngay_xoa IS NULL
              AND EXISTS (
                    SELECT 1 FROM vuong_mac v
                    WHERE v.du_lieu_doi_tuong_id = d.id
                      AND v.ngay_xoa IS NULL
                      AND v.hoat_dong = TRUE
                      AND v.trang_thai IN ('pending', 'in_progress')
                      AND v.co_the_bo_sung_san_luong = FALSE
              )
            """, nativeQuery = true)
    int backfillCoVuongMacMo();

    @Query(value = """
            SELECT *
            FROM hop_dong_doi_tuong d
            WHERE (:includeDeleted = TRUE OR d.ngay_xoa IS NULL)
              AND (:activeOnly IS NULL OR (:activeOnly = TRUE AND d.hoat_dong = TRUE) OR (:activeOnly = FALSE AND d.hoat_dong = FALSE))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongQuanLyId AS uuid) IS NULL OR d.doi_tuong_quan_ly_id = CAST(:doiTuongQuanLyId AS uuid))
              AND (CAST(:trangThaiHopDongId AS uuid) IS NULL OR d.trang_thai_hop_dong_id = CAST(:trangThaiHopDongId AS uuid))
              AND (:withoutNhomUuTien IS NULL OR :withoutNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NULL)
              AND (:withNhomUuTien IS NULL OR :withNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NOT NULL)
            ORDER BY d.ngay_tao DESC, d.id DESC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<HopDongDoiTuong> searchPageNative(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("trangThaiHopDongId") UUID trangThaiHopDongId,
            @Param("withoutNhomUuTien") Boolean withoutNhomUuTien,
            @Param("withNhomUuTien") Boolean withNhomUuTien,
            @Param("size") int size,
            @Param("offset") long offset);

    @Query(value = """
            SELECT d.id
            FROM hop_dong_doi_tuong d
            WHERE (:includeDeleted = TRUE OR d.ngay_xoa IS NULL)
              AND (:activeOnly IS NULL OR (:activeOnly = TRUE AND d.hoat_dong = TRUE) OR (:activeOnly = FALSE AND d.hoat_dong = FALSE))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongQuanLyId AS uuid) IS NULL OR d.doi_tuong_quan_ly_id = CAST(:doiTuongQuanLyId AS uuid))
              AND (CAST(:trangThaiHopDongId AS uuid) IS NULL OR d.trang_thai_hop_dong_id = CAST(:trangThaiHopDongId AS uuid))
              AND (CAST(:hopDongNhomUuTienId AS uuid) IS NULL OR d.hop_dong_nhom_uu_tien_id = CAST(:hopDongNhomUuTienId AS uuid))
              AND (:withoutNhomUuTien IS NULL OR :withoutNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NULL)
              AND (:withNhomUuTien IS NULL OR :withNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NOT NULL)
            ORDER BY d.ngay_tao DESC, d.id DESC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<UUID> searchIdsPageNative(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("trangThaiHopDongId") UUID trangThaiHopDongId,
            @Param("hopDongNhomUuTienId") UUID hopDongNhomUuTienId,
            @Param("withoutNhomUuTien") Boolean withoutNhomUuTien,
            @Param("withNhomUuTien") Boolean withNhomUuTien,
            @Param("size") int size,
            @Param("offset") long offset);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE HopDongDoiTuong d
            SET d.ngayXoa = :now, d.hoatDong = false
            WHERE d.id IN :ids AND d.ngayXoa IS NULL
            """)
    int softDeleteByIds(@Param("ids") Collection<UUID> ids, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE hop_dong_doi_tuong d
            SET ngay_xoa = :now, hoat_dong = false
            WHERE d.ngay_xoa IS NULL
              AND (:activeOnly IS NULL OR (:activeOnly = TRUE AND d.hoat_dong = TRUE) OR (:activeOnly = FALSE AND d.hoat_dong = FALSE))
              AND d.hop_dong_id = CAST(:hopDongId AS uuid)
              AND (CAST(:doiTuongQuanLyId AS uuid) IS NULL OR d.doi_tuong_quan_ly_id = CAST(:doiTuongQuanLyId AS uuid))
              AND (CAST(:trangThaiHopDongId AS uuid) IS NULL OR d.trang_thai_hop_dong_id = CAST(:trangThaiHopDongId AS uuid))
              AND (CAST(:hopDongNhomUuTienId AS uuid) IS NULL OR d.hop_dong_nhom_uu_tien_id = CAST(:hopDongNhomUuTienId AS uuid))
              AND (:withoutNhomUuTien IS NULL OR :withoutNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NULL)
              AND (:withNhomUuTien IS NULL OR :withNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NOT NULL)
              AND (:hasExclude = FALSE OR d.id NOT IN (:excludeIds))
              AND (:hasScope = FALSE OR d.id IN (:scopeIds))
            """, nativeQuery = true)
    int softDeleteByFilter(
            @Param("now") Instant now,
            @Param("activeOnly") Boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("trangThaiHopDongId") UUID trangThaiHopDongId,
            @Param("hopDongNhomUuTienId") UUID hopDongNhomUuTienId,
            @Param("withoutNhomUuTien") Boolean withoutNhomUuTien,
            @Param("withNhomUuTien") Boolean withNhomUuTien,
            @Param("hasExclude") boolean hasExclude,
            @Param("excludeIds") Collection<UUID> excludeIds,
            @Param("hasScope") boolean hasScope,
            @Param("scopeIds") Collection<UUID> scopeIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE hop_dong_doi_tuong d
            SET ngay_xoa = :now, hoat_dong = false
            FROM doi_tuong_quan_ly dt
            LEFT JOIN trang_thai_hop_dong t ON t.id = d.trang_thai_hop_dong_id
            WHERE d.doi_tuong_quan_ly_id = dt.id
              AND d.ngay_xoa IS NULL
              AND (:activeOnly IS NULL OR (:activeOnly = TRUE AND d.hoat_dong = TRUE) OR (:activeOnly = FALSE AND d.hoat_dong = FALSE))
              AND d.hop_dong_id = CAST(:hopDongId AS uuid)
              AND (CAST(:doiTuongQuanLyId AS uuid) IS NULL OR d.doi_tuong_quan_ly_id = CAST(:doiTuongQuanLyId AS uuid))
              AND (CAST(:trangThaiHopDongId AS uuid) IS NULL OR d.trang_thai_hop_dong_id = CAST(:trangThaiHopDongId AS uuid))
              AND (CAST(:hopDongNhomUuTienId AS uuid) IS NULL OR d.hop_dong_nhom_uu_tien_id = CAST(:hopDongNhomUuTienId AS uuid))
              AND (:withoutNhomUuTien IS NULL OR :withoutNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NULL)
              AND (:withNhomUuTien IS NULL OR :withNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NOT NULL)
              AND (
                    LOWER(COALESCE(dt.ten, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(dt.ma, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(t.ma, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(t.ten, '')) LIKE CONCAT('%', :keyword, '%')
                    OR EXISTS (
                        SELECT 1
                        FROM hop_dong_doi_tuong_gia_tri g
                        WHERE g.hop_dong_doi_tuong_id = d.id
                          AND g.ngay_xoa IS NULL
                          AND LOWER(COALESCE(g.gia_tri, '')) LIKE CONCAT('%', :keyword, '%')
                    )
                  )
              AND (:hasExclude = FALSE OR d.id NOT IN (:excludeIds))
              AND (:hasScope = FALSE OR d.id IN (:scopeIds))
            """, nativeQuery = true)
    int softDeleteByFilterWithKeyword(
            @Param("now") Instant now,
            @Param("activeOnly") Boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("trangThaiHopDongId") UUID trangThaiHopDongId,
            @Param("hopDongNhomUuTienId") UUID hopDongNhomUuTienId,
            @Param("keyword") String keyword,
            @Param("withoutNhomUuTien") Boolean withoutNhomUuTien,
            @Param("withNhomUuTien") Boolean withNhomUuTien,
            @Param("hasExclude") boolean hasExclude,
            @Param("excludeIds") Collection<UUID> excludeIds,
            @Param("hasScope") boolean hasScope,
            @Param("scopeIds") Collection<UUID> scopeIds);

    /**
     * Ids khớp cùng tiêu chí với softDeleteByFilter (không LIMIT) — dùng để publish
     * HopDongDoiTuongCascadeDeleteEvent cho business tự cascade, thay vì core gọi thẳng
     * repository của business.
     */
    @Query(value = """
            SELECT d.id
            FROM hop_dong_doi_tuong d
            WHERE d.ngay_xoa IS NULL
              AND (:activeOnly IS NULL OR (:activeOnly = TRUE AND d.hoat_dong = TRUE) OR (:activeOnly = FALSE AND d.hoat_dong = FALSE))
              AND d.hop_dong_id = CAST(:hopDongId AS uuid)
              AND (CAST(:doiTuongQuanLyId AS uuid) IS NULL OR d.doi_tuong_quan_ly_id = CAST(:doiTuongQuanLyId AS uuid))
              AND (CAST(:trangThaiHopDongId AS uuid) IS NULL OR d.trang_thai_hop_dong_id = CAST(:trangThaiHopDongId AS uuid))
              AND (CAST(:hopDongNhomUuTienId AS uuid) IS NULL OR d.hop_dong_nhom_uu_tien_id = CAST(:hopDongNhomUuTienId AS uuid))
              AND (:withoutNhomUuTien IS NULL OR :withoutNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NULL)
              AND (:withNhomUuTien IS NULL OR :withNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NOT NULL)
              AND (:hasExclude = FALSE OR d.id NOT IN (:excludeIds))
              AND (:hasScope = FALSE OR d.id IN (:scopeIds))
            """, nativeQuery = true)
    List<UUID> findIdsByFilter(
            @Param("activeOnly") Boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("trangThaiHopDongId") UUID trangThaiHopDongId,
            @Param("hopDongNhomUuTienId") UUID hopDongNhomUuTienId,
            @Param("withoutNhomUuTien") Boolean withoutNhomUuTien,
            @Param("withNhomUuTien") Boolean withNhomUuTien,
            @Param("hasExclude") boolean hasExclude,
            @Param("excludeIds") Collection<UUID> excludeIds,
            @Param("hasScope") boolean hasScope,
            @Param("scopeIds") Collection<UUID> scopeIds);

    /**
     * Ids khớp cùng tiêu chí với softDeleteByFilterWithKeyword (không LIMIT) — xem findIdsByFilter.
     */
    @Query(value = """
            SELECT d.id
            FROM hop_dong_doi_tuong d
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id
            LEFT JOIN trang_thai_hop_dong t ON t.id = d.trang_thai_hop_dong_id
            WHERE d.ngay_xoa IS NULL
              AND (:activeOnly IS NULL OR (:activeOnly = TRUE AND d.hoat_dong = TRUE) OR (:activeOnly = FALSE AND d.hoat_dong = FALSE))
              AND d.hop_dong_id = CAST(:hopDongId AS uuid)
              AND (CAST(:doiTuongQuanLyId AS uuid) IS NULL OR d.doi_tuong_quan_ly_id = CAST(:doiTuongQuanLyId AS uuid))
              AND (CAST(:trangThaiHopDongId AS uuid) IS NULL OR d.trang_thai_hop_dong_id = CAST(:trangThaiHopDongId AS uuid))
              AND (CAST(:hopDongNhomUuTienId AS uuid) IS NULL OR d.hop_dong_nhom_uu_tien_id = CAST(:hopDongNhomUuTienId AS uuid))
              AND (:withoutNhomUuTien IS NULL OR :withoutNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NULL)
              AND (:withNhomUuTien IS NULL OR :withNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NOT NULL)
              AND (
                    LOWER(COALESCE(dt.ten, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(dt.ma, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(t.ma, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(t.ten, '')) LIKE CONCAT('%', :keyword, '%')
                    OR EXISTS (
                        SELECT 1
                        FROM hop_dong_doi_tuong_gia_tri g
                        WHERE g.hop_dong_doi_tuong_id = d.id
                          AND g.ngay_xoa IS NULL
                          AND LOWER(COALESCE(g.gia_tri, '')) LIKE CONCAT('%', :keyword, '%')
                    )
                  )
              AND (:hasExclude = FALSE OR d.id NOT IN (:excludeIds))
              AND (:hasScope = FALSE OR d.id IN (:scopeIds))
            """, nativeQuery = true)
    List<UUID> findIdsByFilterWithKeyword(
            @Param("activeOnly") Boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("trangThaiHopDongId") UUID trangThaiHopDongId,
            @Param("hopDongNhomUuTienId") UUID hopDongNhomUuTienId,
            @Param("keyword") String keyword,
            @Param("withoutNhomUuTien") Boolean withoutNhomUuTien,
            @Param("withNhomUuTien") Boolean withNhomUuTien,
            @Param("hasExclude") boolean hasExclude,
            @Param("excludeIds") Collection<UUID> excludeIds,
            @Param("hasScope") boolean hasScope,
            @Param("scopeIds") Collection<UUID> scopeIds);

    @Query(value = """
            SELECT COUNT(*)
            FROM hop_dong_doi_tuong d
            WHERE (:includeDeleted = TRUE OR d.ngay_xoa IS NULL)
              AND (:activeOnly IS NULL OR (:activeOnly = TRUE AND d.hoat_dong = TRUE) OR (:activeOnly = FALSE AND d.hoat_dong = FALSE))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongQuanLyId AS uuid) IS NULL OR d.doi_tuong_quan_ly_id = CAST(:doiTuongQuanLyId AS uuid))
              AND (CAST(:trangThaiHopDongId AS uuid) IS NULL OR d.trang_thai_hop_dong_id = CAST(:trangThaiHopDongId AS uuid))
              AND (:withoutNhomUuTien IS NULL OR :withoutNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NULL)
              AND (:withNhomUuTien IS NULL OR :withNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NOT NULL)
            """, nativeQuery = true)
    long countSearchNative(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("trangThaiHopDongId") UUID trangThaiHopDongId,
            @Param("withoutNhomUuTien") Boolean withoutNhomUuTien,
            @Param("withNhomUuTien") Boolean withNhomUuTien);

    @Query(value = """
            SELECT d.*
            FROM hop_dong_doi_tuong d
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id
            LEFT JOIN trang_thai_hop_dong t ON t.id = d.trang_thai_hop_dong_id
            WHERE (:includeDeleted = TRUE OR d.ngay_xoa IS NULL)
              AND (:activeOnly IS NULL OR (:activeOnly = TRUE AND d.hoat_dong = TRUE) OR (:activeOnly = FALSE AND d.hoat_dong = FALSE))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongQuanLyId AS uuid) IS NULL OR d.doi_tuong_quan_ly_id = CAST(:doiTuongQuanLyId AS uuid))
              AND (CAST(:trangThaiHopDongId AS uuid) IS NULL OR d.trang_thai_hop_dong_id = CAST(:trangThaiHopDongId AS uuid))
              AND (:withoutNhomUuTien IS NULL OR :withoutNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NULL)
              AND (:withNhomUuTien IS NULL OR :withNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NOT NULL)
              AND (
                    LOWER(COALESCE(dt.ten, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(dt.ma, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(t.ma, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(t.ten, '')) LIKE CONCAT('%', :keyword, '%')
                    OR EXISTS (
                        SELECT 1
                        FROM hop_dong_doi_tuong_gia_tri g
                        WHERE g.hop_dong_doi_tuong_id = d.id
                          AND g.ngay_xoa IS NULL
                          AND LOWER(COALESCE(g.gia_tri, '')) LIKE CONCAT('%', :keyword, '%')
                    )
                  )
            ORDER BY d.ngay_tao DESC, d.id DESC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<HopDongDoiTuong> searchPageWithKeyword(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("trangThaiHopDongId") UUID trangThaiHopDongId,
            @Param("keyword") String keyword,
            @Param("withoutNhomUuTien") Boolean withoutNhomUuTien,
            @Param("withNhomUuTien") Boolean withNhomUuTien,
            @Param("size") int size,
            @Param("offset") long offset);

    @Query(value = """
            SELECT d.id
            FROM hop_dong_doi_tuong d
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id
            LEFT JOIN trang_thai_hop_dong t ON t.id = d.trang_thai_hop_dong_id
            WHERE (:includeDeleted = TRUE OR d.ngay_xoa IS NULL)
              AND (:activeOnly IS NULL OR (:activeOnly = TRUE AND d.hoat_dong = TRUE) OR (:activeOnly = FALSE AND d.hoat_dong = FALSE))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongQuanLyId AS uuid) IS NULL OR d.doi_tuong_quan_ly_id = CAST(:doiTuongQuanLyId AS uuid))
              AND (CAST(:trangThaiHopDongId AS uuid) IS NULL OR d.trang_thai_hop_dong_id = CAST(:trangThaiHopDongId AS uuid))
              AND (CAST(:hopDongNhomUuTienId AS uuid) IS NULL OR d.hop_dong_nhom_uu_tien_id = CAST(:hopDongNhomUuTienId AS uuid))
              AND (:withoutNhomUuTien IS NULL OR :withoutNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NULL)
              AND (:withNhomUuTien IS NULL OR :withNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NOT NULL)
              AND (
                    LOWER(COALESCE(dt.ten, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(dt.ma, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(t.ma, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(t.ten, '')) LIKE CONCAT('%', :keyword, '%')
                    OR EXISTS (
                        SELECT 1
                        FROM hop_dong_doi_tuong_gia_tri g
                        WHERE g.hop_dong_doi_tuong_id = d.id
                          AND g.ngay_xoa IS NULL
                          AND LOWER(COALESCE(g.gia_tri, '')) LIKE CONCAT('%', :keyword, '%')
                    )
                  )
            ORDER BY d.ngay_tao DESC, d.id DESC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<UUID> searchIdsPageWithKeyword(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("trangThaiHopDongId") UUID trangThaiHopDongId,
            @Param("hopDongNhomUuTienId") UUID hopDongNhomUuTienId,
            @Param("keyword") String keyword,
            @Param("withoutNhomUuTien") Boolean withoutNhomUuTien,
            @Param("withNhomUuTien") Boolean withNhomUuTien,
            @Param("size") int size,
            @Param("offset") long offset);

    @Query(value = """
            SELECT COUNT(*)
            FROM hop_dong_doi_tuong d
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id
            LEFT JOIN trang_thai_hop_dong t ON t.id = d.trang_thai_hop_dong_id
            WHERE (:includeDeleted = TRUE OR d.ngay_xoa IS NULL)
              AND (:activeOnly IS NULL OR (:activeOnly = TRUE AND d.hoat_dong = TRUE) OR (:activeOnly = FALSE AND d.hoat_dong = FALSE))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongQuanLyId AS uuid) IS NULL OR d.doi_tuong_quan_ly_id = CAST(:doiTuongQuanLyId AS uuid))
              AND (CAST(:trangThaiHopDongId AS uuid) IS NULL OR d.trang_thai_hop_dong_id = CAST(:trangThaiHopDongId AS uuid))
              AND (:withoutNhomUuTien IS NULL OR :withoutNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NULL)
              AND (:withNhomUuTien IS NULL OR :withNhomUuTien = FALSE OR d.hop_dong_nhom_uu_tien_id IS NOT NULL)
              AND (
                    LOWER(COALESCE(dt.ten, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(dt.ma, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(t.ma, '')) LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(COALESCE(t.ten, '')) LIKE CONCAT('%', :keyword, '%')
                    OR EXISTS (
                        SELECT 1
                        FROM hop_dong_doi_tuong_gia_tri g
                        WHERE g.hop_dong_doi_tuong_id = d.id
                          AND g.ngay_xoa IS NULL
                          AND LOWER(COALESCE(g.gia_tri, '')) LIKE CONCAT('%', :keyword, '%')
                    )
                  )
            """, nativeQuery = true)
    long countSearchWithKeyword(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("trangThaiHopDongId") UUID trangThaiHopDongId,
            @Param("keyword") String keyword,
            @Param("withoutNhomUuTien") Boolean withoutNhomUuTien,
            @Param("withNhomUuTien") Boolean withNhomUuTien);

    @Query("""
            SELECT COUNT(d)
            FROM HopDongDoiTuong d
            JOIN HopDong h ON h.id = d.hopDongId
            JOIN DoiTuongQuanLy dtql ON dtql.id = d.doiTuongQuanLyId
            WHERE d.ngayXoa IS NULL
              AND h.ngayXoa IS NULL
              AND dtql.ngayXoa IS NULL
              AND (d.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND (h.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND (:kieuHopDongId IS NULL OR h.kieuHopDongId = :kieuHopDongId)
              AND d.doiTuongQuanLyId <> :hangMucDoiTuongId
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%boq%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hang muc%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hạng mục%'
            """)
    long countFilteredByLoaiHopDong(
            @Param("activeOnly") Boolean activeOnly,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId,
            @Param("hangMucDoiTuongId") UUID hangMucDoiTuongId);

    @Query("""
            SELECT COUNT(d)
            FROM HopDongDoiTuong d
            JOIN HopDong h ON h.id = d.hopDongId
            WHERE d.ngayXoa IS NULL
              AND h.ngayXoa IS NULL
              AND d.hoatDong = TRUE
              AND h.hoatDong = TRUE
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND (:khuVucId IS NULL OR d.khuVucId = :khuVucId)
            """)
    long countActiveScoped(
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("khuVucId") UUID khuVucId);

    @Query("""
            SELECT h.loaiHopDongId, COUNT(d)
            FROM HopDongDoiTuong d
            JOIN HopDong h ON h.id = d.hopDongId
            WHERE d.ngayXoa IS NULL
              AND h.ngayXoa IS NULL
              AND (d.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND h.loaiHopDongId IS NOT NULL
            GROUP BY h.loaiHopDongId
            """)
    List<Object[]> countGroupByLoaiHopDongId(@Param("activeOnly") Boolean activeOnly);

    @Query("""
            SELECT h.loaiHopDongId, d.doiTuongQuanLyId, COUNT(d)
            FROM HopDongDoiTuong d
            JOIN HopDong h ON h.id = d.hopDongId
            WHERE d.ngayXoa IS NULL
              AND h.ngayXoa IS NULL
              AND (d.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND h.loaiHopDongId IS NOT NULL
              AND d.doiTuongQuanLyId IS NOT NULL
            GROUP BY h.loaiHopDongId, d.doiTuongQuanLyId
            """)
    List<Object[]> countGroupByLoaiAndDoiTuongQuanLy(@Param("activeOnly") Boolean activeOnly);

    @Query("""
            SELECT UPPER(COALESCE(t.ma, t.ten, CASE WHEN d.ngayHtTc IS NOT NULL THEN 'HT' ELSE 'UNKNOWN' END)), COUNT(d)
            FROM HopDongDoiTuong d
            JOIN HopDong h ON h.id = d.hopDongId
            LEFT JOIN TrangThaiHopDong t ON t.id = d.trangThaiHopDongId
            WHERE d.ngayXoa IS NULL
              AND h.ngayXoa IS NULL
              AND (d.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
            GROUP BY UPPER(COALESCE(t.ma, t.ten, CASE WHEN d.ngayHtTc IS NOT NULL THEN 'HT' ELSE 'UNKNOWN' END))
            """)
    List<Object[]> countGroupByTrangThaiMa(
            @Param("activeOnly") Boolean activeOnly,
            @Param("loaiHopDongId") UUID loaiHopDongId);

    @Query("""
            SELECT h.id, COUNT(d)
            FROM HopDongDoiTuong d
            JOIN HopDong h ON h.id = d.hopDongId
            JOIN DoiTuongQuanLy dtql ON dtql.id = d.doiTuongQuanLyId
            WHERE d.ngayXoa IS NULL
              AND h.ngayXoa IS NULL
              AND dtql.ngayXoa IS NULL
              AND (d.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND (h.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND (:kieuHopDongId IS NULL OR h.kieuHopDongId = :kieuHopDongId)
              AND d.doiTuongQuanLyId <> :hangMucDoiTuongId
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%boq%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hang muc%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hạng mục%'
            GROUP BY h.id
            """)
    List<Object[]> countGroupByHopDongId(
            @Param("activeOnly") Boolean activeOnly,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId,
            @Param("hangMucDoiTuongId") UUID hangMucDoiTuongId);

    /**
     * Đếm số đối tượng đang có vướng mắc mở (cột denormalize coVuongMacMo), nhóm theo hợp đồng.
     */
    @Query("""
            SELECT h.id, COUNT(d)
            FROM HopDongDoiTuong d
            JOIN HopDong h ON h.id = d.hopDongId
            WHERE d.ngayXoa IS NULL
              AND h.ngayXoa IS NULL
              AND d.coVuongMacMo = TRUE
              AND (d.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND (h.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND (:kieuHopDongId IS NULL OR h.kieuHopDongId = :kieuHopDongId)
            GROUP BY h.id
            """)
    List<Object[]> countVuongMacMoGroupByHopDongId(
            @Param("activeOnly") Boolean activeOnly,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId);

    @Query("""
            SELECT h.id, UPPER(COALESCE(t.ma, t.ten, CASE WHEN d.ngayHtTc IS NOT NULL THEN 'HT' ELSE 'UNKNOWN' END)), COUNT(d)
            FROM HopDongDoiTuong d
            JOIN HopDong h ON h.id = d.hopDongId
            JOIN DoiTuongQuanLy dtql ON dtql.id = d.doiTuongQuanLyId
            LEFT JOIN TrangThaiHopDong t ON t.id = d.trangThaiHopDongId
            WHERE d.ngayXoa IS NULL
              AND h.ngayXoa IS NULL
              AND dtql.ngayXoa IS NULL
              AND (d.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND (h.hoatDong = TRUE OR :activeOnly IS NULL OR :activeOnly = FALSE)
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND (:kieuHopDongId IS NULL OR h.kieuHopDongId = :kieuHopDongId)
              AND d.doiTuongQuanLyId <> :hangMucDoiTuongId
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%boq%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hang muc%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hạng mục%'
            GROUP BY h.id, UPPER(COALESCE(t.ma, t.ten, CASE WHEN d.ngayHtTc IS NOT NULL THEN 'HT' ELSE 'UNKNOWN' END))
            """)
    List<Object[]> countGroupByHopDongIdAndTrangThaiMa(
            @Param("activeOnly") Boolean activeOnly,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId,
            @Param("hangMucDoiTuongId") UUID hangMucDoiTuongId);

    @Query("""
            SELECT d.hopDongNhomUuTienId, COUNT(d)
            FROM HopDongDoiTuong d
            WHERE d.hopDongId = :hopDongId
              AND d.ngayXoa IS NULL
              AND d.hopDongNhomUuTienId IS NOT NULL
            GROUP BY d.hopDongNhomUuTienId
            """)
    List<Object[]> countGroupByNhomUuTienForHopDong(@Param("hopDongId") UUID hopDongId);

    @Query("""
            SELECT h.id, COUNT(d)
            FROM HopDongDoiTuong d
            JOIN HopDong h ON h.id = d.hopDongId
            JOIN DoiTuongQuanLy dtql ON dtql.id = d.doiTuongQuanLyId
            WHERE d.ngayXoa IS NULL
              AND h.ngayXoa IS NULL
              AND dtql.ngayXoa IS NULL
              AND d.hoatDong = TRUE
              AND h.id IN :hopDongIds
              AND d.doiTuongQuanLyId <> :hangMucDoiTuongId
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%boq%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hang muc%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hạng mục%'
            GROUP BY h.id
            """)
    List<Object[]> countGroupByHopDongIdForIds(
            @Param("hopDongIds") Collection<UUID> hopDongIds,
            @Param("hangMucDoiTuongId") UUID hangMucDoiTuongId);

    /**
     * Đếm đối tượng "chậm tiến độ": đã khởi công (có ngày thi công gần nhất) nhưng chưa hoàn
     * thành (ngayHtTc null) và không có cập nhật thi công nào trong >= nguongNgay ngày gần đây.
     * Cùng ngưỡng 30 ngày với VuongMac.quaHan30Ngay để nhất quán quy ước trong hệ thống.
     */
    @Query("""
            SELECT h.id, COUNT(d)
            FROM HopDongDoiTuong d
            JOIN HopDong h ON h.id = d.hopDongId
            JOIN DoiTuongQuanLy dtql ON dtql.id = d.doiTuongQuanLyId
            WHERE d.ngayXoa IS NULL
              AND h.ngayXoa IS NULL
              AND dtql.ngayXoa IS NULL
              AND d.hoatDong = TRUE
              AND h.id IN :hopDongIds
              AND d.doiTuongQuanLyId <> :hangMucDoiTuongId
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%boq%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hang muc%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hạng mục%'
              AND d.ngayHtTc IS NULL
              AND d.ngayThiCongGanNhat IS NOT NULL
              AND d.ngayThiCongGanNhat < :nguongNgay
            GROUP BY h.id
            """)
    List<Object[]> countChamTienDoGroupByHopDongIdForIds(
            @Param("hopDongIds") Collection<UUID> hopDongIds,
            @Param("hangMucDoiTuongId") UUID hangMucDoiTuongId,
            @Param("nguongNgay") LocalDate nguongNgay);

    @Query("""
            SELECT h.id, UPPER(COALESCE(t.ma, t.ten, CASE WHEN d.ngayHtTc IS NOT NULL THEN 'HT' ELSE 'UNKNOWN' END)), COUNT(d)
            FROM HopDongDoiTuong d
            JOIN HopDong h ON h.id = d.hopDongId
            JOIN DoiTuongQuanLy dtql ON dtql.id = d.doiTuongQuanLyId
            LEFT JOIN TrangThaiHopDong t ON t.id = d.trangThaiHopDongId
            WHERE d.ngayXoa IS NULL
              AND h.ngayXoa IS NULL
              AND dtql.ngayXoa IS NULL
              AND d.hoatDong = TRUE
              AND h.id IN :hopDongIds
              AND d.doiTuongQuanLyId <> :hangMucDoiTuongId
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%boq%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hang muc%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hạng mục%'
            GROUP BY h.id, UPPER(COALESCE(t.ma, t.ten, CASE WHEN d.ngayHtTc IS NOT NULL THEN 'HT' ELSE 'UNKNOWN' END))
            """)
    List<Object[]> countGroupByHopDongIdAndTrangThaiMaForIds(
            @Param("hopDongIds") Collection<UUID> hopDongIds,
            @Param("hangMucDoiTuongId") UUID hangMucDoiTuongId);

    /**
     * Tư vấn thiết kế — Chưa làm / Đang làm: có sản lượng hoặc đã qua bước đầu luồng → đang làm.
     * row: [hopDongId, chuaLamSanLuong, dangLamSanLuong, hoanThanhSanLuong]
     */
    @Query(value = """
            SELECT d.hop_dong_id,
                   SUM(CASE
                       WHEN d.ngay_ht_tc IS NOT NULL THEN 0
                       WHEN UPPER(COALESCE(t.ma, '')) NOT IN ('CXN_HUY', 'CXN_HT')
                            AND (
                                UPPER(COALESCE(t.ma, '')) LIKE '%HUY%'
                                OR UPPER(COALESCE(t.ma, '')) LIKE '%HỦY%'
                                OR UPPER(COALESCE(t.ma, '')) LIKE '%HUỶ%'
                            ) THEN 0
                       WHEN UPPER(COALESCE(t.ma, '')) IN ('HT', 'HOAN_THANH') THEN 0
                       WHEN COALESCE(d.san_luong_hieu_luc, 0) <= 0
                            AND (
                                d.trang_thai_hop_dong_id IS NULL
                                OR UPPER(COALESCE(t.ma, '')) IN ('', 'UNKNOWN', 'CKS')
                                OR UPPER(COALESCE(t.ma, '')) LIKE '%CHUA%'
                                OR UPPER(COALESCE(t.ma, '')) LIKE '%CHƯA%'
                            ) THEN 1
                       ELSE 0
                   END),
                   SUM(CASE
                       WHEN d.ngay_ht_tc IS NOT NULL THEN 0
                       WHEN UPPER(COALESCE(t.ma, '')) NOT IN ('CXN_HUY', 'CXN_HT')
                            AND (
                                UPPER(COALESCE(t.ma, '')) LIKE '%HUY%'
                                OR UPPER(COALESCE(t.ma, '')) LIKE '%HỦY%'
                                OR UPPER(COALESCE(t.ma, '')) LIKE '%HUỶ%'
                            ) THEN 0
                       WHEN UPPER(COALESCE(t.ma, '')) IN ('HT', 'HOAN_THANH') THEN 0
                       WHEN COALESCE(d.san_luong_hieu_luc, 0) > 0 THEN 1
                       WHEN d.trang_thai_hop_dong_id IS NOT NULL
                            AND UPPER(COALESCE(t.ma, '')) NOT IN ('', 'UNKNOWN', 'CKS')
                            AND UPPER(COALESCE(t.ma, '')) NOT LIKE '%CHUA%'
                            AND UPPER(COALESCE(t.ma, '')) NOT LIKE '%CHƯA%'
                            THEN 1
                       ELSE 0
                   END),
                   SUM(CASE
                       WHEN d.ngay_ht_tc IS NOT NULL THEN 1
                       WHEN UPPER(COALESCE(t.ma, '')) IN ('HT', 'HOAN_THANH') THEN 1
                       ELSE 0
                   END)
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL
            INNER JOIN doi_tuong_quan_ly dtql ON dtql.id = d.doi_tuong_quan_ly_id AND dtql.ngay_xoa IS NULL
            LEFT JOIN trang_thai_hop_dong t ON t.id = d.trang_thai_hop_dong_id
            WHERE d.ngay_xoa IS NULL
              AND d.hoat_dong = TRUE
              AND d.hop_dong_id IN :hopDongIds
              AND d.doi_tuong_quan_ly_id <> :hangMucDoiTuongId
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%boq%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hang muc%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hạng mục%'
            GROUP BY d.hop_dong_id
            """, nativeQuery = true)
    List<Object[]> countTuVanSanLuongGroupByHopDongIdForIds(
            @Param("hopDongIds") Collection<UUID> hopDongIds,
            @Param("hangMucDoiTuongId") UUID hangMucDoiTuongId);

    @Query("""
            SELECT d.id FROM HopDongDoiTuong d
            JOIN HopDong h ON h.id = d.hopDongId AND h.ngayXoa IS NULL AND h.hoatDong = TRUE
            WHERE d.ngayXoa IS NULL
              AND d.hoatDong = TRUE
              AND (:hopDongId IS NULL OR d.hopDongId = :hopDongId)
            ORDER BY d.ngayCapNhat DESC, d.id DESC
            """)
    List<UUID> findActiveIdsWithActiveHopDong(@Param("hopDongId") UUID hopDongId);

    /**
     * Đối tượng "đủ điều kiện quyết toán, chưa quá hạn" — HTTC + đã có pháp lý (cột thật
     * hop_dong.trang_thai_phap_ly) + chưa quyết toán + số ngày tồn CHƯA vượt ngưỡng. KHÔNG lọc
     * "đang vướng mắc" ở đây (bảng vuong_mac thuộc module khác) — service tự loại tiếp bằng
     * VuongMacRepository.findDoiTuongIdsCoVuongDangMo cho đúng tập id của trang đã fetch.
     */
    @Query(value = """
            WITH candidate AS (
                SELECT d.id AS id,
                       d.hop_dong_id AS hop_dong_id,
                       h.ma_hop_dong AS ma_hop_dong,
                       h.gia_tri_hd AS gia_tri_hd,
                       d.san_luong_hieu_luc AS san_luong_hieu_luc,
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
                WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
                  AND d.co_vuong_mac_mo = FALSE
                  AND h.trang_thai_phap_ly = 2
                  AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
                  AND NOT (
                        d.quyet_toan_thuc IS NOT NULL
                        OR UPPER(COALESCE(t.ma, '')) = 'QT'
                        OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYET TOAN%'
                        OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYẾT TOÁN%'
                      )
            )
            SELECT id, hop_dong_id, ma_hop_dong, gia_tri_hd, san_luong_hieu_luc, resolved_ngay_ht
            FROM candidate
            WHERE resolved_ngay_ht IS NOT NULL
              AND (CAST(:dateFrom AS date) IS NULL OR resolved_ngay_ht >= CAST(:dateFrom AS date))
              AND (CAST(:dateTo AS date) IS NULL OR resolved_ngay_ht <= CAST(:dateTo AS date))
              AND resolved_ngay_ht > CAST(:nguongNgay AS date)
            ORDER BY resolved_ngay_ht ASC, id ASC
            """,
            countQuery = """
                    WITH candidate AS (
                        SELECT d.id AS id,
                               h.trang_thai_phap_ly,
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
                        WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
                          AND d.co_vuong_mac_mo = FALSE
                          AND h.trang_thai_phap_ly = 2
                          AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
                          AND NOT (
                                d.quyet_toan_thuc IS NOT NULL
                                OR UPPER(COALESCE(t.ma, '')) = 'QT'
                                OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYET TOAN%'
                                OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYẾT TOÁN%'
                              )
                    )
                    SELECT COUNT(*)
                    FROM candidate
                    WHERE resolved_ngay_ht IS NOT NULL
                      AND (CAST(:dateFrom AS date) IS NULL OR resolved_ngay_ht >= CAST(:dateFrom AS date))
                      AND (CAST(:dateTo AS date) IS NULL OR resolved_ngay_ht <= CAST(:dateTo AS date))
                      AND resolved_ngay_ht > CAST(:nguongNgay AS date)
                    """,
            nativeQuery = true)
    Page<HopDongDoiTuongTonCandidateRow> findChoQuyetToan(
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("nguongNgay") LocalDate nguongNgay,
            Pageable pageable);

    /**
     * Giống findChoQuyetToan nhưng số ngày tồn ĐÃ vượt ngưỡng — xem javadoc findChoQuyetToan.
     */
    @Query(value = """
            WITH candidate AS (
                SELECT d.id AS id,
                       d.hop_dong_id AS hop_dong_id,
                       h.ma_hop_dong AS ma_hop_dong,
                       h.gia_tri_hd AS gia_tri_hd,
                       d.san_luong_hieu_luc AS san_luong_hieu_luc,
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
                WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
                  AND d.co_vuong_mac_mo = FALSE
                  AND h.trang_thai_phap_ly = 2
                  AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
                  AND NOT (
                        d.quyet_toan_thuc IS NOT NULL
                        OR UPPER(COALESCE(t.ma, '')) = 'QT'
                        OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYET TOAN%'
                        OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYẾT TOÁN%'
                      )
            )
            SELECT id, hop_dong_id, ma_hop_dong, gia_tri_hd, san_luong_hieu_luc, resolved_ngay_ht
            FROM candidate
            WHERE resolved_ngay_ht IS NOT NULL
              AND (CAST(:dateFrom AS date) IS NULL OR resolved_ngay_ht >= CAST(:dateFrom AS date))
              AND (CAST(:dateTo AS date) IS NULL OR resolved_ngay_ht <= CAST(:dateTo AS date))
              AND resolved_ngay_ht <= CAST(:nguongNgay AS date)
            ORDER BY resolved_ngay_ht ASC, id ASC
            """,
            countQuery = """
                    WITH candidate AS (
                        SELECT d.id AS id,
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
                        WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
                          AND d.co_vuong_mac_mo = FALSE
                          AND h.trang_thai_phap_ly = 2
                          AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
                          AND NOT (
                                d.quyet_toan_thuc IS NOT NULL
                                OR UPPER(COALESCE(t.ma, '')) = 'QT'
                                OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYET TOAN%'
                                OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYẾT TOÁN%'
                              )
                    )
                    SELECT COUNT(*)
                    FROM candidate
                    WHERE resolved_ngay_ht IS NOT NULL
                      AND (CAST(:dateFrom AS date) IS NULL OR resolved_ngay_ht >= CAST(:dateFrom AS date))
                      AND (CAST(:dateTo AS date) IS NULL OR resolved_ngay_ht <= CAST(:dateTo AS date))
                      AND resolved_ngay_ht <= CAST(:nguongNgay AS date)
                    """,
            nativeQuery = true)
    Page<HopDongDoiTuongTonCandidateRow> findQuaHan(
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("nguongNgay") LocalDate nguongNgay,
            Pageable pageable);

    /**
     * Đối tượng thuộc hợp đồng chưa có pháp lý (trang_thai_phap_ly IS NULL hoặc != 2) — danh sách
     * cơ bản theo nghiệp vụ module trạm tồn. Số ngày tồn tính từ ngày tạo đối tượng.
     */
    @Query(value = """
            SELECT d.id AS id,
                   d.hop_dong_id AS hop_dong_id,
                   h.ma_hop_dong AS ma_hop_dong,
                   h.gia_tri_hd AS gia_tri_hd,
                   d.san_luong_hieu_luc AS san_luong_hieu_luc,
                   (d.ngay_tao AT TIME ZONE 'Asia/Ho_Chi_Minh')::date AS resolved_ngay_ht
            FROM hop_dong_doi_tuong d
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.hoat_dong = TRUE AND h.ngay_xoa IS NULL
            WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
              AND (h.trang_thai_phap_ly IS NULL OR h.trang_thai_phap_ly <> 2)
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
            ORDER BY d.ngay_tao ASC, d.id ASC
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM hop_dong_doi_tuong d
                    JOIN hop_dong h ON h.id = d.hop_dong_id AND h.hoat_dong = TRUE AND h.ngay_xoa IS NULL
                    WHERE d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
                      AND (h.trang_thai_phap_ly IS NULL OR h.trang_thai_phap_ly <> 2)
                      AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
                    """,
            nativeQuery = true)
    Page<HopDongDoiTuongTonCandidateRow> findChuaPhapLy(
            @Param("loaiHopDongId") UUID loaiHopDongId,
            Pageable pageable);

    /**
     * Lookup 1 đối tượng thuộc HĐ chưa pháp lý — dùng cho chi tiết trạm tồn.
     */
    @Query(value = """
            SELECT d.id AS id,
                   d.hop_dong_id AS hop_dong_id,
                   h.ma_hop_dong AS ma_hop_dong,
                   h.gia_tri_hd AS gia_tri_hd,
                   d.san_luong_hieu_luc AS san_luong_hieu_luc,
                   (d.ngay_tao AT TIME ZONE 'Asia/Ho_Chi_Minh')::date AS resolved_ngay_ht
            FROM hop_dong_doi_tuong d
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.hoat_dong = TRUE AND h.ngay_xoa IS NULL
            WHERE d.id = CAST(:doiTuongId AS uuid)
              AND d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
              AND (h.trang_thai_phap_ly IS NULL OR h.trang_thai_phap_ly <> 2)
            LIMIT 1
            """,
            nativeQuery = true)
        Optional<HopDongDoiTuongTonCandidateRow> findChuaPhapLyByDoiTuongId(
            @Param("doiTuongId") UUID doiTuongId);

    /**
     * Lookup 1 đối tượng chờ quyết toán (chưa quá hạn).
     */
    @Query(value = """
            WITH candidate AS (
                SELECT d.id AS id,
                       d.hop_dong_id AS hop_dong_id,
                       h.ma_hop_dong AS ma_hop_dong,
                       h.gia_tri_hd AS gia_tri_hd,
                       d.san_luong_hieu_luc AS san_luong_hieu_luc,
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
                WHERE d.id = CAST(:doiTuongId AS uuid)
                  AND d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
                  AND d.co_vuong_mac_mo = FALSE
                  AND h.trang_thai_phap_ly = 2
                  AND NOT (
                        d.quyet_toan_thuc IS NOT NULL
                        OR UPPER(COALESCE(t.ma, '')) = 'QT'
                        OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYET TOAN%'
                        OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYẾT TOÁN%'
                      )
            )
            SELECT id, hop_dong_id, ma_hop_dong, gia_tri_hd, san_luong_hieu_luc, resolved_ngay_ht
            FROM candidate
            WHERE resolved_ngay_ht IS NOT NULL
              AND (CAST(:dateFrom AS date) IS NULL OR resolved_ngay_ht >= CAST(:dateFrom AS date))
              AND (CAST(:dateTo AS date) IS NULL OR resolved_ngay_ht <= CAST(:dateTo AS date))
              AND resolved_ngay_ht > CAST(:nguongNgay AS date)
            LIMIT 1
            """,
            nativeQuery = true)
        Optional<HopDongDoiTuongTonCandidateRow> findChoQuyetToanByDoiTuongId(
            @Param("doiTuongId") UUID doiTuongId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("nguongNgay") LocalDate nguongNgay);

    /**
     * Lookup 1 đối tượng quá hạn quyết toán.
     */
    @Query(value = """
            WITH candidate AS (
                SELECT d.id AS id,
                       d.hop_dong_id AS hop_dong_id,
                       h.ma_hop_dong AS ma_hop_dong,
                       h.gia_tri_hd AS gia_tri_hd,
                       d.san_luong_hieu_luc AS san_luong_hieu_luc,
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
                WHERE d.id = CAST(:doiTuongId AS uuid)
                  AND d.hoat_dong = TRUE AND d.ngay_xoa IS NULL
                  AND d.co_vuong_mac_mo = FALSE
                  AND h.trang_thai_phap_ly = 2
                  AND NOT (
                        d.quyet_toan_thuc IS NOT NULL
                        OR UPPER(COALESCE(t.ma, '')) = 'QT'
                        OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYET TOAN%'
                        OR UPPER(COALESCE(t.ma, '')) LIKE '%QUYẾT TOÁN%'
                      )
            )
            SELECT id, hop_dong_id, ma_hop_dong, gia_tri_hd, san_luong_hieu_luc, resolved_ngay_ht
            FROM candidate
            WHERE resolved_ngay_ht IS NOT NULL
              AND (CAST(:dateFrom AS date) IS NULL OR resolved_ngay_ht >= CAST(:dateFrom AS date))
              AND (CAST(:dateTo AS date) IS NULL OR resolved_ngay_ht <= CAST(:dateTo AS date))
              AND resolved_ngay_ht <= CAST(:nguongNgay AS date)
            LIMIT 1
            """,
            nativeQuery = true)
        Optional<HopDongDoiTuongTonCandidateRow> findQuaHanByDoiTuongId(
            @Param("doiTuongId") UUID doiTuongId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("nguongNgay") LocalDate nguongNgay);

    /**
     * Hàng nhẹ phục vụ dashboard phân công.
     */
    @Query(value = """
            SELECT d.id, d.hop_dong_id, d.nha_thau_id, d.trang_thai_hop_dong_id, d.khu_vuc_id,
                   d.tinh_thanh_id, d.ngay_ht_tc, d.co_vuong_mac_mo, d.quyet_toan_thuc, d.san_luong_hieu_luc
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
            """, nativeQuery = true)
    List<Object[]> findActiveRowsForPhanCong();
}
