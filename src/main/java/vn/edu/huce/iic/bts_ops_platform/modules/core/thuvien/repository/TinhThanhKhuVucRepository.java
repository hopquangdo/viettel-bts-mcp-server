package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.TinhThanhKhuVuc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TinhThanhKhuVucRepository extends JpaRepository<TinhThanhKhuVuc, UUID> {

    long countByKhuVucId(UUID khuVucId);

    Optional<TinhThanhKhuVuc> findByTinhThanhId(UUID tinhThanhId);

    List<TinhThanhKhuVuc> findByKhuVucId(UUID khuVucId);
}
