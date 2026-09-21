package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DanhMucBienBan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers.CauHinhListQuery;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DanhMucBienBanRepository extends JpaRepository<DanhMucBienBan, UUID> {

    List<DanhMucBienBan> findByNgayXoaIsNullOrderByThuTuMacDinhAsc();

    List<DanhMucBienBan> findByHoatDongTrueAndNgayXoaIsNullOrderByThuTuMacDinhAsc();

    Optional<DanhMucBienBan> findByMaAndNgayXoaIsNull(String ma);

    Optional<DanhMucBienBan> findByIdAndNgayXoaIsNull(UUID id);

    List<DanhMucBienBan> findByIdInAndNgayXoaIsNull(Collection<UUID> ids);

    boolean existsByMaIgnoreCase(String ma);

    boolean existsByMaIgnoreCaseAndIdNot(String ma, UUID id);

    @Query("""
            SELECT e FROM DanhMucBienBan e
            WHERE """
            + CauHinhListQuery.NOT_DELETED + """
             AND """
            + CauHinhListQuery.ACTIVE_ONLY + """
              AND (
                    :keyword = ''
                    OR LOWER(CONCAT(COALESCE(e.ma, ''), ' ', COALESCE(e.ten, ''))) LIKE CONCAT('%', :keyword, '%')
                  )
            ORDER BY e.thuTuMacDinh ASC, e.ngayTao ASC
            """)
    List<DanhMucBienBan> search(
            @Param("includeDeleted") boolean includeDeleted,
            @Param("activeOnly") Boolean activeOnly,
            @Param("keyword") String keyword);
}
