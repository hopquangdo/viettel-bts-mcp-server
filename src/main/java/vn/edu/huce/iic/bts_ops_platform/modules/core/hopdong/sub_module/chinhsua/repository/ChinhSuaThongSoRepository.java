package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.entity.ChinhSuaThongSo;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ChinhSuaThongSoRepository extends JpaRepository<ChinhSuaThongSo, UUID> {

    long countByHopDongDoiTuongIdAndThuocTinhIdAndTrangThaiAndNgayXoaIsNull(
            UUID hopDongDoiTuongId, UUID thuocTinhId, String trangThai);

    List<ChinhSuaThongSo> findByHopDongDoiTuongIdAndNgayXoaIsNullOrderByNgayTaoDesc(UUID hopDongDoiTuongId);

    Page<ChinhSuaThongSo> findByTrangThaiAndNgayXoaIsNullOrderByNgayTaoDesc(String trangThai, Pageable pageable);

    @Query("""
            SELECT c FROM ChinhSuaThongSo c
            WHERE c.ngayXoa IS NULL AND c.trangThai = :trangThai
              AND (:hopDongId IS NULL OR c.hopDongId = :hopDongId)
            ORDER BY c.ngayTao DESC
            """)
    Page<ChinhSuaThongSo> findChoDuyet(
            @Param("trangThai") String trangThai,
            @Param("hopDongId") UUID hopDongId,
            Pageable pageable);

    @Query("""
            SELECT c.hopDongDoiTuongId, c.thuocTinhId, COUNT(c)
            FROM ChinhSuaThongSo c
            WHERE c.ngayXoa IS NULL AND c.trangThai = 'da_ap_dung'
              AND c.hopDongDoiTuongId IN :doiTuongIds
            GROUP BY c.hopDongDoiTuongId, c.thuocTinhId
            """)
    List<Object[]> countDaApDungGroupByDoiTuongAndThuocTinh(@Param("doiTuongIds") Collection<UUID> doiTuongIds);

    boolean existsByHopDongDoiTuongIdAndThuocTinhIdAndTrangThaiAndNgayXoaIsNull(
            UUID hopDongDoiTuongId, UUID thuocTinhId, String trangThai);

    @Query("""
            SELECT c.hopDongDoiTuongId, MAX(c.ngayTao)
            FROM ChinhSuaThongSo c
            WHERE c.ngayXoa IS NULL
              AND c.trangThai <> 'tu_choi'
              AND c.hopDongDoiTuongId IN :doiTuongIds
            GROUP BY c.hopDongDoiTuongId
            """)
    List<Object[]> findLastChinhSuaAtByDoiTuongIds(@Param("doiTuongIds") Collection<UUID> doiTuongIds);
}
