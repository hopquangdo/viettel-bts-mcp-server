package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.entity.PhanCongLichSu;

import java.util.Collection;
import java.util.UUID;

public interface PhanCongLichSuRepository extends JpaRepository<PhanCongLichSu, UUID> {

    Page<PhanCongLichSu> findByHopDongDoiTuongIdOrderByNgayTaoDesc(UUID hopDongDoiTuongId, Pageable pageable);

    @Query("""
            SELECT l FROM PhanCongLichSu l
            WHERE (:hopDongId IS NULL OR l.hopDongId = :hopDongId)
              AND (:hopDongDoiTuongId IS NULL OR l.hopDongDoiTuongId = :hopDongDoiTuongId)
              AND (:nhaThauId IS NULL OR l.nhaThauMoiId = :nhaThauId OR l.nhaThauCuId = :nhaThauId)
            ORDER BY l.ngayTao DESC
            """)
    Page<PhanCongLichSu> searchHistory(
            @Param("hopDongId") UUID hopDongId,
            @Param("hopDongDoiTuongId") UUID hopDongDoiTuongId,
            @Param("nhaThauId") UUID nhaThauId,
            Pageable pageable);

    @Query("""
            SELECT l FROM PhanCongLichSu l
            WHERE l.hopDongDoiTuongId IN :ids
            ORDER BY l.ngayTao DESC
            """)
    Page<PhanCongLichSu> findByHopDongDoiTuongIdInOrderByNgayTaoDesc(
            @Param("ids") Collection<UUID> ids,
            Pageable pageable);
}
