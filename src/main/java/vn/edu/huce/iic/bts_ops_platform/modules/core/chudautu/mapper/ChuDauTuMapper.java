package vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.huce.iic.bts_ops_platform.common.mapper.EntityMapperConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.request.ChuDauTuCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.request.ChuDauTuTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.dto.response.ChuDauTuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.chudautu.entity.ChuDauTu;

@Mapper(config = EntityMapperConfig.class)
public interface ChuDauTuMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    ChuDauTu fromTaoRequest(ChuDauTuTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromCapNhatRequest(ChuDauTuCapNhatRequest request, @MappingTarget ChuDauTu entity);

    ChuDauTuResponse toResponse(ChuDauTu entity);
}
