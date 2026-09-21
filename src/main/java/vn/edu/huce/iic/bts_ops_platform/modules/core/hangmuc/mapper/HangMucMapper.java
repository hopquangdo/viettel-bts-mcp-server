package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.huce.iic.bts_ops_platform.common.mapper.EntityMapperConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucChiTietCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucChiTietTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucCongViecCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucCongViecTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucNhomCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.request.HangMucNhomTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucChiTietTreeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucCongViecResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.response.HangMucNhomTreeResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucChiTiet;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucCongViec;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity.HangMucNhom;

@Mapper(config = EntityMapperConfig.class)
public interface HangMucMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    HangMucNhom fromHangMucNhomTaoRequest(HangMucNhomTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromHangMucNhomCapNhatRequest(HangMucNhomCapNhatRequest request, @MappingTarget HangMucNhom entity);

    HangMucNhomResponse toHangMucNhomResponse(HangMucNhom entity);

    @Mapping(target = "chiTiet", ignore = true)
    HangMucNhomTreeResponse toHangMucNhomTreeResponse(HangMucNhom entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    HangMucChiTiet fromHangMucChiTietTaoRequest(HangMucChiTietTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromHangMucChiTietCapNhatRequest(HangMucChiTietCapNhatRequest request, @MappingTarget HangMucChiTiet entity);

    HangMucChiTietResponse toHangMucChiTietResponse(HangMucChiTiet entity);

    @Mapping(target = "congViec", ignore = true)
    HangMucChiTietTreeResponse toHangMucChiTietTreeResponse(HangMucChiTiet entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    @Mapping(target = "thuTu", ignore = true)
    HangMucCongViec fromHangMucCongViecTaoRequest(HangMucCongViecTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromHangMucCongViecCapNhatRequest(HangMucCongViecCapNhatRequest request, @MappingTarget HangMucCongViec entity);

    HangMucCongViecResponse toHangMucCongViecResponse(HangMucCongViec entity);

}
