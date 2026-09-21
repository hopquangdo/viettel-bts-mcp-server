package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LienKetHopDong;

import java.util.List;
import java.util.UUID;

public interface LienKetHopDongRepository extends JpaRepository<LienKetHopDong, UUID> {

    List<LienKetHopDong> findByNgayXoaIsNull();

    @Query("""
            SELECT e FROM LienKetHopDong e
            WHERE e.ngayXoa IS NULL
              AND (
                    :keyword = ''
                    OR LOWER(CONCAT(
                        COALESCE(e.tenKieuNguon, ''), ' ',
                        COALESCE(e.tenKieuDich, '')
                    )) LIKE CONCAT('%', :keyword, '%')
                  )
            ORDER BY e.loaiLienKet ASC, e.tenKieuNguon ASC
            """)
    List<LienKetHopDong> search(@Param("keyword") String keyword);
}
