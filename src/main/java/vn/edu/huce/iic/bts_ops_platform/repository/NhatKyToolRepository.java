package vn.edu.huce.iic.bts_ops_platform.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.dto.nhatky.NhatKyRow;
import vn.edu.huce.iic.bts_ops_platform.entity.auditlog.AuditLog;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Đọc nhật ký thao tác (audit_log) theo ID đã resolve. Chỉ lấy các hành động nghiệp vụ trong danh sách cho phép ({@code actions}),
 * không bao giờ trả nhật ký đăng nhập hay tài khoản.
 */
public interface NhatKyToolRepository extends JpaRepository<AuditLog, UUID> {

    /** Điều kiện lọc dùng chung, alias {@code a} = audit_log, {@code d} = hop_dong_doi_tuong. */
    String LOC = """
            WHERE a.hanh_dong = ANY(string_to_array(:actions, ','))
              AND (CAST(:hopDongId AS uuid) IS NULL OR a.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR a.doi_tuong_id = CAST(:doiTuongId AS uuid)
                   OR d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid))
              AND (CAST(:nguoi AS text) IS NULL OR LOWER(COALESCE(a.ten_nguoi_thuc_hien, '')) LIKE :nguoi ESCAPE '\\')
              AND (CAST(:tuFrom AS timestamptz) IS NULL OR a.ngay_tao >= CAST(:tuFrom AS timestamptz))
              AND (CAST(:denTo AS timestamptz) IS NULL OR a.ngay_tao < CAST(:denTo AS timestamptz))
            """;

    String TU = """
            FROM audit_log a
            LEFT JOIN hop_dong_doi_tuong d ON d.id = a.doi_tuong_id
            """;

    @Query(value = """
            SELECT a.ngay_tao AS ngay, a.ten_nguoi_thuc_hien AS nguoiThucHien, a.hanh_dong AS hanhDong, a.mo_ta AS moTa, a.ip AS ip,
                   a.doi_tuong_id AS doiTuongId, a.hop_dong_id AS hopDongId, h.ma_hop_dong AS maHopDong, h.ten AS tenHopDong,
                   COALESCE(q.ten, q.ma) AS loaiDoiTuong
            """ + TU + """
            LEFT JOIN hop_dong h ON h.id = a.hop_dong_id
            LEFT JOIN doi_tuong_quan_ly q ON q.id = d.doi_tuong_quan_ly_id
            """ + LOC + """
            ORDER BY CASE WHEN CAST(:tang AS boolean) THEN EXTRACT(EPOCH FROM a.ngay_tao) END ASC, a.ngay_tao DESC, a.id
            """,
            countQuery = "SELECT COUNT(*) " + TU + LOC, nativeQuery = true)
    Page<NhatKyRow> danhSach(@Param("actions") String actions, @Param("hopDongId") UUID hopDongId, @Param("doiTuongId") UUID doiTuongId,
                             @Param("nguoi") String nguoi, @Param("tuFrom") Instant tuFrom, @Param("denTo") Instant denTo,
                             @Param("tang") Boolean tang, Pageable pageable);

    /** Mốc sớm nhất có nhật ký thuộc nhóm hành động cho trước (vd các thao tác sản lượng). */
    @Query(value = "SELECT MIN(a.ngay_tao) FROM audit_log a WHERE a.hanh_dong = ANY(string_to_array(:actions, ','))", nativeQuery = true)
    Instant mocSomNhat(@Param("actions") String actions);

    /** Giá trị khoá chính (mã trạm, mã tuyến…) của các đối tượng cụ thể, để hiển thị thay cho id. row: [hop_dong_doi_tuong_id, gia_tri]. */
    @Query(value = """
            SELECT g.hop_dong_doi_tuong_id, g.gia_tri
            FROM hop_dong_doi_tuong_gia_tri g
            JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.la_khoa_chinh = TRUE AND tt.ngay_xoa IS NULL
            WHERE g.ngay_xoa IS NULL AND g.hop_dong_doi_tuong_id IN (:ids)
            """, nativeQuery = true)
    List<Object[]> maKhoaChinh(@Param("ids") Collection<UUID> ids);
}
