package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ThuocTinhHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.CauHinhListQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ThuocTinhHopDongRepository extends JpaRepository<ThuocTinhHopDong, UUID> {

    List<ThuocTinhHopDong> findByNgayXoaIsNull();

    Optional<ThuocTinhHopDong> findByIdAndNgayXoaIsNull(UUID id);

    List<ThuocTinhHopDong> findByIdInAndNgayXoaIsNull(List<UUID> ids);

    @Query("""
            SELECT e FROM ThuocTinhHopDong e
            WHERE """
            + CauHinhListQuery.NOT_DELETED + """
             AND """
            + CauHinhListQuery.ACTIVE_ONLY + """
              AND (
                    :keyword = ''
                    OR LOWER(CONCAT(
                        COALESCE(e.ten, ''), ' ',
                        COALESCE(e.kieuDuLieuId, ''), ' ',
                        COALESCE(e.donVi, '')
                    )) LIKE CONCAT('%', :keyword, '%')
                  )
            ORDER BY e.ngayTao DESC
            """)
    List<ThuocTinhHopDong> search(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("keyword") String keyword);
}
