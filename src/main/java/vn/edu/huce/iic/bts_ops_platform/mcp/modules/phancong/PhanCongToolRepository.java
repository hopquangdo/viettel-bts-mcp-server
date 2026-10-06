package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.ChuaPhanCongRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.NguoiDungRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.TongQuanDoiTuongRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.XepHangNhaThauRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.doituong.HopDongDoiTuong;

import java.util.List;
import java.util.UUID;

/**
 * Repo tool-riêng cho phancong_tool, đọc từ hop_dong_doi_tuong — nguồn chân lý phân công nhà thầu
 * ({@code nha_thau_id}), thay cho việc suy luận qua bảng phan_cong.
 */
public interface PhanCongToolRepository extends JpaRepository<HopDongDoiTuong, UUID> {

    /** Đối tượng chưa phân công nhà thầu (nha_thau_id IS NULL). Lọc theo hopDongId / khuVucId (đã resolve) nếu truyền. */
    @Query(value = """
            SELECT dt.ma AS maDoiTuong, COALESCE(dt.ten, dt.ma) AS tenDoiTuong,
                   h.ma_hop_dong AS maHopDong, h.ten AS tenHopDong,
                   kv.ten AS khuVuc, tt.ten AS tinh
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id AND dt.ngay_xoa IS NULL
            LEFT JOIN khu_vuc kv ON kv.id = d.khu_vuc_id
            LEFT JOIN tinh_thanh tt ON tt.id = d.tinh_thanh_id
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND d.nha_thau_id IS NULL
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
                ORDER BY h.ma_hop_dong, dt.ma
                """,
                countQuery = """
                SELECT COUNT(*)
                FROM hop_dong_doi_tuong d
                INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND d.nha_thau_id IS NULL
                  AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
                  AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
                  AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
                """, nativeQuery = true)
            Page<ChuaPhanCongRow> findChuaPhanCong(@Param("hopDongId") UUID hopDongId, @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId,
                               @Param("doiTuongId") UUID doiTuongId,
                               Pageable pageable);

    @Query(value = """
            SELECT DISTINCT du_lieu_doi_tuong_id FROM vuong_mac
            WHERE ngay_xoa IS NULL AND hoat_dong = TRUE
              AND du_lieu_doi_tuong_id IN (:ids) AND trang_thai IN ('pending', 'in_progress')
            """, nativeQuery = true)
    List<UUID> findDoiTuongIdsCoVuongDangMo(@Param("ids") List<UUID> ids);

    @Query(value = "SELECT id, ho_ten, ten_dang_nhap FROM nguoi_dung WHERE id = :id AND ngay_xoa IS NULL", nativeQuery = true)
    NguoiDungRow findNguoiDungById(@Param("id") UUID id);

    @Query(value = """
            SELECT id, ho_ten, ten_dang_nhap FROM nguoi_dung
            WHERE ngay_xoa IS NULL AND LOWER(ho_ten) LIKE LOWER(CONCAT('%', :ten, '%'))
            ORDER BY ho_ten LIMIT 1
            """, nativeQuery = true)
    NguoiDungRow findNguoiDungByTen(@Param("ten") String ten);

    /** Xếp hạng nhà thầu theo số đối tượng phụ trách, kèm số đang vướng mắc và tổng sản lượng hiệu lực. */
    @Query(value = """
            SELECT COALESCE(nd.ho_ten, '—') AS tenNhaThau,
                   COUNT(*) AS soDoiTuong,
                   COUNT(*) FILTER (WHERE d.co_vuong_mac_mo) AS soDangVuongMac,
                   COALESCE(SUM(d.san_luong_hieu_luc), 0) AS tongSanLuongHieuLuc
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            LEFT JOIN nguoi_dung nd ON nd.id = d.nha_thau_id
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND d.nha_thau_id IS NOT NULL
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
              AND ((CAST(:giaiDoan AS text) IS NULL AND CAST(:vung AS text) IS NULL)
                   OR EXISTS (
                        SELECT 1 FROM phan_cong p
                        WHERE p.ngay_xoa IS NULL AND p.hoat_dong = TRUE
                          AND p.hop_dong_doi_tuong_id = d.id
                          AND (CAST(:giaiDoan AS text) IS NULL OR LOWER(p.giai_doan) = LOWER(CAST(:giaiDoan AS text)))
                          AND (CAST(:vung AS text) IS NULL OR LOWER(p.ma_vung) = LOWER(CAST(:vung AS text)))
                   ))
            GROUP BY d.nha_thau_id, nd.ho_ten
            ORDER BY COUNT(*) DESC, nd.ho_ten
            """, nativeQuery = true)
    List<XepHangNhaThauRow> rankNhaThau(Pageable pageable, @Param("hopDongId") UUID hopDongId,
                                       @Param("khuVucId") UUID khuVucId,
                                       @Param("tinhThanhId") UUID tinhThanhId,
                                       @Param("doiTuongId") UUID doiTuongId,
                                       @Param("giaiDoan") String giaiDoan,
                                       @Param("vung") String vung);

    /** id -> mã + tên hợp đồng của đúng các hợp đồng đang cần enrich (không đọc cả bảng). */
    @Query(value = "SELECT id, ma_hop_dong, ten FROM hop_dong WHERE id IN (:ids)", nativeQuery = true)
    List<Object[]> findHopDongIdMaTenByIds(@Param("ids") java.util.Collection<UUID> ids);

    /** id -> tên khu vực — bảng danh mục nhỏ, dùng để enrich PhanCongToolItem.khuVucId. */
    @Query(value = "SELECT id, ten FROM khu_vuc", nativeQuery = true)
    List<Object[]> findAllKhuVucIdTen();

    /** id -> tên tỉnh thành — bảng danh mục nhỏ, dùng để enrich PhanCongToolItem.tinhThanhId. */
    @Query(value = "SELECT id, ten FROM tinh_thanh", nativeQuery = true)
    List<Object[]> findAllTinhThanhIdTen();

    /**
     * Thống kê tổng quan theo đối tượng (nguồn chân lý hop_dong_doi_tuong) - bám REST
     * PhanCongDashboardQueryRepository.aggregateTongQuan: tổng trạm, đã phân nhà thầu, số nhà thầu,
     * hoàn thành (có ngay_ht_tc), vướng mắc mở, tồn (hoàn thành, không vướng, chưa quyết toán).
     */
    @Query(value = """
            SELECT COUNT(*) AS tongTram,
                   COUNT(*) FILTER (WHERE d.nha_thau_id IS NOT NULL) AS daPhanNhaThau,
                   COUNT(DISTINCT d.nha_thau_id) AS soNhaThau,
                   COUNT(*) FILTER (WHERE d.ngay_ht_tc IS NOT NULL) AS hoanThanh,
                   COUNT(*) FILTER (WHERE COALESCE(d.co_vuong_mac_mo, FALSE) IS TRUE) AS vuongMac,
                   COUNT(*) FILTER (WHERE d.ngay_ht_tc IS NOT NULL
                                      AND COALESCE(d.co_vuong_mac_mo, FALSE) IS NOT TRUE
                                      AND d.quyet_toan_thuc IS NULL) AS ton
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
            """, nativeQuery = true)
    TongQuanDoiTuongRow tongQuanTheoDoiTuong(@Param("hopDongId") UUID hopDongId, @Param("nhaThauId") UUID nhaThauId,
                                              @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId,
                                              @Param("doiTuongId") UUID doiTuongId);


    /**
     * Tỷ lệ hủy trạm theo nhà thầu/cán bộ được giao (hop_dong_doi_tuong.nha_thau_id) — cùng câu với business-api
     * TyLeHuyTramStatsRepository.thongKeTheoCanBo (cảnh báo "Tỷ lệ hủy trạm vượt ngưỡng — theo cán bộ" trên Dashboard).
     * Trạm hủy: có lý do hủy hoặc trạng thái hợp đồng của đối tượng chứa "HUY"/"HỦY". row: [ten, tongTram, soTramHuy].
     */
    @Query(value = """
            SELECT COALESCE(nd.ho_ten, 'Chưa gán cán bộ'), COUNT(*),
                   COUNT(*) FILTER (WHERE dt.ly_do_huy IS NOT NULL
                                    OR UPPER(COALESCE(tt.ma, '')) LIKE '%HUY%'
                                    OR UPPER(COALESCE(tt.ten, '')) LIKE '%HỦY%'
                                    OR UPPER(COALESCE(tt.ten, '')) LIKE '%HUY%')
            FROM hop_dong_doi_tuong dt
            LEFT JOIN trang_thai_hop_dong tt ON tt.id = dt.trang_thai_hop_dong_id
            LEFT JOIN nguoi_dung nd ON nd.id = dt.nha_thau_id AND nd.ngay_xoa IS NULL
            WHERE dt.ngay_xoa IS NULL AND dt.hoat_dong = TRUE
            GROUP BY dt.nha_thau_id, nd.ho_ten
            ORDER BY 3 DESC, 2 DESC
            """, nativeQuery = true)
    List<Object[]> tyLeHuyTheoNhaThau();

    /** Như {@link #tyLeHuyTheoNhaThau} nhưng nhóm theo khu vực của đối tượng (cảnh báo "theo khu vực"). row: [ten, tongTram, soTramHuy]. */
    @Query(value = """
            SELECT COALESCE(kv.ten, 'Chưa gán khu vực'), COUNT(*),
                   COUNT(*) FILTER (WHERE dt.ly_do_huy IS NOT NULL
                                    OR UPPER(COALESCE(tt.ma, '')) LIKE '%HUY%'
                                    OR UPPER(COALESCE(tt.ten, '')) LIKE '%HỦY%'
                                    OR UPPER(COALESCE(tt.ten, '')) LIKE '%HUY%')
            FROM hop_dong_doi_tuong dt
            LEFT JOIN trang_thai_hop_dong tt ON tt.id = dt.trang_thai_hop_dong_id
            LEFT JOIN khu_vuc kv ON kv.id = dt.khu_vuc_id AND kv.ngay_xoa IS NULL
            WHERE dt.ngay_xoa IS NULL AND dt.hoat_dong = TRUE
            GROUP BY dt.khu_vuc_id, kv.ten
            ORDER BY 3 DESC, 2 DESC
            """, nativeQuery = true)
    List<Object[]> tyLeHuyTheoKhuVuc();

    /** Ngưỡng % cảnh báo tỷ lệ hủy trạm đang cấu hình (null = chưa cấu hình, dùng mặc định 15). */
    @Query(value = "SELECT gia_tri_nguong FROM canh_bao_nguong_cau_hinh WHERE loai = 'TY_LE_HUY_TRAM' AND hoat_dong = TRUE LIMIT 1",
            nativeQuery = true)
    java.math.BigDecimal nguongTyLeHuy();

    /** Số khu vực đang hoạt động (ô "Khu vực" trên Dashboard, trước khi áp phạm vi dữ liệu người dùng). */
    @Query(value = "SELECT COUNT(*) FROM khu_vuc WHERE ngay_xoa IS NULL AND hoat_dong = TRUE", nativeQuery = true)
    long countKhuVucHoatDong();
}
