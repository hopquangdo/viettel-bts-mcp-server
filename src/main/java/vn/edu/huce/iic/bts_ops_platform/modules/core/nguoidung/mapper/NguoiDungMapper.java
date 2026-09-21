package vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.huce.iic.bts_ops_platform.common.mapper.EntityMapperConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.request.TaoTaiKhoanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.dto.response.NguoiDungResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.nguoidung.entity.NguoiDung;

@Mapper(config = EntityMapperConfig.class)
public interface NguoiDungMapper {

    @Mapping(target = "hoatDong", expression = "java(Boolean.TRUE.equals(entity.getHoatDong()))")
    NguoiDungResponse toNguoiDungResponse(NguoiDung entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    @Mapping(target = "matKhau", ignore = true)
    @Mapping(target = "sso", ignore = true)
    NguoiDung fromTaoTaiKhoanRequest(TaoTaiKhoanRequest request);
}
