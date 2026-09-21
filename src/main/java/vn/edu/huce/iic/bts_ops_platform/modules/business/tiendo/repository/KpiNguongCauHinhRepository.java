package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity.KpiNguongCauHinh;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KpiNguongCauHinhRepository extends JpaRepository<KpiNguongCauHinh, UUID> {

    List<KpiNguongCauHinh> findByHoatDongTrueOrderByUuTienAscMaAsc();

    Optional<KpiNguongCauHinh> findByMa(String ma);

    Optional<KpiNguongCauHinh> findByIdAndHoatDongTrue(UUID id);
}
