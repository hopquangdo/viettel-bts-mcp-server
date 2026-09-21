package vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.modules.business.realtime.services.RealtimeEventService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.entity.ThongBao;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.repository.ThongBaoRecipientRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.repository.ThongBaoRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.services.ThongBaoDispatchService;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ThongBaoDispatchServiceImpl implements ThongBaoDispatchService {

    private final ThongBaoRepository thongBaoRepository;
    private final ThongBaoRecipientRepository recipientRepository;
    private final RealtimeEventService realtimeEventService;

    @Override
    @Transactional
    public ThongBao notifyUser(
            UUID nguoiNhanId,
            String loai,
            String tieuDe,
            String noiDung,
            String lienKet,
            UUID thamChieuId) {
        if (nguoiNhanId == null) {
            return null;
        }
        ThongBao saved = persist(nguoiNhanId, loai, tieuDe, noiDung, lienKet, thamChieuId);
        realtimeEventService.pushNotification(nguoiNhanId, saved.getId(), loai);
        return saved;
    }

    @Override
    @Transactional
    public void notifyUsers(
            Collection<UUID> nguoiNhanIds,
            String loai,
            String tieuDe,
            String noiDung,
            String lienKet,
            UUID thamChieuId) {
        if (nguoiNhanIds == null || nguoiNhanIds.isEmpty()) {
            return;
        }
        Set<UUID> unique = new LinkedHashSet<>(nguoiNhanIds);
        for (UUID userId : unique) {
            notifyUser(userId, loai, tieuDe, noiDung, lienKet, thamChieuId);
        }
    }

    @Override
    @Transactional
    public void notifyUsersWithQuyenHan(
            String quyenHanMa,
            String loai,
            String tieuDe,
            String noiDung,
            String lienKet,
            UUID thamChieuId) {
        notifyUsers(recipientRepository.findUserIdsByQuyenHanMa(quyenHanMa), loai, tieuDe, noiDung, lienKet, thamChieuId);
    }

    private ThongBao persist(
            UUID nguoiNhanId,
            String loai,
            String tieuDe,
            String noiDung,
            String lienKet,
            UUID thamChieuId) {
        ThongBao entity = new ThongBao();
        entity.setNguoiNhanId(nguoiNhanId);
        entity.setLoai(loai);
        entity.setTieuDe(tieuDe);
        entity.setNoiDung(noiDung);
        entity.setLienKet(lienKet);
        entity.setThamChieuId(thamChieuId);
        entity.setDaDoc(false);
        entity.setNgayTao(Instant.now());
        return thongBaoRepository.save(entity);
    }
}
