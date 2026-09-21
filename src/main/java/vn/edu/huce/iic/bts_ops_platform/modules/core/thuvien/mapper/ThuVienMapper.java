package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.huce.iic.bts_ops_platform.common.mapper.EntityMapperConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.KhuVucCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.KhuVucTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.request.TinhThanhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.KhuVucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.dto.response.TinhThanhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.KhuVuc;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.TinhThanh;

@Mapper(config = EntityMapperConfig.class)
public interface ThuVienMapper {

    @Mapping(target = "soTinhThanh", ignore = true)
    KhuVucResponse toKhuVucResponse(KhuVuc entity);

    default KhuVucResponse toKhuVucResponse(KhuVuc entity, long soTinhThanh) {
        KhuVucResponse response = toKhuVucResponse(entity);
        response.setSoTinhThanh(soTinhThanh);
        return response;
    }

    @Mapping(target = "khuVucId", ignore = true)
    @Mapping(target = "tenKhuVuc", ignore = true)
    @Mapping(target = "hoatDong", expression = "java(Boolean.TRUE.equals(entity.getHoatDong()))")
    @Mapping(target = "laTinhCu", expression = "java(Boolean.TRUE.equals(entity.getLaTinhCu()))")
    TinhThanhResponse toTinhThanhResponse(TinhThanh entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    KhuVuc fromKhuVucTaoRequest(KhuVucTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    void updateFromKhuVucCapNhatRequest(KhuVucCapNhatRequest request, @MappingTarget KhuVuc entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "tinhThanhId", ignore = true)
    TinhThanh fromTinhThanhTaoRequest(TinhThanhTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "tinhThanhId", ignore = true)
    void updateFromTinhThanhCapNhatRequest(TinhThanhCapNhatRequest request, @MappingTarget TinhThanh entity);
}
