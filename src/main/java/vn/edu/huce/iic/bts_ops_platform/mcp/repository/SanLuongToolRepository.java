package vn.edu.huce.iic.bts_ops_platform.mcp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.sanluong.SanLuong;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SanLuongToolRepository extends JpaRepository<SanLuong, UUID> {

    /**
     * Tổng hợp sản lượng trong khoảng ngày — viết lại để khớp cấu trúc/ngữ nghĩa với REST
     * {@code SanLuongRepository.aggregateTongHop} (dùng bởi {@code SanLuongServiceImpl.computeTongHop},
     * cùng field totalDisplayed/withOutput/periodTotal/todayTotal đổ vào SanLuongTongHopResponse):
     * <ul>
     *   <li><b>totalDisplayed</b>: REST đếm SỐ ĐỐI TƯỢNG CÓ ÍT NHẤT 1 bản ghi san_luong (bất kỳ
     *   trạng_thái) khớp filter trong kỳ (group theo hop_dong_doi_tuong_id trên tập đã lọc theo
     *   ngày) — KHÔNG PHẢI tổng số đối tượng đang hoạt động như bản cũ (COUNT(DISTINCT d.id) trên
     *   toàn bộ hop_dong_doi_tuong, kể cả đối tượng chưa từng có sản lượng) — bản cũ làm
     *   totalDisplayed bị thổi phồng so với REST.</li>
     *   <li><b>todayTotal</b>: REST tính theo bản ghi 'done' có (ngay_cap_nhat theo giờ VN = hôm
     *   nay) HOẶC (ngay_thuc_hien = hôm nay) — bản cũ chỉ xét ngay_thuc_hien = hôm nay, bỏ sót các
     *   bản ghi vừa cập nhật hôm nay cho 1 ngày thi công trong quá khứ.</li>
     *   <li>Bổ sung {@code s.hoat_dong = TRUE} (REST luôn lọc cứng điều kiện này trên san_luong,
     *   không chỉ ngay_xoa IS NULL) — bản cũ thiếu điều kiện này.</li>
     * </ul>
     */
    @Query(value = """
            WITH sl_filtered AS (
                SELECT s.hop_dong_doi_tuong_id, s.trang_thai, s.don_gia, s.khoi_luong_hoan_thanh,
                       s.ngay_thuc_hien, s.ngay_cap_nhat
                FROM san_luong s
                INNER JOIN hop_dong_doi_tuong d ON d.id = s.hop_dong_doi_tuong_id AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
                INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                WHERE s.ngay_xoa IS NULL AND s.hoat_dong = TRUE
                  AND (s.ngay_thuc_hien BETWEEN :fromDate AND :toDate OR s.ngay_thuc_hien BETWEEN :prevFrom AND :prevTo)
                  AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
                  AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
                  AND (CAST(:doiTuongIds AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
                  AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
                  AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
                  AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
            ),
            per_doi_tuong AS (
                SELECT hop_dong_doi_tuong_id AS doi_tuong_id,
                       COUNT(*) FILTER (WHERE ngay_thuc_hien BETWEEN :fromDate AND :toDate) AS cur_cnt,
                       SUM(CASE WHEN trang_thai = 'done' AND ngay_thuc_hien BETWEEN :fromDate AND :toDate
                                THEN COALESCE(don_gia, 0) * COALESCE(khoi_luong_hoan_thanh, 0)
                                ELSE 0 END) AS productive_total,
                       SUM(CASE WHEN trang_thai = 'done' AND ngay_thuc_hien BETWEEN :fromDate AND :toDate
                                 AND ((ngay_cap_nhat AT TIME ZONE 'Asia/Ho_Chi_Minh')::date = :today
                                      OR ngay_thuc_hien = :today)
                                THEN COALESCE(don_gia, 0) * COALESCE(khoi_luong_hoan_thanh, 0)
                                ELSE 0 END) AS today_total,
                       SUM(CASE WHEN trang_thai = 'done' AND ngay_thuc_hien BETWEEN :prevFrom AND :prevTo
                                THEN COALESCE(don_gia, 0) * COALESCE(khoi_luong_hoan_thanh, 0)
                                ELSE 0 END) AS previous_total
                FROM sl_filtered
                GROUP BY hop_dong_doi_tuong_id
            )
            SELECT
                COUNT(*) FILTER (WHERE pd.cur_cnt > 0)::int AS totalDisplayed,
                COUNT(*) FILTER (WHERE pd.productive_total > 0)::int AS withOutput,
                COALESCE(SUM(pd.productive_total), 0) AS periodTotal,
                COALESCE(SUM(pd.today_total), 0) AS todayTotal,
                COALESCE(SUM(pd.previous_total), 0) AS previousTotal
            FROM per_doi_tuong pd
            """, nativeQuery = true)
    Optional<SanLuongTongHopProjection> aggregateTongHop(
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
            @Param("prevFrom") LocalDate prevFrom, @Param("prevTo") LocalDate prevTo, @Param("today") LocalDate today,
            @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongIds") String doiTuongIds, @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId);

    /**
     * 1 dòng / đối tượng đang hoạt động, chỉ tính trong khoảng [fromDate, toDate] (KHÔNG quét toàn bộ lịch sử):
     * tổng hạng mục áp dụng (lá: chi tiết không có công việc con, hoặc công việc), số hạng mục 'done' trong kỳ,
     * giá trị sản lượng trong kỳ, nhà thầu/hợp đồng/tên đối tượng.
     * Thứ tự đọc: lọc ĐỐI TƯỢNG trước (nhà thầu, hợp đồng, khu vực, tỉnh) rồi mới đọc san_luong của các đối tượng đó ({@code slb},
     * gom theo đối tượng + hạng mục lá), sau đó nối hạng mục lá bằng khoá duy nhất ({@code slx}); danh sách lá chỉ dựng cho hợp đồng cần.
     * Là nguồn chung cho {@link #progressBundle} (nhóm đối tượng, 20 đối tượng đầu, trang đối tượng chưa có sản lượng) và {@link #demDoiTuongChuaCoSanLuong}.
     */
    String PER_CTES = """
            WITH leaf_items AS (
                SELECT ct.id AS leaf_id, ct.id AS ct_id, FALSE AS is_cv, nh.hop_dong_id
                FROM hang_muc_chi_tiet ct
                JOIN hang_muc_nhom nh ON nh.id = ct.hang_muc_nhom_id AND nh.ngay_xoa IS NULL AND nh.hoat_dong = TRUE
                WHERE ct.ngay_xoa IS NULL AND ct.hoat_dong = TRUE
                  AND (CAST(:hopDongId AS uuid) IS NULL OR nh.hop_dong_id = CAST(:hopDongId AS uuid))
                  AND NOT EXISTS (SELECT 1 FROM hang_muc_cong_viec cv
                                  WHERE cv.hang_muc_chi_tiet_id = ct.id AND cv.ngay_xoa IS NULL)
                UNION ALL
                SELECT cv.id AS leaf_id, ct.id AS ct_id, TRUE AS is_cv, nh.hop_dong_id
                FROM hang_muc_cong_viec cv
                JOIN hang_muc_chi_tiet ct ON ct.id = cv.hang_muc_chi_tiet_id AND ct.ngay_xoa IS NULL AND ct.hoat_dong = TRUE
                JOIN hang_muc_nhom nh ON nh.id = ct.hang_muc_nhom_id AND nh.ngay_xoa IS NULL AND nh.hoat_dong = TRUE
                WHERE cv.ngay_xoa IS NULL
                  AND (CAST(:hopDongId AS uuid) IS NULL OR nh.hop_dong_id = CAST(:hopDongId AS uuid))
            ),
            leaf_cnt AS (
                SELECT hop_dong_id, COUNT(*) AS total FROM leaf_items GROUP BY hop_dong_id
            ),
            ct_children AS (
                SELECT ct_id, hop_dong_id, COUNT(*) AS cnt FROM leaf_items WHERE is_cv GROUP BY ct_id, hop_dong_id
            ),
            slb AS (
                SELECT s.hop_dong_doi_tuong_id AS did, d2.hop_dong_id AS hd, COALESCE(s.hang_muc_cong_viec_id, s.hang_muc_chi_tiet_id) AS leaf,
                       SUM(COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0)) AS v
                FROM hop_dong_doi_tuong d2
                JOIN san_luong s ON s.hop_dong_doi_tuong_id = d2.id
                WHERE d2.ngay_xoa IS NULL AND d2.hoat_dong = TRUE
                  AND (CAST(:nhaThauId AS uuid) IS NULL OR d2.nha_thau_id = CAST(:nhaThauId AS uuid))
                  AND (CAST(:hopDongId AS uuid) IS NULL OR d2.hop_dong_id = CAST(:hopDongId AS uuid))
                  AND (CAST(:doiTuongIds AS text) IS NULL OR d2.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d2.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
                  AND (CAST(:khuVucId AS uuid) IS NULL OR d2.khu_vuc_id = CAST(:khuVucId AS uuid))
                  AND (CAST(:tinhThanhId AS uuid) IS NULL OR d2.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
                  AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d2.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
                  AND s.ngay_xoa IS NULL AND s.hoat_dong = TRUE AND s.trang_thai = 'done'
                  AND s.ngay_thuc_hien BETWEEN :fromDate AND :toDate
                GROUP BY s.hop_dong_doi_tuong_id, d2.hop_dong_id, COALESCE(s.hang_muc_cong_viec_id, s.hang_muc_chi_tiet_id)
            ),
            slx AS (
                SELECT b.did, SUM(b.v) AS period_value, COUNT(li.leaf_id) FILTER (WHERE li.hop_dong_id = b.hd) AS done_cnt
                FROM slb b
                LEFT JOIN leaf_items li ON li.leaf_id = b.leaf
                GROUP BY b.did
            ),
            latest AS (
                SELECT DISTINCT ON (s.hop_dong_doi_tuong_id, COALESCE(s.hang_muc_cong_viec_id, s.hang_muc_chi_tiet_id))
                       s.hop_dong_doi_tuong_id AS did, COALESCE(s.hang_muc_cong_viec_id, s.hang_muc_chi_tiet_id) AS leaf, s.trang_thai AS st
                FROM hop_dong_doi_tuong d3
                JOIN san_luong s ON s.hop_dong_doi_tuong_id = d3.id
                WHERE d3.ngay_xoa IS NULL AND d3.hoat_dong = TRUE
                  AND (CAST(:nhaThauId AS uuid) IS NULL OR d3.nha_thau_id = CAST(:nhaThauId AS uuid))
                  AND (CAST(:hopDongId AS uuid) IS NULL OR d3.hop_dong_id = CAST(:hopDongId AS uuid))
                  AND (CAST(:doiTuongIds AS text) IS NULL OR d3.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d3.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
                  AND (CAST(:khuVucId AS uuid) IS NULL OR d3.khu_vuc_id = CAST(:khuVucId AS uuid))
                  AND (CAST(:tinhThanhId AS uuid) IS NULL OR d3.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
                  AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d3.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
                  AND s.ngay_xoa IS NULL AND s.hoat_dong = TRUE
                  AND COALESCE(s.hang_muc_cong_viec_id, s.hang_muc_chi_tiet_id) IS NOT NULL
                ORDER BY s.hop_dong_doi_tuong_id, COALESCE(s.hang_muc_cong_viec_id, s.hang_muc_chi_tiet_id), s.ngay_cap_nhat DESC NULLS LAST, s.id
            ),
            prog_direct AS (
                SELECT l.did, COUNT(*) AS n, COUNT(*) FILTER (WHERE LOWER(l.st) = 'done') AS nd
                FROM latest l
                JOIN hop_dong_doi_tuong d4 ON d4.id = l.did
                JOIN leaf_items li ON li.leaf_id = l.leaf AND li.hop_dong_id = d4.hop_dong_id
                GROUP BY l.did
            ),
            direct_by_ct AS (
                SELECT l2.did, li2.ct_id, COUNT(*) AS cnt
                FROM latest l2
                JOIN leaf_items li2 ON li2.leaf_id = l2.leaf AND li2.is_cv
                GROUP BY l2.did, li2.ct_id
            ),
            prog_fb AS (
                SELECT l.did, SUM(c.cnt - COALESCE(dc.cnt, 0)) AS n,
                       SUM(CASE WHEN LOWER(l.st) = 'done' THEN c.cnt - COALESCE(dc.cnt, 0) ELSE 0 END) AS nd
                FROM latest l
                JOIN hop_dong_doi_tuong d4 ON d4.id = l.did
                JOIN ct_children c ON c.ct_id = l.leaf AND c.hop_dong_id = d4.hop_dong_id
                LEFT JOIN direct_by_ct dc ON dc.did = l.did AND dc.ct_id = c.ct_id
                GROUP BY l.did
            )
            """;

    String PER_FINAL = """
            SELECT
                d.id AS doiTuongId,
                COALESCE(dt.ten, dt.ma, '—') AS doiTuongTen,
                d.hop_dong_id AS hopDongId,
                COALESCE(h.ma_hop_dong, h.ten, '—') AS hopDongTen,
                d.nha_thau_id AS nhaThauId,
                COALESCE(nd.ho_ten, '—') AS nhaThauTen,
                d.khu_vuc_id AS khuVucId,
                COALESCE(kv.ten, kv.ma, '—') AS khuVucTen,
                d.tinh_thanh_id AS tinhId,
                COALESCE(tt0.ten, tt0.ma, '—') AS tinhTen,
                h.loai_hop_dong_id AS loaiHopDongId,
                COALESCE(lh0.ten, '—') AS loaiHopDongTen,
                d.co_vuong_mac_mo AS hasOpenVuongMac,
                COALESCE(slx.period_value, 0) AS periodValue,
                CASE WHEN COALESCE(pd.n, 0) + COALESCE(pf.n, 0) > 0 THEN (COALESCE(pd.n, 0) + COALESCE(pf.n, 0))::int ELSE COALESCE(lc.total, 0)::int END AS totalHangMuc,
                (COALESCE(pd.nd, 0) + COALESCE(pf.nd, 0))::int AS doneHangMuc,
                COALESCE(slx.done_cnt, 0)::int AS doneTrongKy
            FROM hop_dong_doi_tuong d
            INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id AND dt.ngay_xoa IS NULL
            LEFT JOIN nguoi_dung nd ON nd.id = d.nha_thau_id
            LEFT JOIN khu_vuc kv ON kv.id = d.khu_vuc_id
            LEFT JOIN tinh_thanh tt0 ON tt0.id = d.tinh_thanh_id
            LEFT JOIN loai_hop_dong lh0 ON lh0.id = h.loai_hop_dong_id
            LEFT JOIN leaf_cnt lc ON lc.hop_dong_id = d.hop_dong_id
            LEFT JOIN slx ON slx.did = d.id
            LEFT JOIN prog_direct pd ON pd.did = d.id
            LEFT JOIN prog_fb pf ON pf.did = d.id
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongIds AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
            """;

    String PER_DOI_TUONG = PER_CTES + PER_FINAL;

    /** Câu gộp của {@link #progressBundle}: dựng bảng đối tượng 1 lần (CTE MATERIALIZED) rồi trả 3 loại dòng G/T/C. */
    String PROGRESS_BUNDLE_SQL = PER_CTES + """
            ,
            per AS MATERIALIZED (
            """ + PER_FINAL + """
            )
            SELECT 'G' AS kind, CAST(NULL AS uuid) AS doiTuongId, CAST(NULL AS text) AS doiTuongTen, hopDongId, hopDongTen, nhaThauId, nhaThauTen,
                   khuVucId, khuVucTen, tinhId, tinhTen, loaiHopDongId, loaiHopDongTen, hasOpenVuongMac, periodValue, totalHangMuc, doneHangMuc, COUNT(*) AS n,
                   CAST(NULL AS text) AS maDoiTuong, CAST(NULL AS bigint) AS tong
            FROM per
            GROUP BY hopDongId, hopDongTen, nhaThauId, nhaThauTen, khuVucId, khuVucTen, tinhId, tinhTen, loaiHopDongId, loaiHopDongTen, hasOpenVuongMac, periodValue, totalHangMuc, doneHangMuc
            UNION ALL
            SELECT * FROM (
                SELECT 'T', doiTuongId, doiTuongTen, hopDongId, hopDongTen, nhaThauId, nhaThauTen, khuVucId, khuVucTen, tinhId, tinhTen, loaiHopDongId, loaiHopDongTen, hasOpenVuongMac,
                       periodValue, totalHangMuc, doneHangMuc, CAST(NULL AS bigint), CAST(NULL AS text), CAST(NULL AS bigint)
                FROM per
                WHERE (CAST(:chiCoSanLuong AS boolean) IS NOT TRUE OR periodValue > 0)
                ORDER BY periodValue DESC, doiTuongTen COLLATE "C" ASC, CAST(doiTuongId AS text) ASC
                LIMIT 20
            ) t
            UNION ALL
            SELECT 'C', c.doiTuongId, c.doiTuongTen, c.hopDongId, c.hopDongTen, c.nhaThauId, c.nhaThauTen, c.khuVucId, c.khuVucTen, c.tinhId, c.tinhTen, c.loaiHopDongId, c.loaiHopDongTen,
                   c.hasOpenVuongMac, c.periodValue, c.totalHangMuc, c.doneHangMuc, CAST(NULL AS bigint),
                   (SELECT g.gia_tri FROM hop_dong_doi_tuong_gia_tri g
                      JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.la_khoa_chinh = TRUE AND tt.ngay_xoa IS NULL
                     WHERE g.ngay_xoa IS NULL AND g.hop_dong_doi_tuong_id = c.doiTuongId LIMIT 1),
                   c.tong
            FROM (
                SELECT p.*, COUNT(*) OVER () AS tong
                FROM per p
                WHERE p.periodValue = 0 AND p.doneTrongKy = 0
                ORDER BY p.hopDongTen COLLATE "C" ASC, p.doiTuongTen COLLATE "C" ASC, CAST(p.doiTuongId AS text) ASC
                LIMIT :limit OFFSET :offset
            ) c
            """;

    /**
     * 3 kết quả từ MỘT lần tính bảng đối tượng ({@link #PER_DOI_TUONG} chạy 1 lần thay vì 3), phân biệt bằng cột {@code kind}:
     * <ul>
     *   <li>'G': gom các đối tượng cùng đặc điểm thành 1 dòng (cột {@code n} = số đối tượng), đủ để tính tiến độ và xếp hạng nhà thầu,
     *   khu vực, hợp đồng, xu hướng mà không đổi kết quả;</li>
     *   <li>'T': 20 đối tượng đầu theo giá trị kỳ giảm dần, đồng hạng theo tên rồi id; sắp theo {@code COLLATE "C"} để trùng thứ tự
     *   {@code String.compareTo} của bộ xếp hạng ở Java; {@code :chiCoSanLuong} = true thì chỉ lấy đối tượng có sản lượng;</li>
     *   <li>'C': 1 trang đối tượng chưa có sản lượng (giá trị kỳ = 0 và không hạng mục hoàn thành) kèm mã khoá chính và tổng số
     *   ({@code tong}); sắp theo hợp đồng, tên đối tượng, id để phân trang ổn định.</li>
     * </ul>
     */
    @Query(value = PROGRESS_BUNDLE_SQL, nativeQuery = true)
    List<ProgressBundleRow> progressBundle(
            @Param("chiCoSanLuong") Boolean chiCoSanLuong,
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
            @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongIds") String doiTuongIds, @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("limit") int limit, @Param("offset") long offset);

    /**
     * Danh sách từng đối tượng (bảng trên trang Sản lượng) — chỉ chạy khi người dùng yêu cầu. Dùng lại bảng đối tượng {@link #PER_CTES}
     * (tiến độ hạng mục theo định nghĩa REST), thêm sản lượng theo đúng cách REST tính cho từng dòng:
     * SL tổng = lũy kế mọi bản ghi tính tiền (done/survey/design, không phụ thuộc khoảng ngày), SL hôm nay = bản ghi tính tiền có
     * ngày thực hiện = {@code :today}, thi công gần nhất = ngày thực hiện lớn nhất của bản ghi done.
     * Mặc định chỉ lấy đối tượng có ít nhất 1 bản ghi trong kỳ (như REST); {@code :tatCa} = true lấy cả đối tượng chưa có.
     * {@code :sapXep}: latest (mặc định, như REST), periodValue, totalValue, todayValue — giảm dần, đồng hạng theo id.
     */
    String DANH_SACH_SQL = PER_CTES + """
            ,
            per AS MATERIALIZED (
            """ + PER_FINAL + """
            ),
            agg AS (
                SELECT p.doiTuongId AS did,
                       COUNT(*) FILTER (WHERE s.ngay_thuc_hien BETWEEN :fromDate AND :toDate) AS cnt_ky,
                       SUM(CASE WHEN s.trang_thai IN ('done', 'survey', 'design')
                                THEN COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0) ELSE 0 END) AS total_value,
                       SUM(CASE WHEN s.trang_thai IN ('done', 'survey', 'design') AND s.ngay_thuc_hien = :today
                                THEN COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0) ELSE 0 END) AS today_value,
                       MAX(s.ngay_thuc_hien) FILTER (WHERE s.trang_thai = 'done') AS last_done
                FROM per p
                JOIN san_luong s ON s.hop_dong_doi_tuong_id = p.doiTuongId AND s.ngay_xoa IS NULL AND s.hoat_dong = TRUE
                GROUP BY p.doiTuongId
            ),
            lst AS (
                SELECT p.*, COALESCE(a.total_value, 0) AS total_value, COALESCE(a.today_value, 0) AS today_value, a.last_done,
                       COUNT(*) OVER () AS tong,
                       ROW_NUMBER() OVER (ORDER BY
                           CASE WHEN CAST(:sapXep AS text) = 'periodValue' THEN p.periodValue END DESC NULLS LAST,
                           CASE WHEN CAST(:sapXep AS text) = 'totalValue' THEN COALESCE(a.total_value, 0) END DESC NULLS LAST,
                           CASE WHEN CAST(:sapXep AS text) = 'todayValue' THEN COALESCE(a.today_value, 0) END DESC NULLS LAST,
                           CASE WHEN CAST(:sapXep AS text) NOT IN ('periodValue', 'totalValue', 'todayValue') THEN a.last_done END DESC NULLS LAST,
                           CAST(p.doiTuongId AS text) DESC) AS rn
                FROM per p
                LEFT JOIN agg a ON a.did = p.doiTuongId
                WHERE CAST(:tatCa AS boolean) IS TRUE OR COALESCE(a.cnt_ky, 0) > 0
            ),
            pg AS (
                SELECT * FROM lst ORDER BY rn LIMIT :limit OFFSET :offset
            )
            SELECT pg.doiTuongId AS doiTuongId,
                   (SELECT g.gia_tri FROM hop_dong_doi_tuong_gia_tri g
                      JOIN thuoc_tinh tt2 ON tt2.id = g.thuoc_tinh_id AND tt2.la_khoa_chinh = TRUE AND tt2.ngay_xoa IS NULL
                     WHERE g.ngay_xoa IS NULL AND g.hop_dong_doi_tuong_id = pg.doiTuongId LIMIT 1) AS maDoiTuong,
                   pg.doiTuongTen AS doiTuongTen, pg.hopDongTen AS hopDongTen, pg.nhaThauTen AS nhaThauTen, pg.khuVucTen AS khuVucTen,
                   tt.ma AS tinhMa, tt.ten AS tinhTen, tt.la_tinh_cu AS laTinhCu,
                   pg.hasOpenVuongMac AS hasOpenVuongMac, pg.periodValue AS periodValue,
                   pg.total_value AS totalValue, pg.today_value AS todayValue, pg.last_done AS thiCongGanNhat,
                   pg.totalHangMuc AS totalHangMuc, pg.doneHangMuc AS doneHangMuc, pg.tong AS tong
            FROM pg
            JOIN hop_dong_doi_tuong d ON d.id = pg.doiTuongId
            LEFT JOIN tinh_thanh tt ON tt.id = d.tinh_thanh_id
            ORDER BY pg.rn
            """;

    @Query(value = DANH_SACH_SQL, nativeQuery = true)
    List<DoiTuongListProjection> danhSachDoiTuong(
            @Param("tatCa") Boolean tatCa, @Param("sapXep") String sapXep,
            @Param("today") LocalDate today,
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
            @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongIds") String doiTuongIds, @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("limit") int limit, @Param("offset") long offset);

    /**
     * Hạng mục lá của hợp đồng (chi tiết không có công việc con, hoặc công việc) ghép với bản ghi sản lượng MỚI NHẤT của từng đối tượng cụ thể
     * ({@code :doiTuongIds} là id đối tượng cụ thể, tối đa 5 đối tượng). Hạng mục công việc chưa có bản ghi riêng thì kế thừa bản ghi ở cấp chi tiết
     * (cùng quy tắc tiến độ REST). Sắp theo nhóm, mã chi tiết, thứ tự công việc như REST {@code /doi-tuong/{id}/hang-muc}.
     */
    @Query(value = """
            WITH obj AS (
                SELECT d.id AS did, d.hop_dong_id AS hid,
                       COALESCE((SELECT g.gia_tri FROM hop_dong_doi_tuong_gia_tri g
                                   JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.la_khoa_chinh = TRUE AND tt.ngay_xoa IS NULL
                                  WHERE g.ngay_xoa IS NULL AND g.hop_dong_doi_tuong_id = d.id LIMIT 1), dt.ten, dt.ma) AS ma
                FROM hop_dong_doi_tuong d
                JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                LEFT JOIN doi_tuong_quan_ly dt ON dt.id = d.doi_tuong_quan_ly_id AND dt.ngay_xoa IS NULL
                WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
                  AND d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[]))
                ORDER BY d.id
                LIMIT 5
            ),
            lv AS (
                SELECT nh.hop_dong_id AS hid, nh.thu_tu AS nhom_tt, nh.ten AS nhom_ten, ct.ma AS ct_ma, ct.id AS ct_id, ct.id AS leaf_id, FALSE AS is_cv,
                       ct.ma AS ma, ct.ten AS ten, ct.don_gia AS don_gia, 0 AS cv_tt
                FROM hang_muc_chi_tiet ct
                JOIN hang_muc_nhom nh ON nh.id = ct.hang_muc_nhom_id AND nh.ngay_xoa IS NULL AND nh.hoat_dong = TRUE
                WHERE ct.ngay_xoa IS NULL AND ct.hoat_dong = TRUE
                  AND nh.hop_dong_id IN (SELECT hid FROM obj)
                  AND NOT EXISTS (SELECT 1 FROM hang_muc_cong_viec cv WHERE cv.hang_muc_chi_tiet_id = ct.id AND cv.ngay_xoa IS NULL)
                UNION ALL
                SELECT nh.hop_dong_id, nh.thu_tu, nh.ten, ct.ma, ct.id, cv.id, TRUE, cv.ma, cv.ten, cv.don_gia, cv.thu_tu
                FROM hang_muc_cong_viec cv
                JOIN hang_muc_chi_tiet ct ON ct.id = cv.hang_muc_chi_tiet_id AND ct.ngay_xoa IS NULL AND ct.hoat_dong = TRUE
                JOIN hang_muc_nhom nh ON nh.id = ct.hang_muc_nhom_id AND nh.ngay_xoa IS NULL AND nh.hoat_dong = TRUE
                WHERE cv.ngay_xoa IS NULL
                  AND nh.hop_dong_id IN (SELECT hid FROM obj)
            ),
            lat AS (
                SELECT DISTINCT ON (s.hop_dong_doi_tuong_id, COALESCE(s.hang_muc_cong_viec_id, s.hang_muc_chi_tiet_id))
                       s.hop_dong_doi_tuong_id AS did, COALESCE(s.hang_muc_cong_viec_id, s.hang_muc_chi_tiet_id) AS leaf,
                       s.trang_thai, s.ngay_thuc_hien, s.khoi_luong_hoan_thanh, s.don_gia, s.ket_qua_nghiem_thu, s.ly_do_khong_dat
                FROM san_luong s
                WHERE s.hop_dong_doi_tuong_id IN (SELECT did FROM obj)
                  AND s.ngay_xoa IS NULL AND s.hoat_dong = TRUE
                  AND COALESCE(s.hang_muc_cong_viec_id, s.hang_muc_chi_tiet_id) IS NOT NULL
                ORDER BY s.hop_dong_doi_tuong_id, COALESCE(s.hang_muc_cong_viec_id, s.hang_muc_chi_tiet_id), s.ngay_cap_nhat DESC NULLS LAST, s.id
            )
            SELECT o.did AS doiTuongId, o.ma AS maDoiTuong, l.nhom_ten AS nhom, l.ma AS maHangMuc, l.ten AS tenHangMuc,
                   COALESCE(x.don_gia, l.don_gia) AS donGia, x.trang_thai AS trangThai, x.ngay_thuc_hien AS ngayThucHien,
                   x.khoi_luong_hoan_thanh AS khoiLuong,
                   COALESCE(x.don_gia, 0) * COALESCE(x.khoi_luong_hoan_thanh, 0) AS giaTri,
                   x.ket_qua_nghiem_thu AS ketQuaNghiemThu, x.ly_do_khong_dat AS lyDoKhongDat
            FROM obj o
            JOIN lv l ON l.hid = o.hid
            LEFT JOIN LATERAL (
                SELECT a.* FROM lat a WHERE a.did = o.did AND a.leaf = l.leaf_id
                UNION ALL
                SELECT b.* FROM lat b WHERE l.is_cv AND b.did = o.did AND b.leaf = l.ct_id
                  AND NOT EXISTS (SELECT 1 FROM lat a2 WHERE a2.did = o.did AND a2.leaf = l.leaf_id)
                LIMIT 1
            ) x ON TRUE
            ORDER BY o.did, l.nhom_tt, l.ct_ma, l.cv_tt, l.ma
            """, nativeQuery = true)
    List<HangMucDoiTuongProjection> hangMucCuaDoiTuong(
            @Param("doiTuongIds") String doiTuongIds);

    /**
     * Số vướng mắc mở tạo trong [tuFrom, denTo) của các đối tượng đang hoạt động khớp bộ lọc — cùng điều kiện với
     * {@code VuongMacRepository.demMoTrongGiaiDoan} nhưng lọc bằng JOIN thay vì truyền hàng nghìn id đối tượng.
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM vuong_mac v
            JOIN hop_dong_doi_tuong d ON d.id = v.du_lieu_doi_tuong_id
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE v.ngay_xoa IS NULL AND v.hoat_dong = TRUE
              AND v.trang_thai IN ('pending', 'in_progress')
              AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
              AND v.ngay_tao >= CAST(:tuFrom AS timestamptz) AND v.ngay_tao < CAST(:denTo AS timestamptz)
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongIds AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
            """, nativeQuery = true)
    long demVuongMacMoTrongKy(
            @Param("tuFrom") java.time.Instant tuFrom, @Param("denTo") java.time.Instant denTo,
            @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongIds") String doiTuongIds, @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId);

    /**
     * Giá trị sản lượng hoàn thành theo từng kỳ con (bucket) cho xu hướng: chỉ trả các kỳ CÓ sản lượng. Không tính tỷ lệ hoàn thành hạng mục
     * theo kỳ (đắt, chỉ có ở tool MCP, REST không có).
     * :bucketFrom/:bucketTo là danh sách ngày dạng chuỗi 'yyyy-MM-dd,yyyy-MM-dd,...' (cùng độ dài, cùng thứ tự).
     * row: [chỉ số kỳ (int, từ 1), tổng giá trị sản lượng].
     */
    @Query(value = """
            WITH buckets AS (
                SELECT b.idx, b.bf, b.bt
                FROM unnest(string_to_array(:bucketFrom, ',')::date[], string_to_array(:bucketTo, ',')::date[])
                     WITH ORDINALITY AS b(bf, bt, idx)
            )
            SELECT b.idx::int, COALESCE(SUM(COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0)), 0)
            FROM hop_dong_doi_tuong d
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            JOIN san_luong s ON s.hop_dong_doi_tuong_id = d.id
            JOIN buckets b ON s.ngay_thuc_hien BETWEEN b.bf AND b.bt
            WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongIds AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
              AND s.ngay_xoa IS NULL AND s.hoat_dong = TRUE AND s.trang_thai = 'done'
            GROUP BY b.idx
            """, nativeQuery = true)
    List<Object[]> sanLuongTheoKy(
            @Param("bucketFrom") String bucketFrom, @Param("bucketTo") String bucketTo,
            @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongIds") String doiTuongIds, @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId);

    /**
     * Nhà thầu đang phụ trách >=1 đối tượng hoạt động nhưng KHÔNG có bản ghi san_luong 'done' nào
     * trong [fromDate, toDate] cho bất kỳ đối tượng nào mình phụ trách (lọc theo bộ lọc thu hẹp nếu có).
     */
    @Query(value = """
            WITH agg AS (
                SELECT d.nha_thau_id,
                       COUNT(*) AS soDoiTuong,
                       COUNT(*) FILTER (WHERE EXISTS (
                           SELECT 1 FROM san_luong s
                           WHERE s.hop_dong_doi_tuong_id = d.id AND s.ngay_xoa IS NULL AND s.hoat_dong = TRUE
                             AND s.trang_thai = 'done'
                             AND s.ngay_thuc_hien BETWEEN :fromDate AND :toDate
                       )) AS soDaBao
                FROM hop_dong_doi_tuong d
                INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND d.nha_thau_id IS NOT NULL
                  AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
                  AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
                  AND (CAST(:doiTuongIds AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
                      AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
                      AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
                      AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
                GROUP BY d.nha_thau_id
            )
            SELECT nd.id AS nhaThauId, nd.ho_ten AS tenNhaThau, agg.soDoiTuong AS soDoiTuongPhuTrach, COUNT(*) OVER () AS tong
            FROM agg
            INNER JOIN nguoi_dung nd ON nd.id = agg.nha_thau_id
            WHERE agg.soDaBao = 0
                ORDER BY nd.ho_ten
                """,
            nativeQuery = true)
    List<NhaThauChuaBaoProjection> findNhaThauChuaBaoTrongKy(
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
            @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
                    @Param("doiTuongIds") String doiTuongIds, @Param("khuVucId") UUID khuVucId,
                    @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId, Pageable pageable);

    /** Tổng số nhà thầu của {@link #findNhaThauChuaBaoTrongKy} — chạy song song với câu lấy trang. */
    @Query(value = """
                WITH agg AS (
              SELECT d.nha_thau_id,
                     COUNT(*) FILTER (WHERE EXISTS (
                   SELECT 1 FROM san_luong s
                   WHERE s.hop_dong_doi_tuong_id = d.id AND s.ngay_xoa IS NULL AND s.hoat_dong = TRUE
                     AND s.trang_thai = 'done'
                     AND s.ngay_thuc_hien BETWEEN :fromDate AND :toDate
                     )) AS soDaBao
              FROM hop_dong_doi_tuong d
              INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
              WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND d.nha_thau_id IS NOT NULL
                AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
                AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
                AND (CAST(:doiTuongIds AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
                AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
                AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
                AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
              GROUP BY d.nha_thau_id
                )
                SELECT COUNT(*) FROM agg WHERE soDaBao = 0
                """, nativeQuery = true)
    long demNhaThauChuaBaoTrongKy(
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
            @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongIds") String doiTuongIds, @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId);

    /**
     * Nhà thầu đang phụ trách >=1 đối tượng hoạt động và CÓ ÍT NHẤT 1 bản ghi san_luong 'done'
     * trong [fromDate, toDate] cho đối tượng mình phụ trách (lọc theo bộ lọc thu hẹp nếu có) — đối
     * xứng với {@link #findNhaThauChuaBaoTrongKy}. {@code giaTriDaBao} là tổng giá trị các bản ghi
     * 'done' trong kỳ của nhà thầu đó (không giới hạn theo đối tượng đã lọc riêng lẻ ở trên).
     */
    @Query(value = """
            WITH agg AS (
                SELECT d.nha_thau_id,
                       COUNT(*) AS soDoiTuong,
                       COUNT(*) FILTER (WHERE EXISTS (
                           SELECT 1 FROM san_luong s
                           WHERE s.hop_dong_doi_tuong_id = d.id AND s.ngay_xoa IS NULL AND s.hoat_dong = TRUE
                             AND s.trang_thai = 'done'
                             AND s.ngay_thuc_hien BETWEEN :fromDate AND :toDate
                       )) AS soDaBao
                FROM hop_dong_doi_tuong d
                INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND d.nha_thau_id IS NOT NULL
                  AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
                  AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
                  AND (CAST(:doiTuongIds AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
                      AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
                      AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
                      AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
                GROUP BY d.nha_thau_id
            ),
            gt AS (
                SELECT d.nha_thau_id, SUM(COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0)) AS giaTriDaBao
                FROM san_luong s
                JOIN hop_dong_doi_tuong d ON d.id = s.hop_dong_doi_tuong_id AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
                INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
                WHERE s.ngay_xoa IS NULL AND s.hoat_dong = TRUE AND s.trang_thai = 'done'
                  AND s.ngay_thuc_hien BETWEEN :fromDate AND :toDate
                  AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
                  AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
                  AND (CAST(:doiTuongIds AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
                  AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
                  AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
                  AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
                GROUP BY d.nha_thau_id
            )
            SELECT nd.id AS nhaThauId, nd.ho_ten AS tenNhaThau, agg.soDoiTuong AS soDoiTuongPhuTrach,
                   COALESCE(gt.giaTriDaBao, 0) AS giaTriDaBao, COUNT(*) OVER () AS tong
            FROM agg
            INNER JOIN nguoi_dung nd ON nd.id = agg.nha_thau_id
            LEFT JOIN gt ON gt.nha_thau_id = agg.nha_thau_id
            WHERE agg.soDaBao > 0
                ORDER BY nd.ho_ten
                """,
            nativeQuery = true)
    List<NhaThauDaBaoProjection> findNhaThauDaBaoTrongKy(
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
            @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
                    @Param("doiTuongIds") String doiTuongIds, @Param("khuVucId") UUID khuVucId,
                    @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId, Pageable pageable);

    /** Tổng số nhà thầu của {@link #findNhaThauDaBaoTrongKy} — chạy song song với câu lấy trang. */
    @Query(value = """
                WITH agg AS (
              SELECT d.nha_thau_id,
                     COUNT(*) FILTER (WHERE EXISTS (
                   SELECT 1 FROM san_luong s
                   WHERE s.hop_dong_doi_tuong_id = d.id AND s.ngay_xoa IS NULL AND s.hoat_dong = TRUE
                     AND s.trang_thai = 'done'
                     AND s.ngay_thuc_hien BETWEEN :fromDate AND :toDate
                     )) AS soDaBao
              FROM hop_dong_doi_tuong d
              INNER JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
              WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND d.nha_thau_id IS NOT NULL
                AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
                AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
                AND (CAST(:doiTuongIds AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
                AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
                AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
                AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
              GROUP BY d.nha_thau_id
                )
                SELECT COUNT(*) FROM agg WHERE soDaBao > 0
                """, nativeQuery = true)
    long demNhaThauDaBaoTrongKy(
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
            @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongIds") String doiTuongIds, @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId);

    /** Ngày thực hiện sớm nhất của sản lượng đang hoạt động — mốc bắt đầu khi không truyền khoảng ngày (toàn bộ thời gian, như trang FE). */
    @Query(value = "SELECT MIN(s.ngay_thuc_hien) FROM san_luong s WHERE s.ngay_xoa IS NULL AND s.hoat_dong = TRUE", nativeQuery = true)
    LocalDate findNgayThucHienSomNhat();

    /**
     * Hạng mục sản lượng nghiệm thu KHÔNG ĐẠT (kết quả 'khong_dat'), mới nhất trước. Không lọc hoat_dong vì hạng mục không đạt bị đặt
     * hoat_dong = false khi nghiệm thu. Ngày lọc theo ngày nghiệm thu.
     */
    @Query(value = """
            SELECT COALESCE(hcv.ma, hct.ma) AS maHangMuc, COALESCE(hcv.ten, hct.ten) AS tenHangMuc, h.ma_hop_dong AS maHopDong,
                   COALESCE(q.ten, q.ma) AS loaiDoiTuong,
                   (SELECT g.gia_tri FROM hop_dong_doi_tuong_gia_tri g
                     JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.la_khoa_chinh = TRUE AND tt.ngay_xoa IS NULL
                     WHERE g.hop_dong_doi_tuong_id = d.id AND g.ngay_xoa IS NULL LIMIT 1) AS maDoiTuong,
                   nd.ho_ten AS nhaThau, s.ly_do_khong_dat AS lyDo, s.nguoi_nghiem_thu_ten AS nguoiNghiemThu,
                   s.ngay_nghiem_thu AS ngayNghiemThu
            FROM san_luong s
            JOIN hop_dong_doi_tuong d ON d.id = s.hop_dong_doi_tuong_id AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            LEFT JOIN hang_muc_cong_viec hcv ON hcv.id = s.hang_muc_cong_viec_id
            LEFT JOIN hang_muc_chi_tiet hct ON hct.id = s.hang_muc_chi_tiet_id
            LEFT JOIN doi_tuong_quan_ly q ON q.id = d.doi_tuong_quan_ly_id
            LEFT JOIN nguoi_dung nd ON nd.id = d.nha_thau_id
            WHERE s.ngay_xoa IS NULL AND s.ket_qua_nghiem_thu = 'khong_dat'
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongIds AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
              AND (CAST(:tuFrom AS timestamptz) IS NULL OR s.ngay_nghiem_thu >= CAST(:tuFrom AS timestamptz))
              AND (CAST(:denTo AS timestamptz) IS NULL OR s.ngay_nghiem_thu < CAST(:denTo AS timestamptz))
            ORDER BY s.ngay_nghiem_thu DESC, s.id
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM san_luong s
            JOIN hop_dong_doi_tuong d ON d.id = s.hop_dong_doi_tuong_id AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE s.ngay_xoa IS NULL AND s.ket_qua_nghiem_thu = 'khong_dat'
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongIds AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
              AND (CAST(:tuFrom AS timestamptz) IS NULL OR s.ngay_nghiem_thu >= CAST(:tuFrom AS timestamptz))
              AND (CAST(:denTo AS timestamptz) IS NULL OR s.ngay_nghiem_thu < CAST(:denTo AS timestamptz))
            """, nativeQuery = true)
    Page<SanLuongNghiemThuKhongDatItem> findNghiemThuKhongDat(
            @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongIds") String doiTuongIds, @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("tuFrom") java.time.Instant tuFrom, @Param("denTo") java.time.Instant denTo,
            Pageable pageable);

    /**
     * Số hạng mục và giá trị (khối lượng x đơn giá) theo kết quả nghiệm thu ('dat' hoặc 'khong_dat'); ngày lọc theo ngày nghiệm thu.
     * row: [ket_qua_nghiem_thu, số hạng mục, giá trị].
     */
    @Query(value = """
            SELECT s.ket_qua_nghiem_thu, COUNT(*), COALESCE(SUM(COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0)), 0)
            FROM san_luong s
            JOIN hop_dong_doi_tuong d ON d.id = s.hop_dong_doi_tuong_id AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE s.ngay_xoa IS NULL AND s.ket_qua_nghiem_thu IN ('dat', 'khong_dat')
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongIds AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
              AND (CAST(:tuFrom AS timestamptz) IS NULL OR s.ngay_nghiem_thu >= CAST(:tuFrom AS timestamptz))
              AND (CAST(:denTo AS timestamptz) IS NULL OR s.ngay_nghiem_thu < CAST(:denTo AS timestamptz))
            GROUP BY s.ket_qua_nghiem_thu
            """, nativeQuery = true)
    List<Object[]> thongKeNghiemThu(
            @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongIds") String doiTuongIds, @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("tuFrom") java.time.Instant tuFrom, @Param("denTo") java.time.Instant denTo);

    /**
     * Sản lượng đã báo hoàn thành (done, đang hoạt động) nhưng CHƯA nghiệm thu. Ngày lọc theo ngày thực hiện (có thể null).
     * row: [số hạng mục, giá trị].
     */
    @Query(value = """
            SELECT COUNT(*), COALESCE(SUM(COALESCE(s.don_gia, 0) * COALESCE(s.khoi_luong_hoan_thanh, 0)), 0)
            FROM san_luong s
            JOIN hop_dong_doi_tuong d ON d.id = s.hop_dong_doi_tuong_id AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE
            JOIN hop_dong h ON h.id = d.hop_dong_id AND h.ngay_xoa IS NULL AND h.hoat_dong = TRUE
            WHERE s.ngay_xoa IS NULL AND s.hoat_dong = TRUE AND s.trang_thai = 'done' AND s.ket_qua_nghiem_thu IS NULL
              AND (CAST(:nhaThauId AS uuid) IS NULL OR d.nha_thau_id = CAST(:nhaThauId AS uuid))
              AND (CAST(:hopDongId AS uuid) IS NULL OR d.hop_dong_id = CAST(:hopDongId AS uuid))
              AND (CAST(:doiTuongIds AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(:doiTuongIds, ',') AS uuid[])))
              AND (CAST(:khuVucId AS uuid) IS NULL OR d.khu_vuc_id = CAST(:khuVucId AS uuid))
              AND (CAST(:tinhThanhId AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:tinhThanhId AS uuid))
              AND (CAST(:loaiHopDongId AS uuid) IS NULL OR d.hop_dong_id IN (SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:loaiHopDongId AS uuid)))
              AND (CAST(:fromDate AS date) IS NULL OR s.ngay_thuc_hien >= CAST(:fromDate AS date))
              AND (CAST(:toDate AS date) IS NULL OR s.ngay_thuc_hien <= CAST(:toDate AS date))
            """, nativeQuery = true)
    List<Object[]> thongKeChoNghiemThu(
            @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongIds") String doiTuongIds, @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    /** Tổng số đối tượng của {@link #progressBundle} (loại dòng 'C') — chỉ dùng khi xin trang vượt quá trang cuối. */
    @Query(value = "SELECT COUNT(*) FROM (" + PER_DOI_TUONG + ") x WHERE x.periodValue = 0 AND x.doneTrongKy = 0", nativeQuery = true)
    long demDoiTuongChuaCoSanLuong(
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
            @Param("nhaThauId") UUID nhaThauId, @Param("hopDongId") UUID hopDongId,
            @Param("doiTuongIds") String doiTuongIds, @Param("khuVucId") UUID khuVucId,
            @Param("tinhThanhId") UUID tinhThanhId, @Param("loaiHopDongId") UUID loaiHopDongId);
}
