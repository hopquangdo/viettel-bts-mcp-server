package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.entity.SanLuongAnh;

import java.util.List;
import java.util.UUID;

public interface SanLuongAnhRepository extends JpaRepository<SanLuongAnh, UUID> {

    List<SanLuongAnh> findByHopDongDoiTuongIdAndNgayXoaIsNullOrderByNgayTaoDesc(UUID hopDongDoiTuongId);
}
