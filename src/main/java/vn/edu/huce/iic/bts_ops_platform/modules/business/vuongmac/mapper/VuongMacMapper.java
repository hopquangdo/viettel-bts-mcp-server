package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.huce.iic.bts_ops_platform.common.mapper.EntityMapperConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request.VuongMacCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.request.VuongMacTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.dto.response.VuongMacResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.entity.VuongMac;

@Mapper(config = EntityMapperConfig.class)
public interface VuongMacMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    VuongMac fromVuongMacTaoRequest(VuongMacTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromVuongMacCapNhatRequest(VuongMacCapNhatRequest request, @MappingTarget VuongMac entity);

    VuongMacResponse toVuongMacResponse(VuongMac entity);

}
