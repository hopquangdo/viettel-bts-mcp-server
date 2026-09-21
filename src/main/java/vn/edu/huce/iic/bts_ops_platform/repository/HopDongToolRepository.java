package vn.edu.huce.iic.bts_ops_platform.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.LichSuBuocProjection;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.PhanBoBuocProjection;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.ThongKeKhuVucProjection;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.XepHangKhuVucProjection;
import vn.edu.huce.iic.bts_ops_platform.dto.hopdong.XepHangTinhProjection;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;

import java.util.List;
import java.util.UUID;

public interface HopDongToolRepository extends JpaRepository<HopDong, UUID> {

    /**
     * Bộ lọc cấp hợp đồng (alias {@code h}) theo ID ĐÃ RESOLVE ở handler (hopDongId/khuVucId/nhaThauId/doiTuongId, null = không lọc).
     * Khu vực/nhà thầu/đối tượng lọc qua EXISTS trên hop_dong_doi_tuong, so trực tiếp cột id (không join danh mục).
     */
    String LOC_HOP_DONG = """
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (CAST(:kieuHopDongId AS uuid) IS NULL OR h.kieu_hop_dong_id = CAST(:kieuHopDongId AS uuid))
              AND (
                (CAST(:khuVucId AS uuid) IS NULL AND CAST(:tinhThanhId AS uuid) IS NULL AND CAST(:nhaThauId AS uuid) IS NULL AND CAST(:doiTuongId AS uuid) IS NULL)
                OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL
                      AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
                      AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
                      AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
                      AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
                ))
            """;

    /** id -> tên loại hợp đồng — bảng danh mục nhỏ, lấy hết 1 lần để enrich tên trong hopdong_search. */
    @Query(value = "SELECT id, ten FROM loai_hop_dong", nativeQuery = true)
    List<Object[]> findAllLoaiHopDongIdTen();

    /** id -> tên kiểu hợp đồng — bảng danh mục nhỏ, lấy hết 1 lần để enrich tên trong hopdong_search. */
    @Query(value = "SELECT id, ten FROM kieu_hop_dong", nativeQuery = true)
    List<Object[]> findAllKieuHopDongIdTen();

    /**
     * row: [hopDongId, maHopDong, ten, tongThanhTienThiCong, sanLuongDone]. Lọc theo loại/nhà thầu/mã HĐ nếu có.
     * Dùng EXISTS thay vì LEFT JOIN hop_dong_doi_tuong + DISTINCT — bản cũ nhân bản mỗi hop_dong
     * thành N dòng (1 dòng/đối tượng) rồi mới dedupe, khiến subquery SUM san_luong tương quan chạy
     * lại N lần/hợp đồng thay vì 1 lần (đo thực tế: 82ms cho 7 hợp đồng vì chạy 4775 lần thay vì 7).
     */
    @Query(value = """
            SELECT h.id, h.ma_hop_dong, h.ten, h.tong_thanh_tien_thi_cong,
                   COALESCE((SELECT SUM(COALESCE(s.don_gia,0)*COALESCE(s.khoi_luong_hoan_thanh,0))
                       FROM san_luong s WHERE s.hop_dong_id = h.id AND s.ngay_xoa IS NULL AND s.hoat_dong = TRUE AND s.trang_thai = 'done'), 0)
            FROM hop_dong h
            LEFT JOIN loai_hop_dong l ON l.id = h.loai_hop_dong_id
            WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
              AND h.tong_thanh_tien_thi_cong IS NOT NULL AND h.tong_thanh_tien_thi_cong > 0
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (CAST(:kieuHopDongId AS uuid) IS NULL OR h.kieu_hop_dong_id = CAST(:kieuHopDongId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
              AND ((CAST(:nhaThauId AS uuid) IS NULL AND CAST(:tinhThanhId AS uuid) IS NULL) OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL
                      AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
                      AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))))
            """, nativeQuery = true)
    List<Object[]> tienDoTatCaHopDong(@Param("loaiHopDongId") UUID loaiHopDongId, @Param("kieuHopDongId") UUID kieuHopDongId,
                                       @Param("hopDongId") UUID hopDongId,
                                       @Param("nhaThauId") UUID nhaThauId,
                                       @Param("tinhThanhId") UUID tinhThanhId);

    /** Tìm hợp đồng theo từ khoá (đã dạng %lower%) + nhà thầu/đối tượng đã resolve. EXISTS thay cho join nhân bản dòng nên không cần DISTINCT. */
    @Query(value = """
            SELECT h.* FROM hop_dong h
            LEFT JOIN loai_hop_dong l ON l.id = h.loai_hop_dong_id
            WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
              AND (CAST(:keyword AS text) IS NULL
                   OR LOWER(h.ma_hop_dong) LIKE :keyword ESCAPE '\\' OR LOWER(COALESCE(h.ten, '')) LIKE :keyword ESCAPE '\\')
              AND (CAST(:fromDate AS date) IS NULL OR (CASE WHEN CAST(:loaiNgay AS text) = 'han_hop_dong' THEN h.han_hop_dong ELSE h.ngay_thuc_hien END) >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL OR (CASE WHEN CAST(:loaiNgay AS text) = 'han_hop_dong' THEN h.han_hop_dong ELSE h.ngay_thuc_hien END) <= CAST(:toDate AS date))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (CAST(:kieuHopDongId AS uuid) IS NULL OR h.kieu_hop_dong_id = CAST(:kieuHopDongId AS uuid))
              AND ((CAST(:nhaThauId AS uuid) IS NULL AND CAST(:doiTuongId AS uuid) IS NULL AND CAST(:tinhThanhId AS uuid) IS NULL)
                   OR EXISTS (SELECT 1 FROM hop_dong_doi_tuong d
                              WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL
                                AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
                                AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
                                AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))))
            ORDER BY h.ngay_tao DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM hop_dong h
            LEFT JOIN loai_hop_dong l ON l.id = h.loai_hop_dong_id
            WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
              AND (CAST(:keyword AS text) IS NULL
                   OR LOWER(h.ma_hop_dong) LIKE :keyword ESCAPE '\\' OR LOWER(COALESCE(h.ten, '')) LIKE :keyword ESCAPE '\\')
              AND (CAST(:fromDate AS date) IS NULL OR (CASE WHEN CAST(:loaiNgay AS text) = 'han_hop_dong' THEN h.han_hop_dong ELSE h.ngay_thuc_hien END) >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL OR (CASE WHEN CAST(:loaiNgay AS text) = 'han_hop_dong' THEN h.han_hop_dong ELSE h.ngay_thuc_hien END) <= CAST(:toDate AS date))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (CAST(:kieuHopDongId AS uuid) IS NULL OR h.kieu_hop_dong_id = CAST(:kieuHopDongId AS uuid))
              AND ((CAST(:nhaThauId AS uuid) IS NULL AND CAST(:doiTuongId AS uuid) IS NULL AND CAST(:tinhThanhId AS uuid) IS NULL)
                   OR EXISTS (SELECT 1 FROM hop_dong_doi_tuong d
                              WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL
                                AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
                                AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
                                AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))))
            """,
            nativeQuery = true)
    Page<HopDong> search(@Param("keyword") String keyword, @Param("loaiHopDongId") UUID loaiHopDongId, @Param("kieuHopDongId") UUID kieuHopDongId,
                          @Param("nhaThauId") UUID nhaThauId, @Param("doiTuongId") UUID doiTuongId,
                          @Param("tinhThanhId") UUID tinhThanhId,
                          @Param("fromDate") java.time.LocalDate fromDate, @Param("toDate") java.time.LocalDate toDate,
                          @Param("loaiNgay") String loaiNgay, Pageable pageable);

    /** Số đối tượng hoàn thành thi công (ngay_ht_tc) trong khoảng [fromDate, toDate] theo bộ lọc hiện tại. */
    @Query(value = """
            SELECT COUNT(DISTINCT d.id)
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND d.ngay_ht_tc IS NOT NULL
              AND (CAST(:fromDate AS date) IS NULL OR CAST(d.ngay_ht_tc AS date) >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL OR CAST(d.ngay_ht_tc AS date) <= CAST(:toDate AS date))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (CAST(:kieuHopDongId AS uuid) IS NULL OR h.kieu_hop_dong_id = CAST(:kieuHopDongId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
            """, nativeQuery = true)
    long countDoiTuongHoanThanhTrongKy(@Param("fromDate") java.time.LocalDate fromDate, @Param("toDate") java.time.LocalDate toDate,
                                       @Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId,
                                       @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
                                       @Param("loaiHopDongId") UUID loaiHopDongId, @Param("kieuHopDongId") UUID kieuHopDongId,
                                       @Param("doiTuongId") UUID doiTuongId);


    @Query(value = """
            SELECT COUNT(*)::int AS tong,
                   COUNT(*) FILTER (WHERE d.ngay_ht_tc IS NOT NULL)::int AS hoanThanh,
                   COUNT(*) FILTER (WHERE d.co_vuong_mac_mo = TRUE)::int AS coVuongMacMo
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            LEFT JOIN loai_hop_dong l ON l.id = h.loai_hop_dong_id
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (CAST(:kieuHopDongId AS uuid) IS NULL OR h.kieu_hop_dong_id = CAST(:kieuHopDongId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
            """, nativeQuery = true)
    ThongKeKhuVucProjection thongKeTheoKhuVuc(@Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId, @Param("kieuHopDongId") UUID kieuHopDongId,
                                               @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId);

    /** Xếp hạng khu vực theo số hợp đồng/đối tượng đang hoạt động trong phạm vi lọc hiện tại. */
    @Query(value = """
            SELECT COALESCE(kv.ten, '—') AS khuVuc,
                   COUNT(DISTINCT h.id) AS soHopDong,
                   COUNT(DISTINCT d.id) AS soDoiTuong,
                   COUNT(DISTINCT d.id) FILTER (WHERE d.co_vuong_mac_mo = TRUE) AS soVuongMac
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            LEFT JOIN khu_vuc kv ON kv.id = d.khu_vuc_id
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (CAST(:kieuHopDongId AS uuid) IS NULL OR h.kieu_hop_dong_id = CAST(:kieuHopDongId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
            GROUP BY kv.ten
            ORDER BY soHopDong DESC, khuVuc
            LIMIT :top
            """, nativeQuery = true)
    List<XepHangKhuVucProjection> rankKhuVuc(@Param("top") int top,
                                            @Param("hopDongId") UUID hopDongId,
                                            @Param("tinhThanhId") UUID tinhThanhId,
                                            @Param("nhaThauId") UUID nhaThauId,
                                            @Param("doiTuongId") UUID doiTuongId,
                                            @Param("loaiHopDongId") UUID loaiHopDongId,
                                            @Param("kieuHopDongId") UUID kieuHopDongId);

    /** Xếp hạng tỉnh trong phạm vi 1 hợp đồng cụ thể. */
    @Query(value = """
            SELECT COALESCE(tt.ten, '—') AS tinh,
                   COUNT(DISTINCT d.id) AS soDoiTuong,
                   COUNT(*) FILTER (WHERE d.ngay_ht_tc IS NOT NULL) AS soHoanThanh,
                   COUNT(DISTINCT d.id) FILTER (WHERE d.co_vuong_mac_mo = TRUE) AS soVuongMac
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.id = CAST(:hopDongId AS uuid) AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            LEFT JOIN tinh_thanh tt ON tt.id = d.tinh_thanh_id
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
            GROUP BY tt.ten
            ORDER BY soDoiTuong DESC, tinh
            """, nativeQuery = true)
    List<XepHangTinhProjection> rankTinhTheoHopDong(@Param("hopDongId") UUID hopDongId);

    /** Phân bố đối tượng theo bước trạng thái, trong phạm vi 1 hợp đồng cụ thể. */
    @Query(value = """
            SELECT COALESCE(s.nhan_hien_thi, '— Chưa xác định bước —') AS tenBuoc,
                   COALESCE(s.thu_tu, 0) AS thuTu,
                   COUNT(d.id) AS soDoiTuong
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.id = CAST(:hopDongId AS uuid) AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            LEFT JOIN hop_dong_doi_tuong_trang_thai s ON s.id = d.trang_thai_hop_dong_id
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
            GROUP BY s.nhan_hien_thi, s.thu_tu
            ORDER BY thuTu
            """, nativeQuery = true)
    List<PhanBoBuocProjection> phanBoBuoc(@Param("hopDongId") UUID hopDongId);

    /** Lịch sử chuyển bước từ audit_log cho 1 hợp đồng, phân trang qua Pageable (LIMIT/OFFSET ở DB). */
    @Query(value = """
            SELECT a.ngay_tao AS ngay, a.hanh_dong AS hanhDong, a.mo_ta AS moTa, a.ten_nguoi_thuc_hien AS nguoiThucHien
            FROM audit_log a
            WHERE a.hop_dong_id = :hopDongId
            ORDER BY a.ngay_tao DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM audit_log a WHERE a.hop_dong_id = :hopDongId
            """,
            nativeQuery = true)
    Page<LichSuBuocProjection> lichSuBuoc(@Param("hopDongId") UUID hopDongId, Pageable pageable);

    /**
     * Số liệu từng hợp đồng cho khối tổng quan — bám {@code HopDongServiceImpl.thongKeTatCa} (FE dùng).
     * row: [hop_dong_id, trang_thai_thi_cong (mặc định CHUA_TC), số đối tượng, số vướng mắc đang mở].
     * Số đối tượng: đối tượng đang hoạt động, bỏ "Hạng mục thi công"/BOQ (khớp REST countGroupByHopDongId),
     * chỉ tính đối tượng khớp bộ lọc khuVuc/nhaThau/doiTuong nếu có. Vướng mắc mở: bản ghi pending/in_progress.
     */
    @Query(value = """
            SELECT h.id, COALESCE(h.trang_thai_thi_cong, 'CHUA_TC') AS trang_thai_thi_cong,
                   COALESCE(oc.so_doi_tuong, 0) AS so_doi_tuong, COALESCE(vm.so_vuong_mac, 0) AS so_vuong_mac
            FROM hop_dong h
            LEFT JOIN (
                SELECT d.hop_dong_id AS hop_dong_id, COUNT(*) AS so_doi_tuong
                FROM hop_dong_doi_tuong d
                JOIN hop_dong hx ON hx.id = d.hop_dong_id AND hx.ngay_xoa IS NULL AND hx.hoat_dong = TRUE
                JOIN doi_tuong_quan_ly q ON q.id = d.doi_tuong_quan_ly_id AND q.ngay_xoa IS NULL
                WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
                  AND d.doi_tuong_quan_ly_id <> CAST('d77c2f9d-b5ca-48f6-95fe-6574c9fda5b2' AS uuid)
                  AND LOWER(CONCAT(COALESCE(q.ten, ''), ' ', COALESCE(q.ma, ''))) NOT LIKE '%boq%'
                  AND LOWER(CONCAT(COALESCE(q.ten, ''), ' ', COALESCE(q.ma, ''))) NOT LIKE '%hang muc%'
                  AND LOWER(CONCAT(COALESCE(q.ten, ''), ' ', COALESCE(q.ma, ''))) NOT LIKE '%hạng mục%'
                  AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
                  AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
                  AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
                  AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
                GROUP BY d.hop_dong_id
            ) oc ON oc.hop_dong_id = h.id
            LEFT JOIN (
                SELECT v.hop_dong_id AS hop_dong_id, COUNT(*) AS so_vuong_mac
                FROM vuong_mac v
                WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
                  AND v.trang_thai IN ('pending', 'in_progress') AND v.hop_dong_id IS NOT NULL
                GROUP BY v.hop_dong_id
            ) vm ON vm.hop_dong_id = h.id
            WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (CAST(:kieuHopDongId AS uuid) IS NULL OR h.kieu_hop_dong_id = CAST(:kieuHopDongId AS uuid))
              AND (
                (CAST(:khuVucId AS uuid) IS NULL AND CAST(:tinhThanhId AS uuid) IS NULL AND CAST(:nhaThauId AS uuid) IS NULL AND CAST(:doiTuongId AS uuid) IS NULL)
                OR COALESCE(oc.so_doi_tuong, 0) > 0)
            """, nativeQuery = true)
    List<Object[]> thongKeTheoHopDong(@Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId, @Param("nhaThauId") UUID nhaThauId,
                                       @Param("hopDongId") UUID hopDongId, @Param("doiTuongId") UUID doiTuongId,
            @Param("loaiHopDongId") UUID loaiHopDongId, @Param("kieuHopDongId") UUID kieuHopDongId);

    /**
     * Số đối tượng theo trạng thái của từng hợp đồng — nguồn tính tỷ lệ hoàn thành (khớp REST
     * countGroupByHopDongIdAndTrangThaiMa). row: [hop_dong_id, mã trạng thái (UPPER), số đối tượng].
     */
    @Query(value = """
            SELECT d.hop_dong_id,
                   UPPER(COALESCE(t.ma, t.ten, CASE WHEN d.ngay_ht_tc IS NOT NULL THEN 'HT' ELSE 'UNKNOWN' END)) AS ma_trang_thai,
                   COUNT(*) AS so_doi_tuong
            FROM hop_dong_doi_tuong d
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            JOIN doi_tuong_quan_ly q ON q.id = d.doi_tuong_quan_ly_id AND q.ngay_xoa IS NULL
            LEFT JOIN trang_thai_hop_dong t ON t.id = d.trang_thai_hop_dong_id
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
              AND d.doi_tuong_quan_ly_id <> CAST('d77c2f9d-b5ca-48f6-95fe-6574c9fda5b2' AS uuid)
              AND LOWER(CONCAT(COALESCE(q.ten, ''), ' ', COALESCE(q.ma, ''))) NOT LIKE '%boq%'
              AND LOWER(CONCAT(COALESCE(q.ten, ''), ' ', COALESCE(q.ma, ''))) NOT LIKE '%hang muc%'
              AND LOWER(CONCAT(COALESCE(q.ten, ''), ' ', COALESCE(q.ma, ''))) NOT LIKE '%hạng mục%'
              AND (CAST(:hopDongId AS uuid) IS NULL OR h.id = CAST(:hopDongId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid))
              AND (CAST(:kieuHopDongId AS uuid) IS NULL OR h.kieu_hop_dong_id = CAST(:kieuHopDongId AS uuid))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:doiTuongId AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:doiTuongId AS uuid) OR d.id = CAST(:doiTuongId AS uuid)))
            GROUP BY d.hop_dong_id, 2
            """, nativeQuery = true)
    List<Object[]> thongKeTrangThaiDoiTuongTheoHopDong(@Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId, @Param("nhaThauId") UUID nhaThauId,
                                                        @Param("hopDongId") UUID hopDongId, @Param("doiTuongId") UUID doiTuongId,
            @Param("loaiHopDongId") UUID loaiHopDongId, @Param("kieuHopDongId") UUID kieuHopDongId);

    /**
     * 3 nhóm đếm hợp đồng trong 1 câu (thay cho 3 câu đếm riêng). row: [loại nhóm ('T' trạng thái thi công | 'K' kiểu | 'L' loại), a, b, c, d, số hợp đồng];
     * T: a = trạng thái; K: a = id kiểu, b = mã, c = tên, d = nhóm; L: a = tên loại.
     */
    @Query(value = """
            SELECT 'T' AS kind, h.trang_thai_thi_cong AS a, CAST(NULL AS text) AS b, CAST(NULL AS text) AS c, CAST(NULL AS text) AS d,
                   COUNT(DISTINCT h.id) AS cnt
            FROM hop_dong h
            WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            """ + LOC_HOP_DONG + """
            GROUP BY h.trang_thai_thi_cong
            UNION ALL
            SELECT 'K', CAST(k.id AS text), k.ma, k.ten, CAST(k.nhom AS text), COUNT(DISTINCT h.id)
            FROM hop_dong h INNER JOIN kieu_hop_dong k ON k.id = h.kieu_hop_dong_id
            WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            """ + LOC_HOP_DONG + """
            GROUP BY k.id, k.ma, k.ten, k.nhom
            UNION ALL
            SELECT 'L', COALESCE(l.ten, '—'), CAST(NULL AS text), CAST(NULL AS text), CAST(NULL AS text), COUNT(DISTINCT h.id)
            FROM hop_dong h LEFT JOIN loai_hop_dong l ON l.id = h.loai_hop_dong_id
            WHERE h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            """ + LOC_HOP_DONG + """
            GROUP BY l.ten
            """, nativeQuery = true)
    List<Object[]> countTheoNhomFiltered(@Param("khuVucId") UUID khuVucId, @Param("tinhThanhId") UUID tinhThanhId, @Param("nhaThauId") UUID nhaThauId,
                                         @Param("hopDongId") UUID hopDongId, @Param("doiTuongId") UUID doiTuongId,
            @Param("loaiHopDongId") UUID loaiHopDongId, @Param("kieuHopDongId") UUID kieuHopDongId);
}
