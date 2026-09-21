package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.huce.iic.bts_ops_platform.common.mapper.EntityMapperConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongGiaTriTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTrangThaiCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTrangThaiTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongNhomUuTienCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongNhomUuTienTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongTepDinhKemCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongTepDinhKemTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongThuocTinhCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongThuocTinhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongTrangThaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongNhomUuTienResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongTepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongGiaTri;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity.HopDongDoiTuongTrangThai;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongNhomUuTien;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongTepDinhKem;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDongThuocTinh;

@Mapper(config = EntityMapperConfig.class)
public interface HopDongMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    HopDong fromHopDongTaoRequest(HopDongTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromHopDongCapNhatRequest(HopDongCapNhatRequest request, @MappingTarget HopDong entity);

    HopDongResponse toHopDongResponse(HopDong entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    HopDongThuocTinh fromHopDongThuocTinhTaoRequest(HopDongThuocTinhTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromHopDongThuocTinhCapNhatRequest(HopDongThuocTinhCapNhatRequest request, @MappingTarget HopDongThuocTinh entity);

    HopDongThuocTinhResponse toHopDongThuocTinhResponse(HopDongThuocTinh entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    HopDongTepDinhKem fromHopDongTepDinhKemTaoRequest(HopDongTepDinhKemTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromHopDongTepDinhKemCapNhatRequest(HopDongTepDinhKemCapNhatRequest request, @MappingTarget HopDongTepDinhKem entity);

    HopDongTepDinhKemResponse toHopDongTepDinhKemResponse(HopDongTepDinhKem entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    HopDongNhomUuTien fromHopDongNhomUuTienTaoRequest(HopDongNhomUuTienTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromHopDongNhomUuTienCapNhatRequest(HopDongNhomUuTienCapNhatRequest request, @MappingTarget HopDongNhomUuTien entity);

    HopDongNhomUuTienResponse toHopDongNhomUuTienResponse(HopDongNhomUuTien entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    HopDongDoiTuong fromHopDongDoiTuongTaoRequest(HopDongDoiTuongTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromHopDongDoiTuongCapNhatRequest(HopDongDoiTuongCapNhatRequest request, @MappingTarget HopDongDoiTuong entity);

    @Mapping(source = "ngayThiCongGanNhat", target = "constructionDate")
    HopDongDoiTuongResponse toHopDongDoiTuongResponse(HopDongDoiTuong entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    HopDongDoiTuongGiaTri fromHopDongDoiTuongGiaTriTaoRequest(HopDongDoiTuongGiaTriTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromHopDongDoiTuongGiaTriCapNhatRequest(HopDongDoiTuongGiaTriCapNhatRequest request, @MappingTarget HopDongDoiTuongGiaTri entity);

    HopDongDoiTuongGiaTriResponse toHopDongDoiTuongGiaTriResponse(HopDongDoiTuongGiaTri entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    HopDongDoiTuongTrangThai fromHopDongDoiTuongTrangThaiTaoRequest(HopDongDoiTuongTrangThaiTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromHopDongDoiTuongTrangThaiCapNhatRequest(HopDongDoiTuongTrangThaiCapNhatRequest request, @MappingTarget HopDongDoiTuongTrangThai entity);

    HopDongDoiTuongTrangThaiResponse toHopDongDoiTuongTrangThaiResponse(HopDongDoiTuongTrangThai entity);

}
