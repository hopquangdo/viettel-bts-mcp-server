package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LuongTrangThai;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.CauHinhListQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LuongTrangThaiRepository extends JpaRepository<LuongTrangThai, UUID> {

    List<LuongTrangThai> findByNgayXoaIsNull();

    Optional<LuongTrangThai> findByIdAndNgayXoaIsNull(UUID id);

    @Query("""
            SELECT e FROM LuongTrangThai e
            WHERE """
            + CauHinhListQuery.NOT_DELETED + """
             AND """
            + CauHinhListQuery.ACTIVE_ONLY + """
              AND (
                    :keyword = ''
                    OR LOWER(CONCAT(
                        COALESCE(e.ten, ''), ' ',
                        COALESCE(e.moTa, '')
                    )) LIKE CONCAT('%', :keyword, '%')
                  )
            ORDER BY e.ngayTao DESC
            """)
    List<LuongTrangThai> search(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("keyword") String keyword);
}
