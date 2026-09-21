package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucNhomService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;

import java.util.List;
import java.util.UUID;

/**
 * Tool một-lần, TẮT MẶC ĐỊNH — recompute HopDong.tongThanhTienThiCong + HopDongDoiTuong.sanLuongHieuLuc cho TOÀN BỘ hợp đồng
 * (không chỉ id NULL, khác với HopDongThanhTienBackfillRunner). Dùng khi cần vá lại giá trị sau
 * khi dữ liệu nguồn thay đổi hàng loạt (vd sau scripts/backfill_ngay_thuc_hien.sql).
 *
 * <p>Bật bằng: {@code APP_TOOLS_RECOMPUTE_TONG_THANH_TIEN=true} rồi khởi động app 1 lần — xong
 * việc thì TẮT LẠI (bỏ biến môi trường / set false), không để bật thường trực vì phải tính lại
 * cây hạng mục cho mọi hợp đồng mỗi lần khởi động sẽ tốn thời gian không cần thiết.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.tools.recompute-tong-thanh-tien", havingValue = "true")
public class TongThanhTienRecomputeTool implements CommandLineRunner {

    private final HopDongRepository hopDongRepository;
    private final HangMucNhomService hangMucNhomService;
    private final HopDongDoiTuongService hopDongDoiTuongService;

    @Override
    public void run(String... args) {
        // Vá luôn san_luong_hieu_luc TOÀN BỘ (1 câu UPDATE, rẻ) — cột này cùng cảnh denormalize
        // theo SanLuongChangedEvent, môi trường không có Kafka bị bỏ đói y hệt tong_thanh_tien.
        int patched = hopDongDoiTuongService.recalculateSanLuongHieuLucAll();
        log.info("[TongThanhTienRecomputeTool] recalculated san_luong_hieu_luc for {} hop_dong_doi_tuong", patched);

        List<UUID> ids = hopDongRepository.findByNgayXoaIsNull().stream().map(HopDong::getId).toList();
        if (ids.isEmpty()) {
            return;
        }
        log.info("[TongThanhTienRecomputeTool] recomputing tong_thanh_tien_thi_cong for {} hop_dong", ids.size());
        int failed = 0;
        for (UUID hopDongId : ids) {
            try {
                hangMucNhomService.recomputeTongThanhTienThiCong(hopDongId);
            } catch (Exception e) {
                failed++;
                log.warn("[TongThanhTienRecomputeTool] failed hopDongId={}", hopDongId, e);
            }
        }
        log.info("[TongThanhTienRecomputeTool] done — {} ok, {} failed", ids.size() - failed, failed);
    }
}
