package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LuongTrangThaiBuoc;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LuongTrangThaiBuocId;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LuongTrangThaiBuocRepository extends JpaRepository<LuongTrangThaiBuoc, LuongTrangThaiBuocId> {

    List<LuongTrangThaiBuoc> findById_LuongTrangThaiIdOrderByThuTuAsc(UUID luongTrangThaiId);

    List<LuongTrangThaiBuoc> findById_LuongTrangThaiIdInOrderByThuTuAsc(Collection<UUID> luongTrangThaiIds);

    void deleteById_LuongTrangThaiId(UUID luongTrangThaiId);
}
