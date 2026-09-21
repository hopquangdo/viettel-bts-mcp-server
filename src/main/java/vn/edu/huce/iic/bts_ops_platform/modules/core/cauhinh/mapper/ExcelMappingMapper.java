package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.huce.iic.bts_ops_platform.common.mapper.EntityMapperConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ExcelMappingCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ExcelMappingDongBoItemRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ExcelMappingTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ExcelMappingCotResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ExcelMappingResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ExcelMapping;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ExcelMappingCot;

@Mapper(config = EntityMapperConfig.class)
public interface ExcelMappingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    @Mapping(target = "nguoiCapNhat", ignore = true)
    ExcelMapping fromTaoRequest(ExcelMappingTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    @Mapping(target = "nguoiCapNhat", ignore = true)
    void updateFromCapNhatRequest(ExcelMappingCapNhatRequest request, @MappingTarget ExcelMapping entity);

    ExcelMappingResponse toResponse(ExcelMapping entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    ExcelMappingCot fromDongBoItem(ExcelMappingDongBoItemRequest request);

    ExcelMappingCotResponse toCotResponse(ExcelMappingCot entity);
}
