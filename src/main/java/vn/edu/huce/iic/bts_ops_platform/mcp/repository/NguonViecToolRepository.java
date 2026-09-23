package vn.edu.huce.iic.bts_ops_platform.mcp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.hopdong.HopDong;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Repo tool-riêng cho nguonviec_tool — port lại các query của NguonLucService/SanLuongRepository/
 * VuongMacRepository/HopDongDoiTuongRepository liên quan (san_luong, vuong_mac, hop_dong_doi_tuong,
 * doi_tuong_quan_ly, trang_thai_hop_dong, kieu_hop_dong, loai_hop_dong, hop_dong_thuoc_tinh) thành
 * native SQL, tránh phụ thuộc NguonLucService/NguoiDungRepository ở module ngoài.
 */
public interface NguonViecToolRepository extends JpaRepository<HopDong, UUID> {

    /** Đối tượng quản lý "Hạng mục thi công" — loại trừ khỏi đếm slHuy (xem DoiTuongHopDongLienKetSupport). */
    UUID HANG_MUC_THI_CONG_DOI_TUONG_ID = UUID.fromString("d77c2f9d-b5ca-48f6-95fe-6574c9fda5b2");

    @Query(value = """
            SELECT * FROM hop_dong
            WHERE ngay_xoa IS NULL AND hoat_dong = TRUE
            ORDER BY ma_hop_dong
            """, nativeQuery = true)
    List<HopDong> findActiveHopDong();

    /** row: [hopDongId, soVuongMacMo]. */
    @Query(value = """
            SELECT v.hop_dong_id, COUNT(*)
            FROM vuong_mac v
            WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
              AND v.hop_dong_id IS NOT NULL
              AND v.trang_thai IN ('pending', 'in_progress')
            GROUP BY v.hop_dong_id
            """, nativeQuery = true)
    List<Object[]> countOpenIssuesGroupByHopDong();

    /** row: [hopDongId, maTrangThaiUpper, soLuong] — dùng để đếm số đối tượng ở trạng thái "HUY". */
    @Query(value = """
            SELECT h.id,
                   UPPER(COALESCE(t.nhan_hien_thi, CASE WHEN d.ngay_ht_tc IS NOT NULL THEN 'HT' ELSE 'UNKNOWN' END)),
                   COUNT(*)
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            INNER JOIN doi_tuong_quan_ly dtql ON dtql.id = d.doi_tuong_quan_ly_id AND dtql.ngay_xoa IS NULL
            LEFT JOIN hop_dong_doi_tuong_trang_thai t ON t.id = d.trang_thai_hop_dong_id
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
              AND d.doi_tuong_quan_ly_id <> :hangMucId
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%boq%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hang muc%'
              AND LOWER(CONCAT(COALESCE(dtql.ten, ''), ' ', COALESCE(dtql.ma, ''))) NOT LIKE '%hạng mục%'
            GROUP BY h.id, UPPER(COALESCE(t.nhan_hien_thi, CASE WHEN d.ngay_ht_tc IS NOT NULL THEN 'HT' ELSE 'UNKNOWN' END))
            """, nativeQuery = true)
    List<Object[]> countGroupByHopDongIdAndTrangThaiMa(@Param("hangMucId") UUID hangMucId);

    /** id -> mã kiểu hợp đồng — dùng suy loaiCV (KSTK/TC/TK). */
    @Query(value = "SELECT id, ma FROM kieu_hop_dong", nativeQuery = true)
    List<Object[]> findAllKieuHopDongIdMa();

    /** id -> [ma, ten, he_nghiep_vu] — dùng suy linhVuc. */
    @Query(value = "SELECT id, ma, ten, he_nghiep_vu FROM loai_hop_dong", nativeQuery = true)
    List<Object[]> findAllLoaiHopDong();

    /** row: [hopDongId, tenThuocTinh, giaTri] cho tập hợp đồng — dùng suy trungTam/nhaThau/ngayKy/soDoiKS/soDoiTC. */
    @Query(value = """
            SELECT ht.hop_dong_id, attr.ten, ht.gia_tri
            FROM hop_dong_thuoc_tinh ht
            LEFT JOIN thuoc_tinh_hop_dong attr ON attr.id = ht.thuoc_tinh_hop_dong_id AND attr.ngay_xoa IS NULL
            WHERE ht.hop_dong_id IN :hopDongIds AND ht.ngay_xoa IS NULL
            ORDER BY ht.hop_dong_id, ht.thu_tu
            """, nativeQuery = true)
    List<Object[]> findThuocTinhForHopDongIds(@Param("hopDongIds") Collection<UUID> hopDongIds);

    @Query(value = "SELECT ho_ten FROM nguoi_dung WHERE id = :id", nativeQuery = true)
    String findTenNguoiDungById(@Param("id") UUID id);

    /**
     * 6 tổng tiền theo hợp đồng trong 1 câu (thay cho 6 câu {@code sumThanhTien*}): sản lượng mọi trạng thái và sản lượng
     * "đã thực hiện" (DT), mỗi loại cho kỳ 1, kỳ 2 và cả khoảng [dateFrom, dateTo]. Kỳ 1 và kỳ 2 nằm trong khoảng tổng.
     * row: [hopDongId, sxT1, sxT2, sxTong, dtT1, dtT2, dtTong].
     */
    @Query(value = """
            SELECT COALESCE(s.hop_dong_id, d.hop_dong_id) AS hop_dong_id,
                   COALESCE(SUM(a.thanh_tien) FILTER (WHERE s.ngay_thuc_hien BETWEEN :t1From AND :t1To), 0),
                   COALESCE(SUM(a.thanh_tien) FILTER (WHERE s.ngay_thuc_hien BETWEEN :t2From AND :t2To), 0),
                   COALESCE(SUM(a.thanh_tien), 0),
                   COALESCE(SUM(a.thanh_tien) FILTER (WHERE a.da_thuc_hien AND s.ngay_thuc_hien BETWEEN :t1From AND :t1To), 0),
                   COALESCE(SUM(a.thanh_tien) FILTER (WHERE a.da_thuc_hien AND s.ngay_thuc_hien BETWEEN :t2From AND :t2To), 0),
                   COALESCE(SUM(a.thanh_tien) FILTER (WHERE a.da_thuc_hien), 0)
            FROM san_luong s
            LEFT JOIN hop_dong_doi_tuong d ON d.id = s.hop_dong_doi_tuong_id AND d.ngay_xoa IS NULL
            CROSS JOIN LATERAL (SELECT COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0) AS thanh_tien,
                                       LOWER(COALESCE(s.trang_thai, '')) IN ('done', 'da_duyet', 'approved', 'hoan_thanh', 'da_tc') AS da_thuc_hien) a
            WHERE s.ngay_xoa IS NULL AND s.hoat_dong = TRUE
              AND s.ngay_thuc_hien IS NOT NULL
              AND s.ngay_thuc_hien >= :dateFrom AND s.ngay_thuc_hien <= :dateTo
              AND COALESCE(s.hop_dong_id, d.hop_dong_id) IS NOT NULL
            GROUP BY COALESCE(s.hop_dong_id, d.hop_dong_id)
            """, nativeQuery = true)
    List<Object[]> sumThanhTienTheoKy(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo,
                                      @Param("t1From") LocalDate t1From, @Param("t1To") LocalDate t1To,
                                      @Param("t2From") LocalDate t2From, @Param("t2To") LocalDate t2To);

    /**
     * Thuộc tính của MỌI hợp đồng đang hoạt động trong 1 câu — cùng dữ liệu với {@link #findThuocTinhForHopDongIds} nhưng không
     * phụ thuộc kết quả {@link #findActiveHopDong}, nên chạy song song được. row: [hopDongId, tenThuocTinh, giaTri].
     */
    @Query(value = """
            SELECT ht.hop_dong_id, attr.ten, ht.gia_tri
            FROM hop_dong_thuoc_tinh ht
            JOIN hop_dong h ON h.id = ht.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            LEFT JOIN thuoc_tinh_hop_dong attr ON attr.id = ht.thuoc_tinh_hop_dong_id AND attr.ngay_xoa IS NULL
            WHERE ht.ngay_xoa IS NULL
            ORDER BY ht.hop_dong_id, ht.thu_tu
            """, nativeQuery = true)
    List<Object[]> findThuocTinhHopDongActive();
}
