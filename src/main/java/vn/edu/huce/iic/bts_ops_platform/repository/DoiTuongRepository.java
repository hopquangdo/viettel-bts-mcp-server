package vn.edu.huce.iic.bts_ops_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.dto.doituong.DoiTuongInfoProjection;
import vn.edu.huce.iic.bts_ops_platform.entity.cauhinh.DoiTuongQuanLy;

import java.util.Optional;
import java.util.UUID;

public interface DoiTuongRepository extends JpaRepository<DoiTuongQuanLy, UUID> {

    @Query(value = """
            SELECT id, ma, ten
            FROM doi_tuong_quan_ly
            WHERE ngay_xoa IS NULL AND hoat_dong = TRUE AND id = :id
            """, nativeQuery = true)
        Optional<DoiTuongInfoProjection> findInfoById(@Param("id") UUID id);

    /** Bước 1 của resolve: khớp ĐÚNG mã (dùng index sẵn có), không đụng tới tìm gần đúng. */
    @Query(value = """
            SELECT id, ma, ten FROM doi_tuong_quan_ly
            WHERE ngay_xoa IS NULL AND hoat_dong = TRUE AND (ma = :ma)
            LIMIT 5
            """, nativeQuery = true)
    java.util.List<DoiTuongInfoProjection> findByMaExact(@Param("ma") String ma);

    /**
     * Bước 2 (chỉ khi bước 1 không ra): tìm gần đúng, tối đa 5 ứng viên, hạng 1 = khớp đúng, 2 = bắt đầu bằng, 3 = chứa.
     * :k là từ khoá chữ thường, :prefix/:contains đã escape ký tự đại diện của LIKE.
     */
    @Query(value = """
            SELECT id, ma, ten,
                   CASE WHEN LOWER(ma) = :k OR LOWER(ten) = :k THEN 1
                        WHEN LOWER(ma) LIKE :prefix ESCAPE '\\' OR LOWER(ten) LIKE :prefix ESCAPE '\\' THEN 2
                        ELSE 3 END AS hang
            FROM doi_tuong_quan_ly
            WHERE ngay_xoa IS NULL AND hoat_dong = TRUE AND (LOWER(ma) LIKE :contains ESCAPE '\\' OR LOWER(ten) LIKE :contains ESCAPE '\\')
            ORDER BY hang, ten
            LIMIT 5
            """, nativeQuery = true)
    java.util.List<UngVien> searchGanDung(@Param("k") String k, @Param("prefix") String prefix, @Param("contains") String contains);

    /** Ứng viên tìm gần đúng kèm hạng khớp. */
    interface UngVien extends DoiTuongInfoProjection {
        Integer getHang();
    }

    /**
     * Đối tượng CỤ THỂ (1 trạm, 1 tuyến, 1 tòa nhà…) có giá trị thuộc tính KHOÁ CHÍNH (Mã trạm, Mã tuyến, Tên tòa…) khớp ĐÚNG từ khoá
     * (không phân biệt hoa thường). row: id = id hop_dong_doi_tuong, ma = giá trị khoá, ten = mã hợp đồng (để phân biệt khi trùng).
     */
    @Query(value = """
            SELECT d.id AS id, g.gia_tri AS ma, h.ma_hop_dong AS ten
            FROM hop_dong_doi_tuong_gia_tri g
            JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.la_khoa_chinh = TRUE AND tt.ngay_xoa IS NULL
            JOIN hop_dong_doi_tuong d ON d.id = g.hop_dong_doi_tuong_id AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE g.ngay_xoa IS NULL AND LOWER(g.gia_tri) = :k
            LIMIT 6
            """, nativeQuery = true)
    java.util.List<DoiTuongInfoProjection> findCuTheExact(@Param("k") String k);

    /** Tìm gần đúng đối tượng cụ thể theo giá trị khoá chính, hạng 1 = bắt đầu bằng, 2 = chứa; tối đa 6 ứng viên. */
    @Query(value = """
            SELECT d.id AS id, g.gia_tri AS ma, h.ma_hop_dong AS ten,
                   CASE WHEN LOWER(g.gia_tri) LIKE :prefix ESCAPE '\\' THEN 1 ELSE 2 END AS hang
            FROM hop_dong_doi_tuong_gia_tri g
            JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.la_khoa_chinh = TRUE AND tt.ngay_xoa IS NULL
            JOIN hop_dong_doi_tuong d ON d.id = g.hop_dong_doi_tuong_id AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE g.ngay_xoa IS NULL AND LOWER(g.gia_tri) LIKE :contains ESCAPE '\\'
            ORDER BY hang, g.gia_tri
            LIMIT 6
            """, nativeQuery = true)
    java.util.List<UngVien> searchCuThe(@Param("prefix") String prefix, @Param("contains") String contains);

    /** id có phải 1 đối tượng cụ thể (hop_dong_doi_tuong) đang hoạt động không. */
    @Query(value = "SELECT COUNT(*) FROM hop_dong_doi_tuong d WHERE d.id = :id AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE", nativeQuery = true)
    long countCuTheById(@Param("id") UUID id);
}
