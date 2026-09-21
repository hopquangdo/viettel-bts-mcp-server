package vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.entity.DanhMucDuLieuTrongYeu;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DanhMucDuLieuTrongYeuRepository extends JpaRepository<DanhMucDuLieuTrongYeu, UUID> {

    List<DanhMucDuLieuTrongYeu> findByNgayXoaIsNullOrderByMaAsc();

    Optional<DanhMucDuLieuTrongYeu> findByMaAndNgayXoaIsNull(String ma);
}
