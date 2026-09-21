package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.entity.HoSoTramTepDinhKem;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface HoSoTramTepDinhKemRepository extends JpaRepository<HoSoTramTepDinhKem, UUID> {

    List<HoSoTramTepDinhKem> findByHopDongDoiTuongIdAndNgayXoaIsNullAndHoatDongTrueOrderByNgayTaoDesc(
            UUID hopDongDoiTuongId);

    /** Nạp theo lô cho nhiều trạm cùng lúc — dùng khi "Xuất hồ sơ đầy đủ theo trạm". */
    List<HoSoTramTepDinhKem> findByHopDongDoiTuongIdInAndNgayXoaIsNullAndHoatDongTrue(
            Collection<UUID> hopDongDoiTuongIds);
}
