package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HopDongRepository extends JpaRepository<HopDong, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE HopDong h SET h.tongThanhTienThiCong = :value WHERE h.id = :id")
    void updateTongThanhTienThiCong(@Param("id") UUID id, @Param("value") BigDecimal value);

    /**
     * Tính thẳng trong SQL toàn bộ số liệu tongQuan() (volume) — thay cho load hết hop_dong +
     * hop_dong_doi_tuong vào Java rồi loop cộng dồn. Replicate đúng logic
     * VolumeServiceImpl.toRowFromBatch()/VolumeTinhToanHelper.resolveStatus() — xem javadoc ở
     * VolumeServiceImpl.tongQuanAggregate(). heSoOverrides truyền qua 2 mảng song song
     * (overrideIds/overrideHeSos) thay vì jsonb — đơn giản hơn, bind trực tiếp qua Hibernate.
     * scopeHopDongIds rỗng = không giới hạn phạm vi nhà thầu (xem ContractorScope.isUnrestricted).
     */
    @Query(value = """
            WITH overrides AS (
                SELECT * FROM unnest(CAST(:overrideIds AS uuid[]), CAST(:overrideHeSos AS numeric[])) AS t(id, he_so)
            ),
            hd_base AS (
                SELECT h.id,
                       h.gia_tri_hd,
                       h.tong_thanh_tien_thi_cong AS plan_contract_total,
                       h.loai_hop_dong_id,
                       (h.loai_hop_dong_id = CAST(:gcccLoaiId AS uuid)) AS is_gccc,
                       CASE
                         WHEN h.loai_hop_dong_id = CAST(:gcccLoaiId AS uuid)
                           THEN h.tong_thanh_tien_thi_cong * COALESCE(o.he_so, h.heso_nguong, CAST(:heSoMacDinh AS numeric))
                         ELSE h.tong_thanh_tien_thi_cong * (
                             SELECT heso_nguong FROM loai_hop_dong WHERE id = CAST(:xayMoiConLaiLoaiId AS uuid)
                         )
                       END AS threshold_contract
                FROM hop_dong h
                LEFT JOIN overrides o ON o.id = h.id
                WHERE h.ngay_xoa IS NULL
                  AND h.hoat_dong = TRUE
                  AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
                  AND (
                        CAST(:keyword AS text) IS NULL OR CAST(:keyword AS text) = ''
                        OR LOWER(COALESCE(h.ma_hop_dong, '')) LIKE CONCAT('%', CAST(:keyword AS text), '%')
                        OR LOWER(COALESCE(h.ma, '')) LIKE CONCAT('%', CAST(:keyword AS text), '%')
                        OR LOWER(COALESCE(h.ten, '')) LIKE CONCAT('%', CAST(:keyword AS text), '%')
                        OR EXISTS (
                            SELECT 1 FROM hop_dong_thuoc_tinh ht
                            WHERE ht.hop_dong_id = h.id AND ht.ngay_xoa IS NULL
                              AND LOWER(ht.gia_tri) LIKE CONCAT('%', CAST(:keyword AS text), '%')
                        )
                      )
                  AND (
                        CAST(:hasScope AS boolean) = FALSE
                        OR h.id = ANY(CAST(:scopeHopDongIds AS uuid[]))
                      )
            ),
            hd_scoped AS (
                SELECT b.*,
                       COALESCE(sc.tram_tong, 0) AS tram_tong,
                       CASE
                         WHEN COALESCE(sc.tram_tong, 0) > 0
                           THEN b.plan_contract_total / sc.tram_tong
                         ELSE 0
                       END AS plan_per_tram,
                       CASE
                         WHEN COALESCE(sc.tram_tong, 0) > 0
                              AND b.threshold_contract IS NOT NULL
                              AND b.threshold_contract > 0
                           THEN b.threshold_contract / sc.tram_tong
                         ELSE NULL
                       END AS threshold_per_tram
                FROM hd_base b
                LEFT JOIN (
                    SELECT hop_dong_id, COUNT(*) AS tram_tong
                    FROM hop_dong_doi_tuong
                    WHERE ngay_xoa IS NULL AND hoat_dong = TRUE
                    GROUP BY hop_dong_id
                ) sc ON sc.hop_dong_id = b.id
            ),
            obj_agg AS (
                SELECT d.hop_dong_id,
                       COUNT(*) AS tram_tong,
                       SUM(COALESCE(d.san_luong_hieu_luc, 0)) AS tong_sl_thuc_te,
                       SUM(CASE WHEN b.plan_per_tram > 0
                                     AND COALESCE(d.san_luong_hieu_luc, 0) < b.plan_per_tram
                                THEN 1 ELSE 0 END) AS tram_thieu,
                       SUM(CASE WHEN b.plan_per_tram > 0
                                     AND d.san_luong_hieu_luc > b.plan_per_tram
                                THEN 1 ELSE 0 END) AS tram_thua,
                       SUM(CASE WHEN b.threshold_per_tram IS NOT NULL
                                     AND b.threshold_per_tram > 0
                                     AND d.san_luong_hieu_luc > b.threshold_per_tram
                                THEN 1 ELSE 0 END) AS tram_bat_thuong,
                       COUNT(*) FILTER (WHERE d.quyet_toan_thuc IS NOT NULL) AS tram_da_qt,
                       SUM(COALESCE(d.quyet_toan_thuc, 0)) AS tong_quyet_toan
                FROM hop_dong_doi_tuong d
                JOIN hd_scoped b ON b.id = d.hop_dong_id
                WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
                GROUP BY d.hop_dong_id
            ),
            hd_computed AS (
                SELECT b.id, b.gia_tri_hd,
                       COALESCE(a.tram_tong, 0) AS tram_tong,
                       COALESCE(a.tram_bat_thuong, 0) AS tram_bat_thuong,
                       COALESCE(a.tong_sl_thuc_te, 0) AS tong_sl_thuc_te,
                       COALESCE(a.tong_quyet_toan, 0) AS tong_quyet_toan,
                       CASE
                         WHEN b.gia_tri_hd > 0
                           THEN (CAST(b.gia_tri_hd AS numeric) - COALESCE(a.tong_sl_thuc_te, 0)) * 100.0 / b.gia_tri_hd
                         ELSE 0
                       END AS chenh_lech_percent,
                       CASE
                         WHEN COALESCE(a.tram_thieu, 0) > 0 THEN 'thieu'
                         WHEN COALESCE(a.tram_thua, 0) > 0 OR COALESCE(a.tram_bat_thuong, 0) > 0 THEN 'thua'
                         ELSE 'can_bang'
                       END AS trang_thai_volume,
                       CASE
                         WHEN COALESCE(a.tram_bat_thuong, 0) > 0 THEN 'danger'
                         WHEN COALESCE(a.tram_thieu, 0) > 0 OR (COALESCE(a.tram_thua, 0) - COALESCE(a.tram_bat_thuong, 0)) > 0 THEN 'warning'
                         ELSE 'ok'
                       END AS alert_level,
                       CASE
                         WHEN (CASE WHEN b.is_gccc THEN CAST(b.gia_tri_hd AS numeric) ELSE b.threshold_contract END) IS NULL
                              OR (CASE WHEN b.is_gccc THEN CAST(b.gia_tri_hd AS numeric) ELSE b.threshold_contract END) <= 0
                           THEN 'binh_thuong'
                         WHEN COALESCE(a.tong_sl_thuc_te, 0) > (CASE WHEN b.is_gccc THEN CAST(b.gia_tri_hd AS numeric) ELSE b.threshold_contract END)
                           THEN 'vuot_nguong'
                         WHEN (COALESCE(a.tong_sl_thuc_te, 0) * 100.0 / (CASE WHEN b.is_gccc THEN CAST(b.gia_tri_hd AS numeric) ELSE b.threshold_contract END)) >= 90
                           THEN 'canh_bao'
                         ELSE 'binh_thuong'
                       END AS trang_thai,
                       CASE
                         WHEN COALESCE(a.tram_tong, 0) = 0 OR COALESCE(a.tram_da_qt, 0) = 0 THEN 'chua_qt'
                         WHEN COALESCE(a.tram_da_qt, 0) >= COALESCE(a.tram_tong, 0) THEN 'da_qt'
                         ELSE 'dang_qt'
                       END AS trang_thai_qt
                FROM hd_scoped b
                LEFT JOIN obj_agg a ON a.hop_dong_id = b.id
            )
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
    VolumeTongQuanAggregateRow tongQuanAggregate(
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("keyword") String keyword,
            @Param("gcccLoaiId") UUID gcccLoaiId,
            @Param("xayMoiConLaiLoaiId") UUID xayMoiConLaiLoaiId,
            @Param("heSoMacDinh") BigDecimal heSoMacDinh,
            @Param("overrideIds") UUID[] overrideIds,
            @Param("overrideHeSos") BigDecimal[] overrideHeSos,
            @Param("hasScope") boolean hasScope,
            @Param("scopeHopDongIds") UUID[] scopeHopDongIds);

    /** soHopDongTheoLoai cho tongQuan() — cùng bộ filter với tongQuanAggregate ở trên. */
    @Query(value = """
            SELECT h.loai_hop_dong_id, COUNT(*)
            FROM hop_dong h
            WHERE h.ngay_xoa IS NULL
              AND h.hoat_dong = TRUE
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (
                    CAST(:keyword AS text) IS NULL OR CAST(:keyword AS text) = ''
                    OR LOWER(COALESCE(h.ma_hop_dong, '')) LIKE CONCAT('%', CAST(:keyword AS text), '%')
                    OR LOWER(COALESCE(h.ma, '')) LIKE CONCAT('%', CAST(:keyword AS text), '%')
                    OR LOWER(COALESCE(h.ten, '')) LIKE CONCAT('%', CAST(:keyword AS text), '%')
                    OR EXISTS (
                        SELECT 1 FROM hop_dong_thuoc_tinh ht
                        WHERE ht.hop_dong_id = h.id AND ht.ngay_xoa IS NULL
                          AND LOWER(ht.gia_tri) LIKE CONCAT('%', CAST(:keyword AS text), '%')
                    )
                  )
              AND (
                    CAST(:hasScope AS boolean) = FALSE
                    OR h.id = ANY(CAST(:scopeHopDongIds AS uuid[]))
                  )
              AND h.loai_hop_dong_id IS NOT NULL
            GROUP BY h.loai_hop_dong_id
            """, nativeQuery = true)
    List<Object[]> tongQuanTheoLoai(
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("keyword") String keyword,
            @Param("hasScope") boolean hasScope,
            @Param("scopeHopDongIds") UUID[] scopeHopDongIds);

    /** Backfill lần đầu sau migration — xem HopDongThanhTienBackfillRunner. */
    @Query("SELECT h.id FROM HopDong h WHERE h.ngayXoa IS NULL AND h.tongThanhTienThiCong IS NULL")
    List<UUID> findIdsMissingTongThanhTienThiCong();

    List<HopDong> findByNgayXoaIsNull();

    Optional<HopDong> findByIdAndNgayXoaIsNull(UUID id);

    boolean existsByMaHopDongIgnoreCaseAndNgayXoaIsNull(String maHopDong);

    boolean existsByMaHopDongIgnoreCaseAndNgayXoaIsNullAndIdNot(String maHopDong, UUID id);

    List<HopDong> findByIdInAndNgayXoaIsNull(Collection<UUID> ids);

    List<HopDong> findByLoaiHopDongIdAndNgayXoaIsNull(UUID loaiHopDongId);

    List<HopDong> findByKieuHopDongIdAndNgayXoaIsNull(UUID kieuHopDongId);

    @Query(value = """
            SELECT *
            FROM hop_dong h
            WHERE (:includeDeleted = TRUE OR h.ngay_xoa IS NULL)
              AND (:activeOnly IS NULL OR :activeOnly = FALSE OR h.hoat_dong = TRUE)
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (CAST(:kieuHopDongId AS uuid) IS NULL OR h.kieu_hop_dong_id = CAST(:kieuHopDongId AS uuid))
              AND (:includeArchive = TRUE OR NOT EXISTS (
                    SELECT 1 FROM hop_dong_luu_tru lt
                    WHERE lt.hop_dong_id = h.id AND lt.ngay_xoa IS NULL AND LOWER(lt.trang_thai) = 'archived'
                  ))
            ORDER BY h.ngay_tao DESC, h.id DESC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<HopDong> searchPageNative(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId,
            @Param("includeArchive") boolean includeArchive,
            @Param("size") int size,
            @Param("offset") long offset);

    @Query(value = """
            SELECT COUNT(*)
            FROM hop_dong h
            WHERE (:includeDeleted = TRUE OR h.ngay_xoa IS NULL)
              AND (:activeOnly IS NULL OR :activeOnly = FALSE OR h.hoat_dong = TRUE)
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (CAST(:kieuHopDongId AS uuid) IS NULL OR h.kieu_hop_dong_id = CAST(:kieuHopDongId AS uuid))
              AND (:includeArchive = TRUE OR NOT EXISTS (
                    SELECT 1 FROM hop_dong_luu_tru lt
                    WHERE lt.hop_dong_id = h.id AND lt.ngay_xoa IS NULL AND LOWER(lt.trang_thai) = 'archived'
                  ))
            """, nativeQuery = true)
    long countSearchNative(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId,
            @Param("includeArchive") boolean includeArchive);

    @Query("""
            SELECT h FROM HopDong h
            WHERE (:includeDeleted = TRUE OR h.ngayXoa IS NULL)
              AND (:activeOnly IS NULL OR :activeOnly = FALSE OR h.hoatDong = TRUE)
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND (:kieuHopDongId IS NULL OR h.kieuHopDongId = :kieuHopDongId)
              AND (
                    LOWER(COALESCE(h.maHopDong, '')) LIKE CONCAT('%', :keyword, '%') ESCAPE '\\'
                    OR LOWER(COALESCE(h.ma, '')) LIKE CONCAT('%', :keyword, '%') ESCAPE '\\'
                    OR LOWER(COALESCE(h.ten, '')) LIKE CONCAT('%', :keyword, '%') ESCAPE '\\'
                    OR EXISTS (
                        SELECT 1 FROM HopDongThuocTinh ht
                        WHERE ht.hopDongId = h.id
                          AND ht.ngayXoa IS NULL
                          AND LOWER(ht.giaTri) LIKE CONCAT('%', :keyword, '%') ESCAPE '\\'
                    )
                  )
              AND (:includeArchive = TRUE OR NOT EXISTS (
                    SELECT 1 FROM HopDongLuuTru lt
                    WHERE lt.hopDongId = h.id AND lt.ngayXoa IS NULL AND LOWER(lt.trangThai) = 'archived'
                  ))
            ORDER BY h.ngayTao DESC, h.id DESC
            LIMIT :size OFFSET :offset
            """)
    List<HopDong> searchPageWithKeyword(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId,
            @Param("keyword") String keyword,
            @Param("includeArchive") boolean includeArchive,
            @Param("size") int size,
            @Param("offset") long offset);

    @Query("""
            SELECT COUNT(h)
            FROM HopDong h
            WHERE (:includeDeleted = TRUE OR h.ngayXoa IS NULL)
              AND (:activeOnly IS NULL OR :activeOnly = FALSE OR h.hoatDong = TRUE)
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND (:kieuHopDongId IS NULL OR h.kieuHopDongId = :kieuHopDongId)
              AND (
                    LOWER(COALESCE(h.maHopDong, '')) LIKE CONCAT('%', :keyword, '%') ESCAPE '\\'
                    OR LOWER(COALESCE(h.ma, '')) LIKE CONCAT('%', :keyword, '%') ESCAPE '\\'
                    OR LOWER(COALESCE(h.ten, '')) LIKE CONCAT('%', :keyword, '%') ESCAPE '\\'
                    OR EXISTS (
                        SELECT 1 FROM HopDongThuocTinh ht
                        WHERE ht.hopDongId = h.id
                          AND ht.ngayXoa IS NULL
                          AND LOWER(ht.giaTri) LIKE CONCAT('%', :keyword, '%') ESCAPE '\\'
                    )
                  )
              AND (:includeArchive = TRUE OR NOT EXISTS (
                    SELECT 1 FROM HopDongLuuTru lt
                    WHERE lt.hopDongId = h.id AND lt.ngayXoa IS NULL AND LOWER(lt.trangThai) = 'archived'
                  ))
            """)
    long countSearchWithKeyword(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId,
            @Param("keyword") String keyword,
            @Param("includeArchive") boolean includeArchive);

    @Query("""
            SELECT h.loaiHopDongId, COUNT(h)
            FROM HopDong h
            WHERE h.ngayXoa IS NULL
              AND (:activeOnly IS NULL OR :activeOnly = FALSE OR h.hoatDong = TRUE)
            GROUP BY h.loaiHopDongId
            """)
    List<Object[]> countGroupByLoaiHopDongId(@Param("activeOnly") Boolean activeOnly);

    @Query("""
            SELECT COUNT(h)
            FROM HopDong h
            WHERE h.ngayXoa IS NULL
              AND (:activeOnly IS NULL OR :activeOnly = FALSE OR h.hoatDong = TRUE)
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
            """)
    long countFiltered(@Param("activeOnly") Boolean activeOnly, @Param("loaiHopDongId") UUID loaiHopDongId);

    @Query("""
            SELECT h.kieuHopDongId, COUNT(h)
            FROM HopDong h
            WHERE h.ngayXoa IS NULL
              AND (:activeOnly IS NULL OR :activeOnly = FALSE OR h.hoatDong = TRUE)
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND h.kieuHopDongId IS NOT NULL
            GROUP BY h.kieuHopDongId
            """)
    List<Object[]> countGroupByKieuHopDongId(
            @Param("activeOnly") Boolean activeOnly,
            @Param("loaiHopDongId") UUID loaiHopDongId);

    @Query("""
            SELECT COUNT(h)
            FROM HopDong h
            WHERE h.ngayXoa IS NULL
              AND (:activeOnly IS NULL OR :activeOnly = FALSE OR h.hoatDong = TRUE)
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND (:kieuHopDongId IS NULL OR h.kieuHopDongId = :kieuHopDongId)
            """)
    long countFilteredWithKieu(
            @Param("activeOnly") Boolean activeOnly,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId);

    /**
     * Nạp 1 lần toàn bộ hợp đồng dưới dạng "thẻ thống kê" — nguồn cho {@code thongKeTatCa()}:
     * gom theo loại/kiểu/trạng thái ngay trong Java thay vì chạy lại bộ countGroupBy*() một lần
     * cho mỗi loaiHopDongId (N+1 theo số loại hợp đồng).
     * Trả về [id, loaiHopDongId, kieuHopDongId, trangThaiThiCong, maHopDong, ten].
     */
    @Query("""
            SELECT h.id, h.loaiHopDongId, h.kieuHopDongId,
                   COALESCE(h.trangThaiThiCong, 'CHUA_TC'), h.maHopDong, h.ten
            FROM HopDong h
            WHERE h.ngayXoa IS NULL
              AND (:activeOnly IS NULL OR :activeOnly = FALSE OR h.hoatDong = TRUE)
            ORDER BY h.ngayTao DESC, h.id DESC
            """)
    List<Object[]> findThongKeRows(@Param("activeOnly") Boolean activeOnly);

    @Query("""
            SELECT COALESCE(h.trangThaiThiCong, 'CHUA_TC'), COUNT(h)
            FROM HopDong h
            WHERE h.ngayXoa IS NULL
              AND h.hoatDong = TRUE
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND (:kieuHopDongId IS NULL OR h.kieuHopDongId = :kieuHopDongId)
            GROUP BY COALESCE(h.trangThaiThiCong, 'CHUA_TC')
            """)
    List<Object[]> countGroupByTrangThaiThiCong(
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId);

    @Query("""
            SELECT h.id, h.hesoNguong
            FROM HopDong h
            WHERE h.ngayXoa IS NULL
              AND h.hoatDong = TRUE
              AND h.hesoNguong IS NOT NULL
              AND h.loaiHopDongId = :gcccLoaiId
            """)
    List<Object[]> findGcccVolumeHeSoOverrides(@Param("gcccLoaiId") UUID gcccLoaiId);

    @Query("""
            SELECT h.id
            FROM HopDong h
            WHERE h.ngayXoa IS NULL
              AND h.hoatDong = TRUE
              AND h.hesoNguong IS NOT NULL
              AND h.loaiHopDongId = :gcccLoaiId
            """)
    List<UUID> findGcccHopDongIdsWithHeSoOverride(@Param("gcccLoaiId") UUID gcccLoaiId);
}
