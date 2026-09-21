package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.entity.BienBan;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BienBanRepository extends JpaRepository<BienBan, UUID> {

    List<BienBan> findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(UUID hopDongId);

    long countByHopDongIdAndLoaiBienBanAndNgayXoaIsNull(UUID hopDongId, String loaiBienBan);

    List<BienBan> findByIdInAndNgayXoaIsNull(List<UUID> ids);

    /** Nạp theo lô cho nhiều hợp đồng cùng lúc — dùng để tổng hợp số liệu biên bản trên danh sách hợp đồng. */
    List<BienBan> findByHopDongIdInAndLoaiBienBanAndNgayXoaIsNull(Collection<UUID> hopDongIds, String loaiBienBan);
}
