package vn.edu.huce.iic.bts_ops_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.huce.iic.bts_ops_platform.dto.doituong.DoiTuongInfoProjection;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;

import java.util.List;
import java.util.UUID;

/** Các danh mục nhỏ (loại hợp đồng, kiểu hợp đồng, bước trạng thái hợp đồng) — lấy hết 1 lần rồi khớp trong bộ nhớ. */
public interface DanhMucToolRepository extends JpaRepository<HopDong, UUID> {

    @Query(value = "SELECT id, ma, ten FROM loai_hop_dong WHERE ngay_xoa IS NULL", nativeQuery = true)
    List<DoiTuongInfoProjection> tatCaLoaiHopDong();

    @Query(value = "SELECT id, ma, ten FROM kieu_hop_dong WHERE ngay_xoa IS NULL", nativeQuery = true)
    List<DoiTuongInfoProjection> tatCaKieuHopDong();

    @Query(value = "SELECT id, ma, ten FROM trang_thai_hop_dong WHERE ngay_xoa IS NULL", nativeQuery = true)
    List<DoiTuongInfoProjection> tatCaTrangThaiHopDong();
}
