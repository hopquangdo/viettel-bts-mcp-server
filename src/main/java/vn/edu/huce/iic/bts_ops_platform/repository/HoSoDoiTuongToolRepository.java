package vn.edu.huce.iic.bts_ops_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.dto.hosodoituong.ChuaKhaoSatProjection;
import vn.edu.huce.iic.bts_ops_platform.dto.hosodoituong.KhaoSatXongChuaCoSanLuongProjection;
import vn.edu.huce.iic.bts_ops_platform.dto.hosodoituong.ThuocTinhProjection;
import vn.edu.huce.iic.bts_ops_platform.dto.hosodoituong.TongQuanProjection;
import vn.edu.huce.iic.bts_ops_platform.entity.doituong.HopDongDoiTuong;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Repository cho hosodoituong_tool — chỉ dùng các cột THỰC CÓ trên hop_dong_doi_tuong
 * (ngay_ban_giao_mat_bang, trang_thai_vat_tu_a/b, ngay_*_vat_tu_b, san_luong_hieu_luc). KHÔNG có
 * loại cột / chiều cao / ảnh thi công / ngày khảo sát trong DB.
 */
public interface HoSoDoiTuongToolRepository extends JpaRepository<HopDongDoiTuong, UUID> {

    @Query(value = """
            SELECT COUNT(*) AS tongDoiTuong,
                   COUNT(*) FILTER (WHERE d.ngay_ban_giao_mat_bang IS NULL) AS thieuBanGiaoMatBang,
                   COUNT(*) FILTER (WHERE d.trang_thai_vat_tu_a IS DISTINCT FROM 'DA_DAM_BAO') AS vatTuAChuaDamBao,
                   COUNT(*) FILTER (WHERE d.ngay_hoan_thanh_vat_tu_b IS NULL) AS vatTuBChuaHoanThanh
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:trangThaiIds AS text) IS NULL OR d.trang_thai_hop_dong_id = ANY(CAST(string_to_array(:trangThaiIds, ',') AS uuid[])))
              AND (CAST(:coNhomUuTien AS boolean) IS NULL OR (d.hop_dong_nhom_uu_tien_id IS NOT NULL) = CAST(:coNhomUuTien AS boolean))
            """, nativeQuery = true)
    TongQuanProjection tongQuan(@Param("hopDongId") UUID hopDongId, @Param("nhaThauId") UUID nhaThauId, @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId,
            @Param("trangThaiIds") String trangThaiIds, @Param("coNhomUuTien") Boolean coNhomUuTien);

    @Query(value = """
            SELECT dt.ma AS maDoiTuong, dt.ten AS tenDoiTuong, h.ma_hop_dong AS maHopDong,
                   COALESCE(h.ten, h.ma_hop_dong) AS tenHopDong,
                   d.ngay_ban_giao_mat_bang AS ngayBanGiaoMatBang,
                   d.trang_thai_vat_tu_a AS trangThaiVatTuA, d.trang_thai_vat_tu_b AS trangThaiVatTuB,
                   d.ngay_yeu_cau_vat_tu_b AS ngayYeuCauVatTuB, d.ngay_hoan_thanh_vat_tu_b AS ngayHoanThanhVatTuB
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id AND dt.ngay_xoa IS NULL
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
            ORDER BY d.ngay_tao DESC
            LIMIT 1
            """, nativeQuery = true)
    ThuocTinhProjection thuocTinh(@Param("doiTuongId") UUID doiTuongId, @Param("hopDongId") UUID hopDongId);

    @Query(value = """
            SELECT dt.ma AS maDoiTuong, dt.ten AS tenDoiTuong, kv.ten AS khuVuc, nd.ho_ten AS nhaThau
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id AND dt.ngay_xoa IS NULL
            LEFT JOIN khu_vuc kv ON kv.id = d.khu_vuc_id
            LEFT JOIN nguoi_dung nd ON nd.id = d.nha_thau_id
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND d.ngay_ban_giao_mat_bang IS NULL
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:trangThaiIds AS text) IS NULL OR d.trang_thai_hop_dong_id = ANY(CAST(string_to_array(:trangThaiIds, ',') AS uuid[])))
              AND (CAST(:coNhomUuTien AS boolean) IS NULL OR (d.hop_dong_nhom_uu_tien_id IS NOT NULL) = CAST(:coNhomUuTien AS boolean))
                ORDER BY dt.ma
                """,
                countQuery = """
                SELECT COUNT(*)
                FROM hop_dong_doi_tuong d
                INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
                  AND d.ngay_ban_giao_mat_bang IS NULL
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:trangThaiIds AS text) IS NULL OR d.trang_thai_hop_dong_id = ANY(CAST(string_to_array(:trangThaiIds, ',') AS uuid[])))
              AND (CAST(:coNhomUuTien AS boolean) IS NULL OR (d.hop_dong_nhom_uu_tien_id IS NOT NULL) = CAST(:coNhomUuTien AS boolean))
                """, nativeQuery = true)
            Page<ChuaKhaoSatProjection> chuaKhaoSat(@Param("hopDongId") UUID hopDongId, @Param("nhaThauId") UUID nhaThauId, @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId,
            @Param("trangThaiIds") String trangThaiIds, @Param("coNhomUuTien") Boolean coNhomUuTien, Pageable pageable);

    @Query(value = """
            SELECT dt.ma AS maDoiTuong, dt.ten AS tenDoiTuong, h.ma_hop_dong AS maHopDong,
                   d.ngay_ban_giao_mat_bang AS ngayBanGiaoMatBang
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id AND dt.ngay_xoa IS NULL
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
              AND d.ngay_ban_giao_mat_bang IS NOT NULL
              AND (CAST(:fromDate AS date) IS NULL OR d.ngay_ban_giao_mat_bang >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL OR d.ngay_ban_giao_mat_bang <= CAST(:toDate AS date))
              AND (d.san_luong_hieu_luc IS NULL OR d.san_luong_hieu_luc = 0)
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:trangThaiIds AS text) IS NULL OR d.trang_thai_hop_dong_id = ANY(CAST(string_to_array(:trangThaiIds, ',') AS uuid[])))
              AND (CAST(:coNhomUuTien AS boolean) IS NULL OR (d.hop_dong_nhom_uu_tien_id IS NOT NULL) = CAST(:coNhomUuTien AS boolean))
                        ORDER BY d.ngay_ban_giao_mat_bang
                        """,
                        countQuery = """
                        SELECT COUNT(*)
                        FROM hop_dong_doi_tuong d
                        INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                        WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
                            AND d.ngay_ban_giao_mat_bang IS NOT NULL
              AND (CAST(:fromDate AS date) IS NULL OR d.ngay_ban_giao_mat_bang >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL OR d.ngay_ban_giao_mat_bang <= CAST(:toDate AS date))
                            AND (d.san_luong_hieu_luc IS NULL OR d.san_luong_hieu_luc = 0)
                            AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:trangThaiIds AS text) IS NULL OR d.trang_thai_hop_dong_id = ANY(CAST(string_to_array(:trangThaiIds, ',') AS uuid[])))
              AND (CAST(:coNhomUuTien AS boolean) IS NULL OR (d.hop_dong_nhom_uu_tien_id IS NOT NULL) = CAST(:coNhomUuTien AS boolean))
                        """, nativeQuery = true)
        Page<KhaoSatXongChuaCoSanLuongProjection> khaoSatXongChuaCoSanLuong(@Param("hopDongId") UUID hopDongId, @Param("nhaThauId") UUID nhaThauId, @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId,
            @Param("trangThaiIds") String trangThaiIds, @Param("coNhomUuTien") Boolean coNhomUuTien,
            @Param("fromDate") java.time.LocalDate fromDate, @Param("toDate") java.time.LocalDate toDate, Pageable pageable);

}
