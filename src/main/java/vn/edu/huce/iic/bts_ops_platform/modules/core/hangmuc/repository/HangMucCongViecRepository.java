package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucCongViec;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HangMucCongViecRepository extends JpaRepository<HangMucCongViec, UUID> {

    List<HangMucCongViec> findByNgayXoaIsNull();

    Optional<HangMucCongViec> findByIdAndNgayXoaIsNull(UUID id);

    Optional<HangMucCongViec> findByMaIgnoreCaseAndNgayXoaIsNull(String ma);

    boolean existsByMaIgnoreCase(String ma);
    boolean existsByMaIgnoreCaseAndIdNot(String ma, UUID id);

    @Query("""
            SELECT CASE WHEN COUNT(cv) > 0 THEN TRUE ELSE FALSE END
            FROM HangMucCongViec cv
            JOIN HangMucChiTiet c ON cv.hangMucChiTietId = c.id
            JOIN HangMucNhom g ON c.hangMucNhomId = g.id
            WHERE LOWER(cv.ma) = LOWER(:ma)
              AND g.hopDongId = :hopDongId
              AND cv.ngayXoa IS NULL
              AND c.ngayXoa IS NULL
              AND g.ngayXoa IS NULL
            """)
    boolean existsByMaIgnoreCaseAndHopDongIdAndNgayXoaIsNull(
            @Param("ma") String ma,
            @Param("hopDongId") UUID hopDongId);

    @Query("""
            SELECT CASE WHEN COUNT(cv) > 0 THEN TRUE ELSE FALSE END
            FROM HangMucCongViec cv
            JOIN HangMucChiTiet c ON cv.hangMucChiTietId = c.id
            JOIN HangMucNhom g ON c.hangMucNhomId = g.id
            WHERE LOWER(cv.ma) = LOWER(:ma)
              AND g.hopDongId = :hopDongId
              AND cv.id <> :id
              AND cv.ngayXoa IS NULL
              AND c.ngayXoa IS NULL
              AND g.ngayXoa IS NULL
            """)
    boolean existsByMaIgnoreCaseAndHopDongIdAndIdNotAndNgayXoaIsNull(
            @Param("ma") String ma,
            @Param("hopDongId") UUID hopDongId,
            @Param("id") UUID id);

    List<HangMucCongViec> findByHangMucChiTietIdAndNgayXoaIsNull(UUID hangMucChiTietId);

    List<HangMucCongViec> findByHangMucChiTietIdInAndNgayXoaIsNull(List<UUID> hangMucChiTietIds);

    @Query("""
            SELECT cv FROM HangMucCongViec cv
            JOIN HangMucChiTiet c ON cv.hangMucChiTietId = c.id
            JOIN HangMucNhom g ON c.hangMucNhomId = g.id
            WHERE LOWER(cv.ma) = LOWER(:ma)
              AND g.hopDongId = :hopDongId
              AND cv.ngayXoa IS NULL
              AND c.ngayXoa IS NULL
              AND g.ngayXoa IS NULL
            """)
    Optional<HangMucCongViec> findByMaIgnoreCaseAndHopDongIdAndNgayXoaIsNull(
            @Param("ma") String ma,
            @Param("hopDongId") UUID hopDongId);

    @Query("""
            SELECT LOWER(cv.ma) FROM HangMucCongViec cv
            JOIN HangMucChiTiet c ON cv.hangMucChiTietId = c.id
            JOIN HangMucNhom g ON c.hangMucNhomId = g.id
            WHERE g.hopDongId = :hopDongId AND cv.ma IS NOT NULL
            """)
    List<String> findAllMaLowerByHopDongId(@Param("hopDongId") UUID hopDongId);

    @Query("""
            SELECT cv FROM HangMucCongViec cv
            JOIN HangMucChiTiet c ON cv.hangMucChiTietId = c.id AND c.ngayXoa IS NULL
            JOIN HangMucNhom g ON c.hangMucNhomId = g.id AND g.ngayXoa IS NULL
            WHERE g.hopDongId = :hopDongId
              AND g.hoatDong = TRUE
              AND c.hoatDong = TRUE
              AND cv.ngayXoa IS NULL
            ORDER BY g.thuTu ASC, cv.thuTu ASC
            """)
    List<HangMucCongViec> findActiveByHopDongId(@Param("hopDongId") UUID hopDongId);

    @Query("""
            SELECT g.hopDongId, cv FROM HangMucCongViec cv
            JOIN HangMucChiTiet c ON cv.hangMucChiTietId = c.id AND c.ngayXoa IS NULL
            JOIN HangMucNhom g ON c.hangMucNhomId = g.id AND g.ngayXoa IS NULL
            WHERE g.hopDongId IN :hopDongIds
              AND g.hoatDong = TRUE
              AND c.hoatDong = TRUE
              AND cv.ngayXoa IS NULL
            ORDER BY g.hopDongId ASC, g.thuTu ASC, cv.thuTu ASC
            """)
    List<Object[]> findActivePairsByHopDongIds(@Param("hopDongIds") Collection<UUID> hopDongIds);
}
