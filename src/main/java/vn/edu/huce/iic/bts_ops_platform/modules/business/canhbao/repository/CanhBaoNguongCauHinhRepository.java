package vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.entity.CanhBaoNguongCauHinh;

import java.util.Optional;
import java.util.UUID;

public interface CanhBaoNguongCauHinhRepository extends JpaRepository<CanhBaoNguongCauHinh, UUID> {

    Optional<CanhBaoNguongCauHinh> findByLoaiAndHoatDongTrue(String loai);
}
