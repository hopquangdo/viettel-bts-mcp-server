package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongThuocTinh;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HopDongThuocTinhRepository extends JpaRepository<HopDongThuocTinh, UUID> {

    List<HopDongThuocTinh> findByNgayXoaIsNull();

    Optional<HopDongThuocTinh> findByIdAndNgayXoaIsNull(UUID id);

    List<HopDongThuocTinh> findByHopDongIdAndNgayXoaIsNull(UUID hopDongId);

    List<HopDongThuocTinh> findByHopDongIdInAndNgayXoaIsNull(Collection<UUID> hopDongIds);

    @Query("""
            SELECT ht, attr.ten
            FROM HopDongThuocTinh ht
            LEFT JOIN ThuocTinhHopDong attr ON attr.id = ht.thuocTinhHopDongId AND attr.ngayXoa IS NULL
            WHERE ht.hopDongId IN :hopDongIds
              AND ht.ngayXoa IS NULL
            ORDER BY ht.hopDongId ASC, ht.thuTu ASC
            """)
    List<Object[]> findActiveWithTenByHopDongIds(@Param("hopDongIds") Collection<UUID> hopDongIds);
}
