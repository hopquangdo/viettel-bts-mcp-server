package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.PhanQuyenQuyenHan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.PhanQuyenQuyenHanId;

import java.util.List;

public interface PhanQuyenQuyenHanRepository extends JpaRepository<PhanQuyenQuyenHan, PhanQuyenQuyenHanId> {

    @Query("""
            SELECT p FROM PhanQuyenQuyenHan p
            WHERE p.id.quyenId = :quyenMa
              AND p.ngayXoa IS NULL
              AND p.hoatDong = TRUE
            """)
    List<PhanQuyenQuyenHan> findActiveByQuyenMa(@Param("quyenMa") String quyenMa);

    List<PhanQuyenQuyenHan> findByNgayXoaIsNull();

    /** Mọi dòng (kể cả đã tắt/xóa mềm) của 1 vai trò — dùng cho đồng bộ thay-cả-bộ. */
    List<PhanQuyenQuyenHan> findByIdQuyenId(String quyenId);
}
