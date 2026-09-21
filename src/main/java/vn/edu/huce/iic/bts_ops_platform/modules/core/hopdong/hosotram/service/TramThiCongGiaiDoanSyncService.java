package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.repository.BienBanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.repository.BienBanPhatSinhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TramThiCongGiaiDoanSyncService {

    private final HopDongDoiTuongRepository hopDongDoiTuongRepository;
    private final BienBanRepository bienBanRepository;
    private final BienBanPhatSinhRepository bienBanPhatSinhRepository;
    private final TramThiCongGiaiDoanResolver giaiDoanResolver;

    @Transactional
    public String syncForDoiTuong(UUID hopDongId, UUID doiTuongId) {
        if (hopDongId == null || doiTuongId == null) {
            return null;
        }
        String giaiDoan = giaiDoanResolver.resolve(
                doiTuongId,
                bienBanRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDongId),
                bienBanPhatSinhRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDongId));
        hopDongDoiTuongRepository.findById(doiTuongId)
                .filter(entity -> hopDongId.equals(entity.getHopDongId()) && !entity.isDeleted())
                .ifPresent(entity -> {
                    entity.setGiaiDoanThiCongTen(giaiDoan);
                    hopDongDoiTuongRepository.save(entity);
                });
        return giaiDoan;
    }

    @Transactional
    public void syncForDoiTuongIds(UUID hopDongId, List<UUID> doiTuongIds) {
        if (doiTuongIds == null) {
            return;
        }
        doiTuongIds.stream().distinct().forEach(id -> syncForDoiTuong(hopDongId, id));
    }
}
