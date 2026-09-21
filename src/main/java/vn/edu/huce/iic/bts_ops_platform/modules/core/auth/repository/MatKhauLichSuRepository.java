package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.MatKhauLichSu;

import java.util.List;
import java.util.UUID;

public interface MatKhauLichSuRepository extends JpaRepository<MatKhauLichSu, UUID> {

    List<MatKhauLichSu> findTop5ByNguoiDungIdOrderByNgayTaoDesc(UUID nguoiDungId);
}
