package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongTrangThai;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HopDongDoiTuongTrangThaiRepository extends JpaRepository<HopDongDoiTuongTrangThai, UUID> {

    List<HopDongDoiTuongTrangThai> findByNgayXoaIsNull();

    Optional<HopDongDoiTuongTrangThai> findByIdAndNgayXoaIsNull(UUID id);

    List<HopDongDoiTuongTrangThai> findByDoiTuongIdAndNgayXoaIsNull(UUID doiTuongId);

    List<HopDongDoiTuongTrangThai> findByKieuHopDongIdAndLoaiHopDongIdAndNgayXoaIsNull(
            UUID kieuHopDongId, UUID loaiHopDongId);

    List<HopDongDoiTuongTrangThai> findByKieuHopDongIdAndLoaiHopDongIdAndDoiTuongIdAndNgayXoaIsNullOrderByThuTuAsc(
            UUID kieuHopDongId, UUID loaiHopDongId, UUID doiTuongId);

    Optional<HopDongDoiTuongTrangThai> findByKieuHopDongIdAndLoaiHopDongIdAndDoiTuongIdAndTrangThaiIdAndNgayXoaIsNull(
            UUID kieuHopDongId, UUID loaiHopDongId, UUID doiTuongId, UUID trangThaiId);
}
