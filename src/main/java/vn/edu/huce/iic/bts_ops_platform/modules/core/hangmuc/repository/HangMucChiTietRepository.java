package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucChiTiet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HangMucChiTietRepository extends JpaRepository<HangMucChiTiet, UUID> {

    List<HangMucChiTiet> findByNgayXoaIsNull();

    Optional<HangMucChiTiet> findByIdAndNgayXoaIsNull(UUID id);

    Optional<HangMucChiTiet> findByMaIgnoreCaseAndNgayXoaIsNull(String ma);

    boolean existsByMaIgnoreCase(String ma);
    boolean existsByMaIgnoreCaseAndIdNot(String ma, UUID id);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
            FROM HangMucChiTiet c
            JOIN HangMucNhom g ON c.hangMucNhomId = g.id
            WHERE LOWER(c.ma) = LOWER(:ma)
              AND g.hopDongId = :hopDongId
              AND c.ngayXoa IS NULL
              AND g.ngayXoa IS NULL
            """)
    boolean existsByMaIgnoreCaseAndHopDongIdAndNgayXoaIsNull(
            @Param("ma") String ma,
            @Param("hopDongId") UUID hopDongId);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
            FROM HangMucChiTiet c
            JOIN HangMucNhom g ON c.hangMucNhomId = g.id
            WHERE LOWER(c.ma) = LOWER(:ma)
              AND g.hopDongId = :hopDongId
              AND c.id <> :id
              AND c.ngayXoa IS NULL
              AND g.ngayXoa IS NULL
            """)
    boolean existsByMaIgnoreCaseAndHopDongIdAndIdNotAndNgayXoaIsNull(
            @Param("ma") String ma,
            @Param("hopDongId") UUID hopDongId,
            @Param("id") UUID id);

    List<HangMucChiTiet> findByHangMucNhomIdAndNgayXoaIsNull(UUID hangMucNhomId);

    List<HangMucChiTiet> findByHangMucNhomIdInAndNgayXoaIsNull(List<UUID> hangMucNhomIds);

    @Query("""
            SELECT c FROM HangMucChiTiet c
            JOIN HangMucNhom g ON c.hangMucNhomId = g.id
            WHERE LOWER(c.ma) = LOWER(:ma)
              AND g.hopDongId = :hopDongId
              AND c.ngayXoa IS NULL
              AND g.ngayXoa IS NULL
            """)
    Optional<HangMucChiTiet> findByMaIgnoreCaseAndHopDongIdAndNgayXoaIsNull(
            @Param("ma") String ma,
            @Param("hopDongId") UUID hopDongId);

    @Query("""
            SELECT LOWER(c.ma) FROM HangMucChiTiet c
            JOIN HangMucNhom g ON c.hangMucNhomId = g.id
            WHERE g.hopDongId = :hopDongId AND c.ma IS NOT NULL
            """)
    List<String> findAllMaLowerByHopDongId(@Param("hopDongId") UUID hopDongId);

    @Query("""
            SELECT c FROM HangMucChiTiet c
            WHERE LOWER(c.ma) = LOWER(:ma)
            ORDER BY CASE WHEN c.ngayXoa IS NULL THEN 0 ELSE 1 END, c.ngayCapNhat DESC
            """)
    List<HangMucChiTiet> findAllByMaIgnoreCaseOrderByActiveFirst(@Param("ma") String ma);
}
