package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Tool CHỈ ĐỌC (không update gì) — dùng bảng ánh xạ chính thức tinh_thanh_khu_vuc (thay vì suy
 * majority-vote từ dữ liệu) để tra Khu vực theo mã tỉnh (EAV text, vd 'hyn') cho các đối tượng
 * đang thiếu khu_vuc_id. Tra tinh_thanh KHÔNG lọc ngay_xoa để vẫn khớp được cả tỉnh cũ đã sáp
 * nhập (xem TinhThanh.laTinhCu) — mã tỉnh cũ vẫn còn giá trị tra cứu dù bản ghi đã bị xóa mềm.
 *
 * <p>Bật bằng: {@code APP_TOOLS_DIAGNOSE_KHU_VUC=true}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.tools.diagnose-khu-vuc", havingValue = "true")
public class KhuVucBackfillDiagnosticTool implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        Map<String, Object> missingSummary = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS tong_thieu
                FROM hop_dong_doi_tuong
                WHERE ngay_xoa IS NULL AND hoat_dong = TRUE AND khu_vuc_id IS NULL
                """);
        log.info("[KhuVucDiag] Tổng đối tượng đang thiếu khu_vuc_id: {}", missingSummary.get("tong_thieu"));

        // Áp bảng tinh_thanh_khu_vuc (chính thức) qua mã tỉnh EAV — KHÔNG lọc ngay_xoa ở
        // tinh_thanh để vẫn khớp được tỉnh cũ đã sáp nhập.
        Map<String, Object> coverage = jdbcTemplate.queryForMap("""
                SELECT COUNT(DISTINCT d.id) AS so_va_duoc
                FROM hop_dong_doi_tuong d
                JOIN hop_dong_doi_tuong_gia_tri g ON g.hop_dong_doi_tuong_id = d.id AND g.ngay_xoa IS NULL
                JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.ngay_xoa IS NULL
                JOIN tinh_thanh t ON LOWER(t.ma) = LOWER(TRIM(g.gia_tri))
                JOIN tinh_thanh_khu_vuc tkv ON tkv.tinh_thanh_id = t.id AND tkv.ngay_xoa IS NULL AND tkv.hoat_dong = TRUE
                WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND d.khu_vuc_id IS NULL
                  AND (LOWER(COALESCE(tt.ten,'')) LIKE '%tinh%' OR LOWER(COALESCE(tt.ten,'')) LIKE '%tỉnh%' OR LOWER(COALESCE(tt.ten,'')) LIKE '%province%')
                  AND LOWER(COALESCE(tt.ten,'')) NOT LIKE '%cu%' AND LOWER(COALESCE(tt.ten,'')) NOT LIKE '%cũ%'
                """);
        log.info("[KhuVucDiag] Áp bảng tinh_thanh_khu_vuc (chính thức) qua mã tỉnh EAV: vá được {} / {} đối tượng",
                coverage.get("so_va_duoc"), missingSummary.get("tong_thieu"));

        List<Map<String, Object>> byProvince = jdbcTemplate.queryForList("""
                SELECT t.ma, t.ten, t.la_tinh_cu, kv.ten AS ten_khu_vuc, COUNT(DISTINCT d.id) AS so_luong
                FROM hop_dong_doi_tuong d
                JOIN hop_dong_doi_tuong_gia_tri g ON g.hop_dong_doi_tuong_id = d.id AND g.ngay_xoa IS NULL
                JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.ngay_xoa IS NULL
                JOIN tinh_thanh t ON LOWER(t.ma) = LOWER(TRIM(g.gia_tri))
                JOIN tinh_thanh_khu_vuc tkv ON tkv.tinh_thanh_id = t.id AND tkv.ngay_xoa IS NULL AND tkv.hoat_dong = TRUE
                JOIN khu_vuc kv ON kv.id = tkv.khu_vuc_id
                WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND d.khu_vuc_id IS NULL
                  AND (LOWER(COALESCE(tt.ten,'')) LIKE '%tinh%' OR LOWER(COALESCE(tt.ten,'')) LIKE '%tỉnh%' OR LOWER(COALESCE(tt.ten,'')) LIKE '%province%')
                  AND LOWER(COALESCE(tt.ten,'')) NOT LIKE '%cu%' AND LOWER(COALESCE(tt.ten,'')) NOT LIKE '%cũ%'
                GROUP BY t.ma, t.ten, t.la_tinh_cu, kv.ten
                ORDER BY so_luong DESC
                """);
        log.info("[KhuVucDiag] === Chi tiết theo tỉnh (mã, tên, là tỉnh cũ?, khu vực áp được, số đối tượng vá) ===");
        for (Map<String, Object> row : byProvince) {
            log.info("[KhuVucDiag] {} ({}) laTinhCu={} -> {}  [{} đối tượng]",
                    row.get("ma"), row.get("ten"), row.get("la_tinh_cu"), row.get("ten_khu_vuc"), row.get("so_luong"));
        }

        List<Map<String, Object>> stillUnmatched = jdbcTemplate.queryForList("""
                SELECT LOWER(TRIM(g.gia_tri)) AS ma_tinh_eav, COUNT(DISTINCT d.id) AS cnt
                FROM hop_dong_doi_tuong d
                JOIN hop_dong_doi_tuong_gia_tri g ON g.hop_dong_doi_tuong_id = d.id AND g.ngay_xoa IS NULL
                JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.ngay_xoa IS NULL
                WHERE d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND d.khu_vuc_id IS NULL
                  AND (LOWER(COALESCE(tt.ten,'')) LIKE '%tinh%' OR LOWER(COALESCE(tt.ten,'')) LIKE '%tỉnh%' OR LOWER(COALESCE(tt.ten,'')) LIKE '%province%')
                  AND LOWER(COALESCE(tt.ten,'')) NOT LIKE '%cu%' AND LOWER(COALESCE(tt.ten,'')) NOT LIKE '%cũ%'
                  AND g.gia_tri IS NOT NULL AND TRIM(g.gia_tri) <> ''
                  AND NOT EXISTS (
                        SELECT 1 FROM tinh_thanh t
                        JOIN tinh_thanh_khu_vuc tkv ON tkv.tinh_thanh_id = t.id AND tkv.ngay_xoa IS NULL AND tkv.hoat_dong = TRUE
                        WHERE LOWER(t.ma) = LOWER(TRIM(g.gia_tri))
                      )
                GROUP BY LOWER(TRIM(g.gia_tri))
                ORDER BY cnt DESC
                LIMIT 20
                """);
        log.info("[KhuVucDiag] === Mã tỉnh EAV VẪN không vá được (top 20) — cần xem tay ===");
        for (Map<String, Object> row : stillUnmatched) {
            log.info("[KhuVucDiag] '{}' — {} đối tượng", row.get("ma_tinh_eav"), row.get("cnt"));
        }
        log.info("[KhuVucDiag] === HẾT — chưa update gì, chỉ đọc để review ===");
    }
}
