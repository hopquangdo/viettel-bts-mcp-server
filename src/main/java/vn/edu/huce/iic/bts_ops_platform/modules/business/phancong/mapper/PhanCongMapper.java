package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.huce.iic.bts_ops_platform.common.mapper.EntityMapperConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.request.PhanCongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.request.PhanCongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response.PhanCongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.entity.PhanCong;

@Mapper(config = EntityMapperConfig.class)
public interface PhanCongMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    PhanCong fromTaoRequest(PhanCongTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromCapNhatRequest(PhanCongCapNhatRequest request, @MappingTarget PhanCong entity);

    PhanCongResponse toResponse(PhanCong entity);
}
