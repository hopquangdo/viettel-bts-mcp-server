package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ExcelMappingCot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExcelMappingCotRepository extends JpaRepository<ExcelMappingCot, UUID> {

    List<ExcelMappingCot> findByExcelMappingIdAndNgayXoaIsNullOrderByThuTuAsc(UUID excelMappingId);

    Optional<ExcelMappingCot> findByIdAndNgayXoaIsNull(UUID id);
}
