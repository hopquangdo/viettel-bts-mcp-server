package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucNhom;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HangMucNhomRepository extends JpaRepository<HangMucNhom, UUID> {

    List<HangMucNhom> findByNgayXoaIsNull();

    Optional<HangMucNhom> findByIdAndNgayXoaIsNull(UUID id);

    Optional<HangMucNhom> findByMaIgnoreCaseAndNgayXoaIsNull(String ma);

    Optional<HangMucNhom> findByMaIgnoreCaseAndHopDongIdAndNgayXoaIsNull(String ma, UUID hopDongId);

    boolean existsByMaIgnoreCase(String ma);
    boolean existsByMaIgnoreCaseAndIdNot(String ma, UUID id);

    boolean existsByMaIgnoreCaseAndHopDongIdAndNgayXoaIsNull(String ma, UUID hopDongId);

    boolean existsByMaIgnoreCaseAndHopDongIdAndIdNotAndNgayXoaIsNull(String ma, UUID hopDongId, UUID id);

    List<HangMucNhom> findByHopDongIdAndNgayXoaIsNull(UUID hopDongId);

    List<HangMucNhom> findByHopDongIdInAndNgayXoaIsNull(Collection<UUID> hopDongIds);

    @Query("SELECT LOWER(g.ma) FROM HangMucNhom g WHERE g.ma IS NOT NULL")
    List<String> findAllMaLower();

    @Query("SELECT LOWER(g.ma) FROM HangMucNhom g WHERE g.hopDongId = :hopDongId AND g.ma IS NOT NULL")
    List<String> findAllMaLowerByHopDongId(@Param("hopDongId") UUID hopDongId);

    @Query("""
            SELECT g FROM HangMucNhom g
            WHERE LOWER(g.ma) = LOWER(:ma)
            ORDER BY CASE WHEN g.ngayXoa IS NULL THEN 0 ELSE 1 END, g.ngayCapNhat DESC
            """)
    List<HangMucNhom> findAllByMaIgnoreCaseOrderByActiveFirst(@Param("ma") String ma);
}
