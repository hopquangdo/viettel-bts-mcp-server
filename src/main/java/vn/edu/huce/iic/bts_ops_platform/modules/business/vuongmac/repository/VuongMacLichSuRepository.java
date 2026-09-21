package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.entity.VuongMacLichSu;

import java.util.List;
import java.util.UUID;

public interface VuongMacLichSuRepository extends JpaRepository<VuongMacLichSu, UUID> {

    List<VuongMacLichSu> findByVuongMacIdOrderByNgayTaoDesc(UUID vuongMacId);
}
