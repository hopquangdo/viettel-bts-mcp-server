package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Tool một-lần, TẮT MẶC ĐỊNH — backfill hop_dong_doi_tuong.khu_vuc_id (và tinh_thanh_id) cho các
 * đối tượng đang thiếu, dựa trên mã tỉnh trong thuộc tính động (EAV, vd 'hyn') tra qua bảng
 * ánh xạ chính thức tinh_thanh_khu_vuc — xem KhuVucBackfillDiagnosticTool (bản đọc-thử trước khi
 * chạy tool này, đã xác nhận vá được 11.378/21.940 đối tượng).
 *
 * <p>CHỈ update các đối tượng đang khu_vuc_id IS NULL — không đè giá trị đã có sẵn. Tra
 * tinh_thanh KHÔNG lọc ngay_xoa để vẫn khớp được cả tỉnh cũ đã sáp nhập.
 *
 * <p>Bật bằng: {@code APP_TOOLS_BACKFILL_KHU_VUC=true} rồi khởi động app 1 lần — xong việc thì
 * TẮT LẠI, không để bật thường trực.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.tools.backfill-khu-vuc", havingValue = "true")
public class KhuVucBackfillApplyTool implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("[KhuVucBackfillApplyTool] Bắt đầu backfill khu_vuc_id/tinh_thanh_id theo mã tỉnh EAV...");

        int updatedKhuVuc = jdbcTemplate.update("""
                UPDATE hop_dong_doi_tuong d
                SET khu_vuc_id = tkv.khu_vuc_id
                FROM hop_dong_doi_tuong_gia_tri g
                JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.ngay_xoa IS NULL
                JOIN tinh_thanh t ON LOWER(t.ma) = LOWER(TRIM(g.gia_tri))
                JOIN tinh_thanh_khu_vuc tkv ON tkv.tinh_thanh_id = t.id AND tkv.ngay_xoa IS NULL AND tkv.hoat_dong = TRUE
                WHERE g.hop_dong_doi_tuong_id = d.id
                  AND g.ngay_xoa IS NULL
                  AND (LOWER(COALESCE(tt.ten,'')) LIKE '%tinh%' OR LOWER(COALESCE(tt.ten,'')) LIKE '%tỉnh%' OR LOWER(COALESCE(tt.ten,'')) LIKE '%province%')
                  AND LOWER(COALESCE(tt.ten,'')) NOT LIKE '%cu%' AND LOWER(COALESCE(tt.ten,'')) NOT LIKE '%cũ%'
                  AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND d.khu_vuc_id IS NULL
                """);
        log.info("[KhuVucBackfillApplyTool] Đã vá khu_vuc_id cho {} đối tượng", updatedKhuVuc);

        int updatedTinh = jdbcTemplate.update("""
                UPDATE hop_dong_doi_tuong d
                SET tinh_thanh_id = t.id
                FROM hop_dong_doi_tuong_gia_tri g
                JOIN thuoc_tinh tt ON tt.id = g.thuoc_tinh_id AND tt.ngay_xoa IS NULL
                JOIN tinh_thanh t ON LOWER(t.ma) = LOWER(TRIM(g.gia_tri))
                WHERE g.hop_dong_doi_tuong_id = d.id
                  AND g.ngay_xoa IS NULL
                  AND (LOWER(COALESCE(tt.ten,'')) LIKE '%tinh%' OR LOWER(COALESCE(tt.ten,'')) LIKE '%tỉnh%' OR LOWER(COALESCE(tt.ten,'')) LIKE '%province%')
                  AND LOWER(COALESCE(tt.ten,'')) NOT LIKE '%cu%' AND LOWER(COALESCE(tt.ten,'')) NOT LIKE '%cũ%'
                  AND d.ngay_xoa IS NULL AND d.hoat_dong = TRUE AND d.tinh_thanh_id IS NULL
                """);
        log.info("[KhuVucBackfillApplyTool] Đã vá tinh_thanh_id cho {} đối tượng", updatedTinh);
        log.info("[KhuVucBackfillApplyTool] === HOÀN TẤT ===");
    }
}
