package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.entity.VuongMac;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VuongMacRepository extends JpaRepository<VuongMac, UUID> {

    Optional<VuongMac> findByIdAndNgayXoaIsNull(UUID id);

    List<VuongMac> findByHopDongIdAndNgayXoaIsNull(UUID hopDongId);

    /** Dùng để evict/refresh cache row khi mã/tỉnh/khu vực/nhà thầu của đối tượng đổi. */
    List<VuongMac> findByDuLieuDoiTuongIdAndNgayXoaIsNull(UUID duLieuDoiTuongId);

    /** Cascade khi hợp đồng đối tượng bị xóa hàng loạt — xem HopDongDoiTuongCascadeDeleteEvent. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE VuongMac v
            SET v.ngayXoa = :now, v.hoatDong = false
            WHERE v.duLieuDoiTuongId IN :ids AND v.ngayXoa IS NULL
            """)
    int softDeleteByDuLieuDoiTuongIds(@Param("ids") Collection<UUID> ids, @Param("now") Instant now);

    @Query("""
            SELECT v.hopDongId, COUNT(v)
            FROM VuongMac v
            WHERE v.ngayXoa IS NULL
              AND v.hoatDong = TRUE
              AND v.hopDongId IS NOT NULL
              AND v.trangThai IN ('pending', 'in_progress')
            GROUP BY v.hopDongId
            """)
    List<Object[]> countOpenIssuesGroupByHopDong();

    @Query("""
            SELECT COUNT(v)
            FROM VuongMac v
            WHERE v.ngayXoa IS NULL
              AND v.hoatDong = TRUE
              AND v.trangThai IN :trangThais
            """)
    long countActiveByTrangThaiIn(@Param("trangThais") Collection<String> trangThais);

    @Query("""
            SELECT COUNT(v)
            FROM VuongMac v
            JOIN HopDong h ON h.id = v.hopDongId
            WHERE v.ngayXoa IS NULL
              AND v.hoatDong = TRUE
              AND h.ngayXoa IS NULL
              AND v.trangThai IN :trangThais
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND (:kieuHopDongId IS NULL OR h.kieuHopDongId = :kieuHopDongId)
            """)
    long countActiveByTrangThaiInAndScope(
            @Param("trangThais") Collection<String> trangThais,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId);

    @Query("""
            SELECT h.id, COUNT(v)
            FROM VuongMac v
            JOIN HopDong h ON h.id = v.hopDongId
            WHERE v.ngayXoa IS NULL
              AND v.hoatDong = TRUE
              AND h.ngayXoa IS NULL
              AND v.trangThai IN ('pending', 'in_progress')
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND (:kieuHopDongId IS NULL OR h.kieuHopDongId = :kieuHopDongId)
            GROUP BY h.id
            """)
    List<Object[]> countOpenIssuesGroupByHopDongScoped(
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("kieuHopDongId") UUID kieuHopDongId);

    @Query("""
            SELECT h.loaiHopDongId, COUNT(v)
            FROM VuongMac v
            JOIN HopDong h ON h.id = v.hopDongId
            WHERE v.ngayXoa IS NULL
              AND v.hoatDong = TRUE
              AND h.ngayXoa IS NULL
              AND v.trangThai IN :trangThais
              AND h.loaiHopDongId IS NOT NULL
            GROUP BY h.loaiHopDongId
            """)
    List<Object[]> countActiveByLoaiHopDongAndTrangThaiIn(
            @Param("trangThais") Collection<String> trangThais);

    /**
     * Đối tượng có vướng mắc đang mở (pending / in_progress).
     * Không tính resolved (đã giải quyết) và rejected (từ chối giải quyết).
     */
    @Query("""
            SELECT DISTINCT v.duLieuDoiTuongId
            FROM VuongMac v
            WHERE v.ngayXoa IS NULL
              AND v.hoatDong = TRUE
              AND v.duLieuDoiTuongId IN :doiTuongIds
              AND v.trangThai IN ('pending', 'in_progress')
            """)
    List<UUID> findDoiTuongIdsCoVuongDangMo(@Param("doiTuongIds") Collection<UUID> doiTuongIds);

    /** Số vướng mắc đang mở (pending/in_progress) của 1 đối tượng — dùng cho cache descriptor. */
    @Query("""
            SELECT COUNT(v)
            FROM VuongMac v
            WHERE v.ngayXoa IS NULL
              AND v.hoatDong = TRUE
              AND v.duLieuDoiTuongId = :doiTuongId
              AND v.trangThai IN ('pending', 'in_progress')
            """)
    long countOpenByDuLieuDoiTuongId(@Param("doiTuongId") UUID doiTuongId);

    /**
     * Vướng mắc đang hoạt động (chưa xóa) trùng đối tượng + giai đoạn — dùng cảnh báo trước khi tạo mới.
     */
    @Query("""
            SELECT v FROM VuongMac v
            WHERE v.ngayXoa IS NULL
              AND v.hoatDong = TRUE
              AND v.duLieuDoiTuongId IN :doiTuongIds
              AND LOWER(v.giaiDoan) = LOWER(:giaiDoan)
            ORDER BY v.ngayTao DESC
            """)
    List<VuongMac> findActiveByDuLieuDoiTuongIdsAndGiaiDoan(
            @Param("doiTuongIds") Collection<UUID> doiTuongIds,
            @Param("giaiDoan") String giaiDoan);

    /** Số vướng mắc đang mở mà user chọn không bổ sung sản lượng — dùng denormalize co_vuong_mac_mo. */
    @Query("""
            SELECT COUNT(v)
            FROM VuongMac v
            WHERE v.ngayXoa IS NULL
              AND v.hoatDong = TRUE
              AND v.duLieuDoiTuongId = :doiTuongId
              AND v.trangThai IN ('pending', 'in_progress')
              AND v.coTheBoSungSanLuong = FALSE
            """)
    long countBlockingOpenByDuLieuDoiTuongId(@Param("doiTuongId") UUID doiTuongId);

    /**
     * Số vướng mắc đang mở (pending/in_progress), phát sinh (ngayTao) trong giai đoạn
     * [tuFromInstant, denToInstant) — dùng cho SanLuongTongHopResponse.issueCount. Không lọc
     * theo search/contractor (đã chốt nghiệp vụ: search chỉ ảnh hưởng danh sách, không ảnh
     * hưởng số liệu tổng hợp). Caller truyền Instant theo giờ VN (xem VietnamDateUtils.ZONE) —
     * ngayTao là Instant nên không so trực tiếp với LocalDate được.
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM vuong_mac v
            JOIN hop_dong_doi_tuong d ON d.id = v.du_lieu_doi_tuong_id
            WHERE v.ngay_xoa IS NULL
              AND v.hoat_dong = TRUE
              AND v.trang_thai IN ('pending', 'in_progress')
              AND d.ngay_xoa IS NULL
              AND d.hoat_dong = TRUE
              AND (CAST(:hopDongId AS uuid) IS NULL OR v.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongQuanLyId AS uuid) IS NULL OR d.doi_tuong_quan_ly_id = CAST(:doiTuongQuanLyId AS uuid))
              AND (:hasScope = FALSE OR v.du_lieu_doi_tuong_id IN (:scopeIds))
              AND (CAST(:tuFromInstant AS timestamptz) IS NULL OR v.ngay_tao >= CAST(:tuFromInstant AS timestamptz))
              AND (CAST(:denToInstant AS timestamptz) IS NULL OR v.ngay_tao < CAST(:denToInstant AS timestamptz))
            """, nativeQuery = true)
    long demMoTrongGiaiDoan(
            @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongQuanLyId") UUID doiTuongQuanLyId,
            @Param("hasScope") boolean hasScope,
            @Param("scopeIds") Collection<UUID> scopeIds,
            @Param("tuFromInstant") Instant tuFromInstant,
            @Param("denToInstant") Instant denToInstant);

    /**
     * Đẩy các tiêu chí có cột thật (active/hopDong/trangThai/giaiDoan/loaiHopDong qua join
     * HopDong) xuống SQL để thu hẹp tập trước khi enrich — thay vì {@code findAll()} rồi lọc
     * hết trong Java như VuongMacServiceImpl.list() cũ. region/province/search theo trường đã
     * enrich (mã trạm, tên HĐ,...) KHÔNG lọc được ở đây vì là thuộc tính động (EAV) — service
     * vẫn phải tự lọc tiếp trong Java trên tập kết quả đã thu hẹp này (xem VuongMacServiceImpl).
     */
    /**
     * nguongNgay: mốc thời điểm (đầu ngày "ngayBaoCao - quaHanNgay") — chỉ giữ vướng mắc tạo
     * TRƯỚC mốc này (đã quá hạn quaHanNgay ngày, strict — không lấy bằng). LUÔN truyền non-null
     * (Instant.MAX khi không lọc — xem VuongMacServiceImpl.resolveNguongNgay) để tránh Hibernate
     * suy sai kiểu tham số null qua CAST (lỗi "cannot cast bytea to timestamp" khi param null).
     * Dùng cho tab "Đang vướng mắc" của trạm tồn.
     */
    @Query("""
            SELECT v FROM VuongMac v
            LEFT JOIN HopDong h ON h.id = v.hopDongId AND h.ngayXoa IS NULL
            LEFT JOIN HopDongDoiTuong hd ON hd.id = v.duLieuDoiTuongId AND hd.ngayXoa IS NULL
            WHERE (:includeDeleted = TRUE OR v.ngayXoa IS NULL)
              AND (:activeOnly = FALSE OR v.hoatDong = TRUE)
              AND (:hopDongId IS NULL OR v.hopDongId = :hopDongId)
              AND (CAST(:trangThai AS string) IS NULL OR LOWER(v.trangThai) = LOWER(CAST(:trangThai AS string)))
              AND (:dangMoOnly = FALSE OR v.trangThai IN ('pending', 'in_progress'))
              AND (CAST(:giaiDoan AS string) IS NULL OR LOWER(v.giaiDoan) = LOWER(CAST(:giaiDoan AS string)))
              AND (CAST(:kieuVuongMac AS string) IS NULL OR LOWER(v.kieuVuongMac) = LOWER(CAST(:kieuVuongMac AS string)))
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND (:khuVucId IS NULL OR hd.khuVucId = :khuVucId)
              AND (:tinhThanhId IS NULL OR hd.tinhThanhId = :tinhThanhId)
              AND v.ngayTao < :nguongNgay
            ORDER BY v.ngayTao DESC
            """)
    List<VuongMac> findFiltered(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("trangThai") String trangThai,
            @Param("dangMoOnly") boolean dangMoOnly,
            @Param("giaiDoan") String giaiDoan,
            @Param("kieuVuongMac") String kieuVuongMac,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId,
            @Param("nguongNgay") Instant nguongNgay);

    /**
     * Bản LIMIT/OFFSET thật ở SQL của {@link #findFiltered} — dùng cho trang Issues.
     * search: LIKE trên cột SQL có sẵn (ma/moTa/tenNguoiBaoCao/maHopDong/ten HĐ/tên người xử lý)
     * — cộng chung điều kiện AND với các filter khác nên LIMIT/OFFSET vẫn đúng. KHÔNG khớp được
     * mã trạm (thuộc tính EAV, không có cột SQL — xem HopDongDanhSachDoiTuongGroupSupport).
     * dateFrom/dateTo: lọc theo v.ngayTao (Instant, đã quy đổi giờ VN ở service), null = không lọc.
     */
    /**
     * dangMoOnly/nguongNgay: cùng ngữ nghĩa với {@link #findFiltered} (nguongNgay LUÔN non-null,
     * NO_NGUONG_NGAY khi không lọc — xem VuongMacServiceImpl.resolveNguongNgay).
     */
    @Query(
            value = """
                    SELECT v FROM VuongMac v
                    LEFT JOIN HopDong h ON h.id = v.hopDongId AND h.ngayXoa IS NULL
                    LEFT JOIN HopDongDoiTuong hd ON hd.id = v.duLieuDoiTuongId AND hd.ngayXoa IS NULL
                    LEFT JOIN NguoiDung nd ON nd.id = v.nguoiXuLyId AND nd.ngayXoa IS NULL
                    WHERE (:includeDeleted = TRUE OR v.ngayXoa IS NULL)
                      AND (:activeOnly = FALSE OR v.hoatDong = TRUE)
                      AND (:hopDongId IS NULL OR v.hopDongId = :hopDongId)
                      AND (CAST(:trangThai AS string) IS NULL OR LOWER(v.trangThai) = LOWER(CAST(:trangThai AS string)))
                      AND (:dangMoOnly = FALSE OR v.trangThai IN ('pending', 'in_progress'))
                      AND (CAST(:giaiDoan AS string) IS NULL OR LOWER(v.giaiDoan) = LOWER(CAST(:giaiDoan AS string)))
                      AND (CAST(:kieuVuongMac AS string) IS NULL OR LOWER(v.kieuVuongMac) = LOWER(CAST(:kieuVuongMac AS string)))
                      AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
                      AND (:khuVucId IS NULL OR hd.khuVucId = :khuVucId)
                      AND (:tinhThanhId IS NULL OR hd.tinhThanhId = :tinhThanhId)
                      AND v.ngayTao < :nguongNgay
                      AND (CAST(:tuNgay AS timestamp) IS NULL OR v.ngayTao >= :tuNgay)
                      AND (CAST(:denNgay AS timestamp) IS NULL OR v.ngayTao < :denNgay)
                      AND (
                            CAST(:keyword AS string) IS NULL
                            OR LOWER(v.ma) LIKE :keyword
                            OR LOWER(v.moTa) LIKE :keyword
                            OR LOWER(COALESCE(v.tenNguoiBaoCao, '')) LIKE :keyword
                            OR LOWER(COALESCE(h.maHopDong, '')) LIKE :keyword
                            OR LOWER(COALESCE(h.ten, '')) LIKE :keyword
                            OR LOWER(COALESCE(nd.hoTen, '')) LIKE :keyword
                          )
                    """,
            countQuery = """
                    SELECT COUNT(v) FROM VuongMac v
                    LEFT JOIN HopDong h ON h.id = v.hopDongId AND h.ngayXoa IS NULL
                    LEFT JOIN HopDongDoiTuong hd ON hd.id = v.duLieuDoiTuongId AND hd.ngayXoa IS NULL
                    LEFT JOIN NguoiDung nd ON nd.id = v.nguoiXuLyId AND nd.ngayXoa IS NULL
                    WHERE (:includeDeleted = TRUE OR v.ngayXoa IS NULL)
                      AND (:activeOnly = FALSE OR v.hoatDong = TRUE)
                      AND (:hopDongId IS NULL OR v.hopDongId = :hopDongId)
                      AND (CAST(:trangThai AS string) IS NULL OR LOWER(v.trangThai) = LOWER(CAST(:trangThai AS string)))
                      AND (:dangMoOnly = FALSE OR v.trangThai IN ('pending', 'in_progress'))
                      AND (CAST(:giaiDoan AS string) IS NULL OR LOWER(v.giaiDoan) = LOWER(CAST(:giaiDoan AS string)))
                      AND (CAST(:kieuVuongMac AS string) IS NULL OR LOWER(v.kieuVuongMac) = LOWER(CAST(:kieuVuongMac AS string)))
                      AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
                      AND (:khuVucId IS NULL OR hd.khuVucId = :khuVucId)
                      AND (:tinhThanhId IS NULL OR hd.tinhThanhId = :tinhThanhId)
                      AND v.ngayTao < :nguongNgay
                      AND (CAST(:tuNgay AS timestamp) IS NULL OR v.ngayTao >= :tuNgay)
                      AND (CAST(:denNgay AS timestamp) IS NULL OR v.ngayTao < :denNgay)
                      AND (
                            CAST(:keyword AS string) IS NULL
                            OR LOWER(v.ma) LIKE :keyword
                            OR LOWER(v.moTa) LIKE :keyword
                            OR LOWER(COALESCE(v.tenNguoiBaoCao, '')) LIKE :keyword
                            OR LOWER(COALESCE(h.maHopDong, '')) LIKE :keyword
                            OR LOWER(COALESCE(h.ten, '')) LIKE :keyword
                            OR LOWER(COALESCE(nd.hoTen, '')) LIKE :keyword
                          )
                    """)
    Page<VuongMac> findFilteredPage(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("trangThai") String trangThai,
            @Param("dangMoOnly") boolean dangMoOnly,
            @Param("giaiDoan") String giaiDoan,
            @Param("kieuVuongMac") String kieuVuongMac,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId,
            @Param("nguongNgay") Instant nguongNgay,
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * Tổng quan (đếm theo trạng thái + quá hạn 30 ngày) bằng 1 query aggregate — thay cho việc
     * fetch hết danh sách rồi đếm ở frontend. Cùng bộ filter với findFilteredPage (không nhận
     * search — xem ghi chú searchPage: search chỉ ảnh hưởng danh sách, không ảnh hưởng thống kê).
     * Trả về mảng 1 dòng: [total, pending, inProgress, resolved, rejected, overdue30Days].
     */
    @Query("""
            SELECT COUNT(v),
                   SUM(CASE WHEN v.trangThai = 'pending' THEN 1 ELSE 0 END),
                   SUM(CASE WHEN v.trangThai = 'in_progress' THEN 1 ELSE 0 END),
                   SUM(CASE WHEN v.trangThai = 'resolved' THEN 1 ELSE 0 END),
                   SUM(CASE WHEN v.trangThai = 'rejected' THEN 1 ELSE 0 END),
                   SUM(CASE WHEN v.quaHan30Ngay = TRUE THEN 1 ELSE 0 END)
            FROM VuongMac v
            LEFT JOIN HopDong h ON h.id = v.hopDongId AND h.ngayXoa IS NULL
            LEFT JOIN HopDongDoiTuong hd ON hd.id = v.duLieuDoiTuongId AND hd.ngayXoa IS NULL
            WHERE (:includeDeleted = TRUE OR v.ngayXoa IS NULL)
              AND (:activeOnly = FALSE OR v.hoatDong = TRUE)
              AND (:hopDongId IS NULL OR v.hopDongId = :hopDongId)
              AND (CAST(:trangThai AS string) IS NULL OR LOWER(v.trangThai) = LOWER(CAST(:trangThai AS string)))
              AND (CAST(:giaiDoan AS string) IS NULL OR LOWER(v.giaiDoan) = LOWER(CAST(:giaiDoan AS string)))
              AND (CAST(:kieuVuongMac AS string) IS NULL OR LOWER(v.kieuVuongMac) = LOWER(CAST(:kieuVuongMac AS string)))
              AND (:loaiHopDongId IS NULL OR h.loaiHopDongId = :loaiHopDongId)
              AND (:khuVucId IS NULL OR hd.khuVucId = :khuVucId)
              AND (:tinhThanhId IS NULL OR hd.tinhThanhId = :tinhThanhId)
            """)
    List<Object[]> countTongQuan(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") boolean activeOnly,
            @Param("hopDongId") UUID hopDongId,
            @Param("trangThai") String trangThai,
            @Param("giaiDoan") String giaiDoan,
            @Param("kieuVuongMac") String kieuVuongMac,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId);

    @Query("""
            SELECT v.kieuVuongMac, COUNT(v)
            FROM VuongMac v
            WHERE v.ngayXoa IS NULL
              AND v.hoatDong = TRUE
            GROUP BY v.kieuVuongMac
            """)
    List<Object[]> countGroupByKieu();

}
