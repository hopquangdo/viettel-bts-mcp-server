package vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.entity.ThongBao;

import java.util.List;
import java.util.UUID;

public interface ThongBaoRecipientRepository extends Repository<ThongBao, UUID> {

    @Query(value = """
            SELECT DISTINCT nd.id
            FROM nguoi_dung nd
            INNER JOIN quyen q ON nd.quyen_id = q.id AND q.ngay_xoa IS NULL
            INNER JOIN phan_quyen_quyen_han pq
                ON pq.quyen_id = q.ma AND pq.ngay_xoa IS NULL AND pq.hoat_dong = TRUE
            INNER JOIN quyen_han qh ON qh.id = pq.quyen_han_id AND qh.ma = :quyenHanMa
            WHERE nd.ngay_xoa IS NULL
            """, nativeQuery = true)
    List<UUID> findUserIdsByQuyenHanMa(@Param("quyenHanMa") String quyenHanMa);
}
