package vn.edu.huce.iic.bts_ops_platform.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.entity.phancong.PhanCong;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PhanCongEntityToolRepository extends JpaRepository<PhanCong, UUID> {

    List<PhanCong> findByNgayXoaIsNull();

    @Query(value = """
            SELECT * FROM phan_cong
            WHERE ngay_xoa IS NULL
              AND (nguoi_dung_id = :nguoiDungId
                   OR LOWER(TRIM(nha_thau)) = LOWER(TRIM(:hoTen))
                   OR LOWER(TRIM(nha_thau)) = LOWER(TRIM(:tenDangNhap)))
              AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR ngay_tao >= CAST(:ngayTaoFrom AS timestamptz))
              AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR ngay_tao < CAST(:ngayTaoTo AS timestamptz))
            """, nativeQuery = true)
    List<PhanCong> findActiveForContractor(@Param("nguoiDungId") UUID nguoiDungId,
                                           @Param("hoTen") String hoTen,
                                           @Param("tenDangNhap") String tenDangNhap, @Param("ngayTaoFrom") Instant ngayTaoFrom, @Param("ngayTaoTo") Instant ngayTaoTo);

    /**
     * Thống kê phân công theo khu vực/tỉnh/hoạt động ngay ở DB (trước đây đọc cả bảng phan_cong lên rồi đếm bằng Java).
     * row: [khuVucId, tinhThanhId, hoatDong, soLuong]. Lọc vùng/nhà thầu/hợp đồng/khu vực nếu truyền (null = không lọc).
     */
    @Query(value = """
            SELECT p.khu_vuc_id, p.tinh_thanh_id, p.hoat_dong, COUNT(*)
            FROM phan_cong p
            WHERE p.ngay_xoa IS NULL
              AND (CAST(:maVung AS text) IS NULL OR LOWER(p.ma_vung) = LOWER(:maVung))
              AND (CAST(:nhaThauPattern AS text) IS NULL OR LOWER(p.nha_thau) LIKE :nhaThauPattern ESCAPE '\\')
              AND (CAST(:hopDongId AS uuid) IS NULL OR p.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR p.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR p.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR p.hop_dong_doi_tuong_id IN (SELECT x.id FROM hop_dong_doi_tuong x WHERE x.ngay_xoa IS NULL
                   AND (x.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR x.id = CAST(:doiTuongId AS uuid))))
              AND (CAST(:giaiDoan AS text) IS NULL OR LOWER(p.giai_doan) = LOWER(:giaiDoan))
              AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR p.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz))
              AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR p.ngay_tao < CAST(:ngayTaoTo AS timestamptz))
            GROUP BY p.khu_vuc_id, p.tinh_thanh_id, p.hoat_dong
            """, nativeQuery = true)
    List<Object[]> thongKe(@Param("maVung") String maVung, @Param("nhaThauPattern") String nhaThauPattern,
                           @Param("hopDongId") UUID hopDongId, @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId,
                           @Param("doiTuongId") UUID doiTuongId, @Param("giaiDoan") String giaiDoan, @Param("ngayTaoFrom") Instant ngayTaoFrom, @Param("ngayTaoTo") Instant ngayTaoTo);

    /** Danh sách phân công đang hoạt động, lọc và phân trang ở DB. Từ khoá chỉ tìm theo giai đoạn (đã là mẫu LIKE). */
    @Query(value = """
            SELECT p.* FROM phan_cong p
            WHERE p.ngay_xoa IS NULL AND (CAST(:lichSu AS boolean) IS TRUE OR p.hoat_dong = TRUE)
              AND (CAST(:keyword AS text) IS NULL OR LOWER(COALESCE(p.giai_doan, '')) LIKE :keyword ESCAPE '\\')
              AND (CAST(:maVung AS text) IS NULL OR LOWER(p.ma_vung) = LOWER(:maVung))
              AND (CAST(:nhaThauPattern AS text) IS NULL OR LOWER(p.nha_thau) LIKE :nhaThauPattern ESCAPE '\\')
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR p.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR p.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR p.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR p.hop_dong_doi_tuong_id IN (SELECT x.id FROM hop_dong_doi_tuong x WHERE x.ngay_xoa IS NULL
                   AND (x.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR x.id = CAST(:doiTuongId AS uuid))))
              AND (CAST(:giaiDoan AS text) IS NULL OR LOWER(p.giai_doan) = LOWER(:giaiDoan))
              AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR p.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz))
              AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR p.ngay_tao < CAST(:ngayTaoTo AS timestamptz))
            ORDER BY p.ngay_tao DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM phan_cong p
            WHERE p.ngay_xoa IS NULL AND (CAST(:lichSu AS boolean) IS TRUE OR p.hoat_dong = TRUE)
              AND (CAST(:keyword AS text) IS NULL OR LOWER(COALESCE(p.giai_doan, '')) LIKE :keyword ESCAPE '\\')
              AND (CAST(:maVung AS text) IS NULL OR LOWER(p.ma_vung) = LOWER(:maVung))
              AND (CAST(:nhaThauPattern AS text) IS NULL OR LOWER(p.nha_thau) LIKE :nhaThauPattern ESCAPE '\\')
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR p.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR p.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR p.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR p.hop_dong_doi_tuong_id IN (SELECT x.id FROM hop_dong_doi_tuong x WHERE x.ngay_xoa IS NULL
                   AND (x.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR x.id = CAST(:doiTuongId AS uuid))))
              AND (CAST(:giaiDoan AS text) IS NULL OR LOWER(p.giai_doan) = LOWER(:giaiDoan))
              AND (CAST(:ngayTaoFrom AS timestamptz) IS NULL OR p.ngay_tao >= CAST(:ngayTaoFrom AS timestamptz))
              AND (CAST(:ngayTaoTo AS timestamptz) IS NULL OR p.ngay_tao < CAST(:ngayTaoTo AS timestamptz))
            """, nativeQuery = true)
    Page<PhanCong> search(@Param("keyword") String keyword, @Param("maVung") String maVung,
                          @Param("nhaThauPattern") String nhaThauPattern,
                          @Param("tinhThanhId") UUID tinhThanhId, @Param("hopDongId") UUID hopDongId, @Param("khuVucId") UUID khuVucId,
                          @Param("doiTuongId") UUID doiTuongId, @Param("giaiDoan") String giaiDoan,
                          @Param("lichSu") Boolean lichSu, @Param("ngayTaoFrom") Instant ngayTaoFrom, @Param("ngayTaoTo") Instant ngayTaoTo, Pageable pageable);
}
