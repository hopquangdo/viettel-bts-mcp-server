package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.huce.iic.bts_ops_platform.common.event.SanLuongChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.config.AsyncEventConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.services.HangMucNhomService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;

/**
 * Giữ 2 cột denormalize đồng bộ mỗi khi SanLuong đổi — core tự nghe event do business.sanluong
 * phát, KHÔNG để business gọi ngược vào service ghi của core (business chỉ công bố sự kiện, core
 * tự quyết định cách cập nhật cột của chính mình):
 * <ul>
 *   <li>HopDong.tongThanhTienThiCong — xem HangMucNhomServiceImpl.recomputeTongThanhTienThiCong.</li>
 *   <li>HopDongDoiTuong.sanLuongHieuLuc — xem HopDongDoiTuongService.recalculateSanLuongHieuLuc.</li>
 * </ul>
 *
 * <p>Nghe theo cả 2 đường giống SanLuongConsumer: listener local (cùng JVM, chạy cả khi KHÔNG có
 * Kafka broker — môi trường dev/single-instance) + Kafka (các instance khác trong cluster).
 * Recompute idempotent (tính lại từ nguồn) nên chạy trùng 2 đường vô hại.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HopDongThanhTienConsumer {

    private final HangMucNhomService hangMucNhomService;
    private final HopDongDoiTuongService hopDongDoiTuongService;

    /** Đường local — AFTER_COMMIT cùng JVM, không phụ thuộc Kafka (xem SanLuongConsumer.onSanLuongChangedLocal). */
    @Async(AsyncEventConfig.SNAPSHOT_REPAIR_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSanLuongChangedLocal(SanLuongChangedEvent event) {
        recompute(event);
    }

    @KafkaListener(topics = "${app.kafka-topics.sanluong-changed}", groupId = "bts-ops-platform-hopdong-thanhtien")
    public void onSanLuongChanged(SanLuongChangedEvent event) {
        recompute(event);
    }

    private void recompute(SanLuongChangedEvent event) {
        if (event.hopDongId() != null) {
            log.debug("recomputing tong_thanh_tien_thi_cong hopDongId={}", event.hopDongId());
            hangMucNhomService.recomputeTongThanhTienThiCong(event.hopDongId());
        }
        if (event.hopDongDoiTuongId() != null) {
            log.debug("recomputing san_luong_hieu_luc hopDongDoiTuongId={}", event.hopDongDoiTuongId());
            hopDongDoiTuongService.recalculateSanLuongHieuLuc(event.hopDongDoiTuongId());
        }
    }
}
