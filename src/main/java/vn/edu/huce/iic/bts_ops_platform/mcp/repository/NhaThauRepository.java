package vn.edu.huce.iic.bts_ops_platform.mcp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhathau.NhaThauInfoProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.nguoidung.NguoiDung;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository riêng cho mcp/services/NhaThauService — tra cứu/kiểm tra tồn tại nhà thầu (bảng nguoi_dung). */
public interface NhaThauRepository extends JpaRepository<NguoiDung, UUID> {

    /** Bước 1 của resolve: khớp ĐÚNG tên đăng nhập (mã định danh, có unique index), không tìm gần đúng. */
    @Query(value = """
            SELECT id, ho_ten AS hoTen FROM nguoi_dung
            WHERE ngay_xoa IS NULL AND ten_dang_nhap = :maDangNhap
            LIMIT 5
            """, nativeQuery = true)
    List<NhaThauInfoProjection> findByTenDangNhapExact(@Param("maDangNhap") String maDangNhap);

    /**
     * Bước 2 (chỉ khi bước 1 không ra): tìm gần đúng theo họ tên, tối đa 5 ứng viên,
     * hạng 1 = khớp đúng, 2 = bắt đầu bằng, 3 = chứa. :k chữ thường, :prefix/:contains đã escape ký tự đại diện LIKE.
     */
    @Query(value = """
            SELECT id, ho_ten AS hoTen,
                   CASE WHEN LOWER(ho_ten) = :k THEN 1
                        WHEN LOWER(ho_ten) LIKE :prefix ESCAPE '\\' THEN 2
                        ELSE 3 END AS hang
            FROM nguoi_dung
            WHERE ngay_xoa IS NULL AND LOWER(ho_ten) LIKE :contains ESCAPE '\\'
            ORDER BY hang, ho_ten
            LIMIT 5
            """, nativeQuery = true)
    List<UngVien> searchGanDung(@Param("k") String k, @Param("prefix") String prefix, @Param("contains") String contains);

    /** Ứng viên tìm gần đúng kèm hạng khớp. */
    interface UngVien extends NhaThauInfoProjection {
        Integer getHang();
    }

    @Query(value = """
            SELECT id, ho_ten AS hoTen FROM nguoi_dung
            WHERE ngay_xoa IS NULL AND id = CAST(:id AS uuid)
            """, nativeQuery = true)
    Optional<NhaThauInfoProjection> findInfoById(@Param("id") UUID id);

    /**
     * Danh sách nhà thầu đang phụ trách >= 1 đối tượng hoạt động (thuộc hợp đồng đang hoạt động), kèm số hợp đồng và số đối tượng
     * trong phạm vi bộ lọc. Từ khoá đã là mẫu LIKE (khớp họ tên hoặc tên đăng nhập). Sắp theo số đối tượng giảm dần rồi tên.
     */
    @Query(value = """
            SELECT nd.id AS id, nd.ho_ten AS hoTen, nd.ten_dang_nhap AS tenDangNhap,
                   COUNT(DISTINCT d.hop_dong_id) AS soHopDong, COUNT(*) AS soDoiTuong
            FROM hop_dong_doi_tuong d
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            JOIN nguoi_dung nd ON nd.id = d.nha_thau_id AND nd.ngay_xoa IS NULL
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
              AND (CAST(:keyword AS text) IS NULL OR LOWER(nd.ho_ten) LIKE :keyword ESCAPE '\\' OR LOWER(nd.ten_dang_nhap) LIKE :keyword ESCAPE '\\')
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
            GROUP BY nd.id, nd.ho_ten, nd.ten_dang_nhap
            ORDER BY COUNT(*) DESC, nd.ho_ten, nd.id
            """,
            countQuery = """
            SELECT COUNT(DISTINCT d.nha_thau_id)
            FROM hop_dong_doi_tuong d
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            JOIN nguoi_dung nd ON nd.id = d.nha_thau_id AND nd.ngay_xoa IS NULL
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
              AND (CAST(:keyword AS text) IS NULL OR LOWER(nd.ho_ten) LIKE :keyword ESCAPE '\\' OR LOWER(nd.ten_dang_nhap) LIKE :keyword ESCAPE '\\')
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
            """, nativeQuery = true)
    Page<NhaThauRow> danhSachNhaThau(@Param("keyword") String keyword, @Param("hopDongId") UUID hopDongId,
                                     @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId, Pageable pageable);

    /** 1 dòng trong danh sách nhà thầu. */
    interface NhaThauRow {
        UUID getId();
        String getHoTen();
        String getTenDangNhap();
        Long getSoHopDong();
        Long getSoDoiTuong();
    }
}
