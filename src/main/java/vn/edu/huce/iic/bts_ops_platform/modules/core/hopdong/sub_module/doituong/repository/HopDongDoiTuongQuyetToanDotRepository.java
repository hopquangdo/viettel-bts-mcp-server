package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongQuyetToanDot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HopDongDoiTuongQuyetToanDotRepository extends JpaRepository<HopDongDoiTuongQuyetToanDot, UUID> {

    List<HopDongDoiTuongQuyetToanDot> findByHopDongDoiTuongIdAndNgayXoaIsNullAndHoatDongTrueOrderByNgayQuyetToanDescNgayTaoDesc(
            UUID hopDongDoiTuongId);

    Optional<HopDongDoiTuongQuyetToanDot> findByIdAndNgayXoaIsNull(UUID id);
}
