package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.CauHinhListQuery;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoaiHopDongRepository extends JpaRepository<LoaiHopDong, UUID> {

    List<LoaiHopDong> findByNgayXoaIsNull();

    Optional<LoaiHopDong> findByIdAndNgayXoaIsNull(UUID id);

    List<LoaiHopDong> findByIdInAndNgayXoaIsNull(Collection<UUID> ids);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, UUID id);

    @Query("""
            SELECT e FROM LoaiHopDong e
            WHERE """
            + CauHinhListQuery.NOT_DELETED + """
             AND """
            + CauHinhListQuery.ACTIVE_ONLY + """
              AND (
                    :keyword = ''
                    OR LOWER(CONCAT(
                        COALESCE(e.ma, ''), ' ',
                        COALESCE(e.ten, ''), ' ',
                        COALESCE(e.moTa, '')
                    )) LIKE CONCAT('%', :keyword, '%')
                  )
            ORDER BY e.ngayTao DESC
            """)
    List<LoaiHopDong> search(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("keyword") String keyword);
}
