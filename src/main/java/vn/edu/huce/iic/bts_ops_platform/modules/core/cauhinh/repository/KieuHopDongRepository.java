package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.CauHinhListQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KieuHopDongRepository extends JpaRepository<KieuHopDong, UUID> {

    List<KieuHopDong> findByNgayXoaIsNull();

    Optional<KieuHopDong> findByIdAndNgayXoaIsNull(UUID id);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, UUID id);

    List<KieuHopDong> findByLoaiHopDongIdAndNgayXoaIsNull(UUID loaiHopDongId);

    @Query("""
            SELECT e FROM KieuHopDong e
            WHERE """
            + CauHinhListQuery.NOT_DELETED + """
             AND """
            + CauHinhListQuery.ACTIVE_ONLY + """
              AND (:loaiHopDongId IS NULL OR e.loaiHopDongId = :loaiHopDongId)
              AND (
                    :keyword = ''
                    OR LOWER(CONCAT(
                        COALESCE(e.ma, ''), ' ',
                        COALESCE(e.ten, ''), ' ',
                        COALESCE(e.nhom, ''), ' ',
                        COALESCE(e.mauSac, ''), ' ',
                        COALESCE(e.moTa, '')
                    )) LIKE CONCAT('%', :keyword, '%')
                  )
            ORDER BY e.ngayTao DESC
            """)
    List<KieuHopDong> search(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("loaiHopDongId") UUID loaiHopDongId,
            @Param("keyword") String keyword);
}
