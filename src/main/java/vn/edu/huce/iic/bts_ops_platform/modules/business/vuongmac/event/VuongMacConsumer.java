package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.event;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.huce.iic.bts_ops_platform.config.AsyncEventConfig;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDeletedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDoiTuongCascadeDeleteEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDoiTuongCoVuongMacChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDoiTuongMetaChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongMetaChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.SnapshotDataMissingEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.VuongMacChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.cache.SanLuongCacheKeys;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.cache.SanLuongCacheNames;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.entity.VuongMac;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.helpers.VuongMacKieuHelper;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.repository.VuongMacRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.services.VuongMacService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongSnapshotService;

import java.util.UUID;

/**
 * All event listeners the vuongmac module owns — one class per module (see hopdong/event,
 * sanluong/event equivalents). Keeps 2 things in sync:
 * <ol>
 *   <li>Soft-deletes VuongMac records that belonged to a deleted hợp đồng.</li>
 *   <li>The VuongMacService row cache (1 entry/vướng mắc, xem VuongMacService.refreshRow) —
 *       làm mới ngay khi chính vướng mắc đó đổi, hoặc khi mã/tỉnh/khu vực/nhà thầu của đối
 *       tượng/hợp đồng liên quan đổi.</li>
 * </ol>
 */
@Slf4j
@Component("vuongMacHopDongDeletedConsumer")
@RequiredArgsConstructor
public class VuongMacConsumer {

    private final VuongMacRepository vuongMacRepository;
    private final VuongMacService vuongMacService;
    private final HopDongDoiTuongSnapshotService hopDongDoiTuongSnapshotService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CacheService cacheService;

    @KafkaListener(topics = "${app.kafka-topics.hop-dong-deleted}", groupId = "bts-ops-platform-vuongmac-hopdong-deleted")
    @Transactional
    public void onHopDongDeleted(HopDongDeletedEvent event) {
        if (event.hopDongId() == null) {
            return;
        }
        Instant now = Instant.now();
        for (VuongMac issue : vuongMacRepository.findByHopDongIdAndNgayXoaIsNull(event.hopDongId())) {
            issue.setNgayXoa(now);
            issue.setHoatDong(false);
            vuongMacRepository.save(issue);
        }
        log.debug("vuongmac cleaned up after hopDongId={} deletion", event.hopDongId());
    }

    /** Chính vướng mắc này vừa tạo/sửa/xóa — làm mới ngay cache row của nó + patch snapshot. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onVuongMacChanged(VuongMacChangedEvent event) {
        if (event.vuongMacId() != null) {
            try {
                vuongMacService.refreshRow(event.vuongMacId());
            } catch (Exception ex) {
                log.warn("failed to refresh vuongmac row cache for id={}", event.vuongMacId(), ex);
            }
        }
        applyVuongMacSnapshotSlice(event.hopDongDoiTuongId());
        evictSanLuongTongHopCache(event.hopDongId());
        try {
            vuongMacService.evictTongQuanCache();
        } catch (Exception ex) {
            log.warn("failed to evict vuongmac stats cache", ex);
        }
    }

    /**
     * SanLuongTongHopResponse.issueCount giờ đọc từ vuong_mac (xem
     * VuongMacService.demMoTrongGiaiDoan) — nên cache TONG_HOP của module sanluong cũng phải
     * bị evict khi vướng mắc đổi, không chỉ khi sản lượng đổi (SanLuongConsumer).
     */
    private void evictSanLuongTongHopCache(UUID hopDongId) {
        try {
            if (hopDongId != null) {
                cacheService.evictByPrefix(
                        SanLuongCacheNames.TONG_HOP, SanLuongCacheKeys.tongHopPrefixForHopDong(hopDongId));
            }
            cacheService.evictByPrefix(
                    SanLuongCacheNames.TONG_HOP, SanLuongCacheKeys.tongHopPrefixForHopDong(null));
        } catch (Exception ex) {
            log.warn("failed to evict sanluong-tonghop cache for hopDongId={}", hopDongId, ex);
        }
    }

    /**
     * Core phát sự kiện này khi nó phải trả snapshot chỉ có nhóm tĩnh (cache miss) — tự
     * đếm lại vướng mắc đang mở rồi patch vào, không đợi VuongMacChangedEvent kế tiếp.
     *
     * <p>@Async bắt buộc — request GET đang phục vụ (nguồn kích hoạt cache-miss) không được
     * chặn chờ việc tự vá cache nền này. Trước đây thiếu @Async, listener chạy đồng bộ ngay
     * trong triggerAfterCompletion() của chính transaction request gốc, khiến response bị
     * treo thêm hàng chục giây (đặc biệt rõ khi DB ở xa qua mạng, mỗi query bên trong tốn
     * round-trip thật).
     */
    @Async(AsyncEventConfig.SNAPSHOT_REPAIR_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSnapshotDataMissing(SnapshotDataMissingEvent event) {
        applyVuongMacSnapshotSlice(event.hopDongDoiTuongId());
    }

    /** Hợp đồng đối tượng bị xóa hàng loạt (theo id hoặc theo filter) — cascade soft-delete vướng mắc. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHopDongDoiTuongCascadeDelete(HopDongDoiTuongCascadeDeleteEvent event) {
        if (event.hopDongDoiTuongIds() == null || event.hopDongDoiTuongIds().isEmpty()) {
            return;
        }
        vuongMacRepository.softDeleteByDuLieuDoiTuongIds(event.hopDongDoiTuongIds(), Instant.now());
    }

    private void applyVuongMacSnapshotSlice(UUID hopDongDoiTuongId) {
        if (hopDongDoiTuongId == null) {
            return;
        }
        try {
            boolean coVuongMacMo = vuongMacRepository.countBlockingOpenByDuLieuDoiTuongId(
                    hopDongDoiTuongId) > 0;
            int soVuongMac = (int) vuongMacRepository.countOpenByDuLieuDoiTuongId(hopDongDoiTuongId);
            hopDongDoiTuongSnapshotService.applyVuongMacFields(hopDongDoiTuongId, soVuongMac);
            applicationEventPublisher.publishEvent(
                    new HopDongDoiTuongCoVuongMacChangedEvent(hopDongDoiTuongId, coVuongMacMo));
        } catch (Exception ex) {
            log.warn("failed to apply vuongmac snapshot slice for hopDongDoiTuongId={}", hopDongDoiTuongId, ex);
        }
    }

    /** Mã/tỉnh/khu vực/nhà thầu của đối tượng đổi — làm mới cache mọi vướng mắc gắn với nó. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHopDongDoiTuongMetaChanged(HopDongDoiTuongMetaChangedEvent event) {
        if (event.hopDongDoiTuongId() == null) {
            return;
        }
        try {
            vuongMacService.refreshRowsByHopDongDoiTuongId(event.hopDongDoiTuongId());
        } catch (Exception ex) {
            log.warn("failed to refresh vuongmac rows for hopDongDoiTuongId={}", event.hopDongDoiTuongId(), ex);
        }
    }

    /** Tên/loại hợp đồng đổi — chỉ evict (không rẻ để recompute eager mọi vướng mắc của hợp đồng). */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHopDongMetaChanged(HopDongMetaChangedEvent event) {
        if (event.hopDongId() == null) {
            return;
        }
        try {
            vuongMacService.evictRowsByHopDongId(event.hopDongId());
        } catch (Exception ex) {
            log.warn("failed to evict vuongmac rows for hopDongId={}", event.hopDongId(), ex);
        }
    }
}
