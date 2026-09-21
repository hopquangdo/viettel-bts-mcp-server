package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.huce.iic.bts_ops_platform.common.mapper.EntityMapperConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenHanCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenHanTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.request.QuyenTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.QuyenHanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.dto.response.QuyenResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.Quyen;
import vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity.QuyenHan;

@Mapper(config = EntityMapperConfig.class)
public interface PhanQuyenMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    Quyen fromQuyenTaoRequest(QuyenTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromQuyenCapNhatRequest(QuyenCapNhatRequest request, @MappingTarget Quyen entity);

    QuyenResponse toQuyenResponse(Quyen entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    QuyenHan fromQuyenHanTaoRequest(QuyenHanTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromQuyenHanCapNhatRequest(QuyenHanCapNhatRequest request, @MappingTarget QuyenHan entity);

    QuyenHanResponse toQuyenHanResponse(QuyenHan entity);

}
