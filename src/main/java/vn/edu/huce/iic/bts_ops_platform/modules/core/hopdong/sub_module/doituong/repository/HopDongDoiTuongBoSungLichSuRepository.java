package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongBoSungLichSu;

import java.util.List;
import java.util.UUID;

public interface HopDongDoiTuongBoSungLichSuRepository extends JpaRepository<HopDongDoiTuongBoSungLichSu, UUID> {

    List<HopDongDoiTuongBoSungLichSu> findByHopDongDoiTuongIdAndNgayXoaIsNullOrderByNgayTaoDesc(
            UUID hopDongDoiTuongId);
}
