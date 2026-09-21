package vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.entity.PhanAnhNguoiDung;

import java.util.List;
import java.util.UUID;

public interface PhanAnhNguoiDungRepository extends JpaRepository<PhanAnhNguoiDung, UUID> {

    List<PhanAnhNguoiDung> findByNguoiDungIdAndNgayXoaIsNullOrderByNgayTaoDesc(UUID nguoiDungId);

    List<PhanAnhNguoiDung> findByNgayXoaIsNullOrderByNgayTaoDesc();
}
