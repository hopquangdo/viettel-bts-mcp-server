package vn.edu.huce.iic.bts_ops_platform.modules.core.file.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.huce.iic.bts_ops_platform.common.mapper.EntityMapperConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.request.TepDinhKemCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.request.TepDinhKemTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.response.TepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.entity.TepDinhKem;

@Mapper(config = EntityMapperConfig.class)
public interface FileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    TepDinhKem fromTepDinhKemTaoRequest(TepDinhKemTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromTepDinhKemCapNhatRequest(TepDinhKemCapNhatRequest request, @MappingTarget TepDinhKem entity);

    TepDinhKemResponse toTepDinhKemResponse(TepDinhKem entity);

}
