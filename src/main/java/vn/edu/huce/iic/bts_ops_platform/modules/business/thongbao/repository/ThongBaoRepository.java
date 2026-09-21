package vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.entity.ThongBao;

import java.time.Instant;
import java.util.UUID;

public interface ThongBaoRepository extends JpaRepository<ThongBao, UUID> {

    Page<ThongBao> findByNguoiNhanIdOrderByNgayTaoDesc(UUID nguoiNhanId, Pageable pageable);

    long countByNguoiNhanIdAndDaDocFalse(UUID nguoiNhanId);

    boolean existsByNguoiNhanIdAndLoaiAndThamChieuIdAndDaDocFalse(
            UUID nguoiNhanId, String loai, UUID thamChieuId);

    @Modifying
    @Query("""
            UPDATE ThongBao t SET t.daDoc = true, t.ngayDoc = :now
            WHERE t.nguoiNhanId = :nguoiNhanId AND t.daDoc = false
            """)
    int markAllRead(@Param("nguoiNhanId") UUID nguoiNhanId, @Param("now") Instant now);

    @Modifying
    @Query("""
            UPDATE ThongBao t SET t.daDoc = true, t.ngayDoc = :now
            WHERE t.id = :id AND t.nguoiNhanId = :nguoiNhanId AND t.daDoc = false
            """)
    int markRead(@Param("id") UUID id, @Param("nguoiNhanId") UUID nguoiNhanId, @Param("now") Instant now);
}
