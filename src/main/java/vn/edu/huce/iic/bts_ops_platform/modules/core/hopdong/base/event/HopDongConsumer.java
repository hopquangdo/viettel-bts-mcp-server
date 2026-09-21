package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.huce.iic.bts_ops_platform.common.event.GeoMetaChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDeletedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongDoiTuongMetaChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.common.event.HopDongMetaChangedEvent;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongNhomUuTien;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongTepDinhKem;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongNhomUuTienService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongTepDinhKemService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongThuocTinhService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongGiaTri;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongGiaTriService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongSnapshotService;

import java.time.Instant;
import java.util.UUID;

/**
 * All event listeners the hopdong module owns — one class per module (see sanluong/event,
 * vuongmac/event equivalents). Keeps 2 things in sync:
 * <ol>
 *   <li>The STATIC half of the shared HopDongDoiTuongSnapshot cache (mã/tỉnh/khu vực/nhà
 *       thầu, tên/loại hợp đồng — see HopDongDoiTuongSnapshotService.getSnapshots), whenever
 *       any of those fields' source data changes. The dynamic half (sản lượng/vướng mắc) is
 *       owned and patched in by business.sanluong.event.SanLuongConsumer /
 *       business.vuongmac.event.VuongMacConsumer instead — core never calls into business to
 *       compute it.</li>
 *   <li>Cascade soft-delete of HopDongDoiTuong/GiaTri/ThuocTinh/NhomUuTien/TepDinhKem when a
 *       hợp đồng is deleted — moved out of HopDongServiceImpl.delete() into onHopDongDeleted
 *       below so the cascade runs the same event-driven way every other module's cascade
 *       already does (see HangMucConsumer/SanLuongConsumer/VuongMacConsumer/PhanCongConsumer),
 *       instead of inline in the same transaction.</li>
 * </ol>
 *
 * <p><b>Scope note:</b> the 3 snapshot events below are, for now, consumed only via Spring's in-JVM
 * ApplicationEventPublisher — on a multi-instance deployment other instances won't see them
 * until the snapshot cache TTL expires. Wire Kafka topics for them (mirroring
 * KafkaTopicConfig/AppKafkaTopicsProperties) if that gap matters.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HopDongConsumer {

    private final HopDongDoiTuongSnapshotService hopDongDoiTuongSnapshotService;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final HopDongDoiTuongGiaTriService hopDongDoiTuongGiaTriService;
    private final HopDongThuocTinhService hopDongThuocTinhService;
    private final HopDongNhomUuTienService hopDongNhomUuTienService;
    private final HopDongTepDinhKemService hopDongTepDinhKemService;

    /**
     * Cascade soft-delete HopDongDoiTuong/GiaTri/ThuocTinh/NhomUuTien/TepDinhKem khi hợp đồng
     * bị xóa — chạy qua Kafka (như HangMucConsumer/SanLuongConsumer/VuongMacConsumer/
     * PhanCongConsumer) thay vì HopDongServiceImpl.delete() tự làm inline trong cùng transaction.
     */
    @KafkaListener(topics = "${app.kafka-topics.hop-dong-deleted}", groupId = "bts-ops-platform-hopdong-cascade-hopdong-deleted")
    @Transactional
    public void onHopDongDeleted(HopDongDeletedEvent event) {
        if (event.hopDongId() == null) {
            return;
        }
        Instant now = Instant.now();
        for (HopDongDoiTuong doiTuong : hopDongDoiTuongService.findActiveEntitiesByHopDongId(event.hopDongId())) {
            softDeleteDoiTuongChildren(doiTuong.getId(), now);
            doiTuong.setNgayXoa(now);
            doiTuong.setHoatDong(false);
            hopDongDoiTuongService.saveEntity(doiTuong);
        }

        hopDongThuocTinhService.findActiveEntitiesByHopDongId(event.hopDongId()).forEach(item -> {
            item.setNgayXoa(now);
            item.setHoatDong(false);
            hopDongThuocTinhService.saveEntity(item);
        });

        for (HopDongNhomUuTien group : hopDongNhomUuTienService.findActiveEntitiesByHopDongId(event.hopDongId())) {
            group.setNgayXoa(now);
            hopDongNhomUuTienService.saveEntity(group);
        }

        for (HopDongTepDinhKem attachment : hopDongTepDinhKemService.findActiveEntitiesByHopDongId(event.hopDongId())) {
            attachment.setNgayXoa(now);
            attachment.setHoatDong(false);
            hopDongTepDinhKemService.saveEntity(attachment);
        }
        log.debug("hopdong children cascaded after hopDongId={} deletion", event.hopDongId());
    }

    private void softDeleteDoiTuongChildren(UUID hopDongDoiTuongId, Instant now) {
        for (HopDongDoiTuongGiaTri value : hopDongDoiTuongGiaTriService
                .findActiveEntitiesByHopDongDoiTuongId(hopDongDoiTuongId)) {
            value.setNgayXoa(now);
            value.setHoatDong(false);
            hopDongDoiTuongGiaTriService.saveEntity(value);
        }
    }

    /** Mã/tỉnh/khu vực/nhà thầu của riêng đối tượng này đổi — làm mới nhóm tĩnh ngay. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHopDongDoiTuongMetaChanged(HopDongDoiTuongMetaChangedEvent event) {
        refreshStatic(event.hopDongDoiTuongId());
    }

    /**
     * Tên/loại hợp đồng đổi — ảnh hưởng mọi đối tượng thuộc hợp đồng này. Chỉ evict
     * (không recompute eager) vì danh sách id không rẻ để lấy ở đây; lần đọc tiếp theo
     * của mỗi đối tượng sẽ tự tính lại (cache miss).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHopDongMetaChanged(HopDongMetaChangedEvent event) {
        if (event.hopDongId() == null) {
            return;
        }
        try {
            hopDongDoiTuongSnapshotService.evictSnapshots(event.hopDongId());
        } catch (Exception ex) {
            log.warn("failed to evict snapshot cache for hopDongId={}", event.hopDongId(), ex);
        }
    }

    /** Tên/mã tỉnh hoặc khu vực đổi — ảnh hưởng không xác định phạm vi, evict toàn bộ cache. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onGeoMetaChanged(GeoMetaChangedEvent event) {
        try {
            hopDongDoiTuongSnapshotService.evictSnapshots(null);
        } catch (Exception ex) {
            log.warn("failed to evict snapshot cache after geo meta change", ex);
        }
    }

    private void refreshStatic(UUID hopDongDoiTuongId) {
        if (hopDongDoiTuongId == null) {
            return;
        }
        try {
            hopDongDoiTuongSnapshotService.refreshStaticFields(hopDongDoiTuongId);
        } catch (Exception ex) {
            log.warn("failed to refresh snapshot static fields for hopDongDoiTuongId={}", hopDongDoiTuongId, ex);
        }
    }
}
