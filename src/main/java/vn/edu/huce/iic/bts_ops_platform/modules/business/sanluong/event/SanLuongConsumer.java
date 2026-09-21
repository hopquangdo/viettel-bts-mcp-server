package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.huce.iic.bts_ops_platform.config.AsyncEventConfig;
import vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongCanceledEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDeletedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDoiTuongCascadeDeleteEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.SanLuongChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.SnapshotDataMissingEvent;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.cache.SanLuongCacheKeys;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.cache.SanLuongCacheNames;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongProgressResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.entity.SanLuong;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.repository.SanLuongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.services.SanLuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongSnapshotService;

/** All Kafka listeners the sanluong module owns — one class per module (see hopdong/event equivalent). */
@Slf4j
@Component
@RequiredArgsConstructor
public class SanLuongConsumer {

    private final SanLuongRepository sanLuongRepository;
    private final CacheService cacheService;
    private final SanLuongService sanLuongService;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongDoiTuongSnapshotService hopDongDoiTuongSnapshotService;

    /**
     * Cleans up SanLuong records that belonged to a deleted hợp đồng. Replaces the
     * direct SanLuongRepository access hopdong used to perform itself.
     */
    @KafkaListener(topics = "${app.kafka-topics.hop-dong-deleted}", groupId = "bts-ops-platform-sanluong-hopdong-deleted")
    @Transactional
    public void onHopDongDeleted(HopDongDeletedEvent event) {
        if (event.hopDongId() == null) {
            return;
        }
        Instant now = Instant.now();
        for (SanLuong record : sanLuongRepository.findByHopDongIdAndNgayXoaIsNull(event.hopDongId())) {
            record.setNgayXoa(now);
            record.setHoatDong(false);
            sanLuongRepository.save(record);
        }
        log.debug("sanluong cleaned up after hopDongId={} deletion", event.hopDongId());
    }

    /**
     * Cập nhật đồng bộ trên cùng JVM — chạy sau khi transaction ghi SanLuong đã commit.
     * Dùng REQUIRES_NEW để tạo transaction riêng, tránh không thể commit vì transaction gốc
     * đã kết thúc ở AFTER_COMMIT phase.
     */
    @Async(AsyncEventConfig.SNAPSHOT_REPAIR_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSanLuongChangedLocal(SanLuongChangedEvent event) {
        warmSanLuongTouch(event);
    }

    /**
     * Dự phòng qua Kafka — đảm bảo các instance khác trong cluster cũng cập nhật đúng
     * (chạy trên nhiều instance, tại thời điểm giao dịch gốc đã chắc chắn commit).
     */
    @KafkaListener(topics = "${app.kafka-topics.sanluong-changed}", groupId = "bts-ops-platform-sanluong-cache")
    public void onSanLuongChangedKafka(SanLuongChangedEvent event) {
        warmSanLuongTouch(event);
    }

    /**
     * Core phát sự kiện này khi nó phải trả snapshot chỉ có nhóm tĩnh (cache miss) — tự
     * tính lại nhóm động thuộc sở hữu sanluong rồi patch vào, không đợi SanLuongChangedEvent
     * kế tiếp mới có dữ liệu đúng.
     *
     * <p>@Async — xem javadoc VuongMacConsumer.onSnapshotDataMissing: request gốc kích hoạt
     * cache-miss không được chặn chờ việc tự vá cache nền này.
     */
    @Async(AsyncEventConfig.SNAPSHOT_REPAIR_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSnapshotDataMissing(SnapshotDataMissingEvent event) {
        applySanLuongSnapshotSlice(event.hopDongDoiTuongId());
    }

    /** Hợp đồng bị hủy — deactivate sản lượng liên quan (không soft-delete, giữ ngayXoa=null). */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHopDongCanceled(HopDongCanceledEvent event) {
        if (event.hopDongId() == null) {
            return;
        }
        for (SanLuong record : sanLuongRepository.findByHopDongIdAndNgayXoaIsNull(event.hopDongId())) {
            record.setHoatDong(false);
            sanLuongRepository.save(record);
        }
    }

    /** Hợp đồng đối tượng bị xóa hàng loạt (theo id hoặc theo filter) — cascade soft-delete sản lượng. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHopDongDoiTuongCascadeDelete(HopDongDoiTuongCascadeDeleteEvent event) {
        if (event.hopDongDoiTuongIds() == null || event.hopDongDoiTuongIds().isEmpty()) {
            return;
        }
        sanLuongRepository.softDeleteByHopDongDoiTuongIds(event.hopDongDoiTuongIds(), Instant.now());
    }

    private void warmSanLuongTouch(SanLuongChangedEvent event) {
        if (event.hopDongId() == null) {
            return;
        }
        log.debug("invalidating sanluong-tonghop cache for hopDongId={}", event.hopDongId());
        cacheService.evictByPrefix(
                SanLuongCacheNames.TONG_HOP, SanLuongCacheKeys.tongHopPrefixForHopDong(event.hopDongId()));

        if (event.hopDongDoiTuongId() == null) {
            return;
        }
        try {
            cacheService.evictByPrefix(SanLuongCacheNames.DOI_TUONG_ROW, event.hopDongDoiTuongId().toString());
            sanLuongService.recomputeAndCacheDoiTuongRow(event.hopDongDoiTuongId(), null);
            sanLuongService.recomputeAndCacheTongHop(event.hopDongId());
            hopDongDoiTuongService.recalculateConstructionDate(event.hopDongDoiTuongId());
            applySanLuongSnapshotSlice(event.hopDongDoiTuongId());
            log.debug("cache warmed for hopDongDoiTuongId={} hopDongId={}",
                    event.hopDongDoiTuongId(), event.hopDongId());
        } catch (Exception ex) {
            log.warn("failed to warm cache for hopDongDoiTuongId={}", event.hopDongDoiTuongId(), ex);
        }
    }

    /** Tính nhóm động thuộc sở hữu sanluong cho 1 đối tượng rồi patch vào snapshot cache của core. */
    private void applySanLuongSnapshotSlice(UUID hopDongDoiTuongId) {
        if (hopDongDoiTuongId == null) {
            return;
        }
        try {
            SanLuongProgressResponse progress = sanLuongService.getProgress(hopDongDoiTuongId);
            BigDecimal tongThanhTienSanLuong = sanLuongService
                    .tongThanhTienTheoDoiTuongIds(List.of(hopDongDoiTuongId))
                    .getOrDefault(hopDongDoiTuongId, BigDecimal.ZERO);
            LocalDate constructionDate = hopDongDoiTuongService.getByIds(List.of(hopDongDoiTuongId))
                    .stream()
                    .findFirst()
                    .map(HopDongDoiTuongResponse::getConstructionDate)
                    .orElse(null);

            hopDongDoiTuongSnapshotService.applySanLuongFields(
                    hopDongDoiTuongId,
                    progress.tongThanhTien(),
                    tongThanhTienSanLuong,
                    progress.itemsDone(),
                    progress.itemsTotal(),
                    constructionDate
            );
        } catch (Exception ex) {
            log.warn("failed to apply sanluong snapshot slice for hopDongDoiTuongId={}", hopDongDoiTuongId, ex);
        }
    }
}
