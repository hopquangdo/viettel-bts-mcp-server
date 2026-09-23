package vn.edu.huce.iic.bts_ops_platform.mcp.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface SoftDeleteRepository<T> extends JpaRepository<T, UUID> {

    List<T> findByNgayXoaIsNull();

    boolean existsByIdAndNgayXoaIsNull(UUID id);
}
