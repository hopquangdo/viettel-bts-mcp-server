package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.huce.iic.bts_ops_platform.common.mapper.EntityMapperConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request.SanLuongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response.SanLuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.entity.SanLuong;

@Mapper(config = EntityMapperConfig.class)
public interface SanLuongMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    SanLuong fromSanLuongTaoRequest(SanLuongTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromSanLuongCapNhatRequest(SanLuongCapNhatRequest request, @MappingTarget SanLuong entity);

    SanLuongResponse toSanLuongResponse(SanLuong entity);

}
