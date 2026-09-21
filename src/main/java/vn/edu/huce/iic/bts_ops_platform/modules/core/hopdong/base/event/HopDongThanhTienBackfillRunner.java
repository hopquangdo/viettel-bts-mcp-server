package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucNhomService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;

import java.util.List;
import java.util.UUID;

/**
 * Backfill 1 lần cho HopDong.tongThanhTienThiCong sau migration V006 (cột mới, NULL với các hợp
 * đồng đã tồn tại). Idempotent — chỉ xử lý các id còn NULL, nên an toàn để chạy lại mỗi lần khởi
 * động (sau lần đầu sẽ luôn no-op). Để recompute lại TOÀN BỘ (vd sau khi vá san_luong.ngay_thuc_hien),
 * dùng TongThanhTienRecomputeTool riêng (app.tools.recompute-tong-thanh-tien=true), không sửa ở đây.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HopDongThanhTienBackfillRunner implements CommandLineRunner {

    private final HopDongRepository hopDongRepository;
    private final HangMucNhomService hangMucNhomService;

    @Override
    public void run(String... args) {
        List<UUID> ids = hopDongRepository.findIdsMissingTongThanhTienThiCong();
        if (ids.isEmpty()) return;
        log.info("backfilling tong_thanh_tien_thi_cong for {} hop_dong", ids.size());
        for (UUID hopDongId : ids) {
            try {
                hangMucNhomService.recomputeTongThanhTienThiCong(hopDongId);
            } catch (Exception e) {
                log.warn("backfill tong_thanh_tien_thi_cong failed hopDongId={}", hopDongId, e);
            }
        }
        log.info("backfill tong_thanh_tien_thi_cong done");
    }
}
