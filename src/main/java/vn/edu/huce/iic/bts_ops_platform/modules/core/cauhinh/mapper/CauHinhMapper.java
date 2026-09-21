package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.edu.huce.iic.bts_ops_platform.common.mapper.EntityMapperConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DanhMucBienBanCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DanhMucBienBanTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DoiTuongQuanLyCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.DoiTuongQuanLyTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuDuLieuCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuDuLieuTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LoaiHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.LoaiHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.ThuocTinhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.TrangThaiHopDongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.TrangThaiHopDongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DanhMucBienBanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongQuanLyResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuDuLieuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LoaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.TrangThaiHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DanhMucBienBan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.DoiTuongQuanLy;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuDuLieu;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ThuocTinh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.ThuocTinhHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.TrangThaiHopDong;

@Mapper(config = EntityMapperConfig.class)
public interface CauHinhMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    LoaiHopDong fromLoaiHopDongTaoRequest(LoaiHopDongTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromLoaiHopDongCapNhatRequest(LoaiHopDongCapNhatRequest request, @MappingTarget LoaiHopDong entity);

    LoaiHopDongResponse toLoaiHopDongResponse(LoaiHopDong entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    KieuHopDong fromKieuHopDongTaoRequest(KieuHopDongTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromKieuHopDongCapNhatRequest(KieuHopDongCapNhatRequest request, @MappingTarget KieuHopDong entity);

    KieuHopDongResponse toKieuHopDongResponse(KieuHopDong entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    DoiTuongQuanLy fromDoiTuongQuanLyTaoRequest(DoiTuongQuanLyTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromDoiTuongQuanLyCapNhatRequest(DoiTuongQuanLyCapNhatRequest request, @MappingTarget DoiTuongQuanLy entity);

    DoiTuongQuanLyResponse toDoiTuongQuanLyResponse(DoiTuongQuanLy entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    TrangThaiHopDong fromTrangThaiHopDongTaoRequest(TrangThaiHopDongTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromTrangThaiHopDongCapNhatRequest(TrangThaiHopDongCapNhatRequest request, @MappingTarget TrangThaiHopDong entity);

    TrangThaiHopDongResponse toTrangThaiHopDongResponse(TrangThaiHopDong entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    KieuDuLieu fromKieuDuLieuTaoRequest(KieuDuLieuTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromKieuDuLieuCapNhatRequest(KieuDuLieuCapNhatRequest request, @MappingTarget KieuDuLieu entity);

    KieuDuLieuResponse toKieuDuLieuResponse(KieuDuLieu entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    ThuocTinh fromThuocTinhTaoRequest(ThuocTinhTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromThuocTinhCapNhatRequest(ThuocTinhCapNhatRequest request, @MappingTarget ThuocTinh entity);

    @Mapping(target = "lienKetBang", ignore = true)
    ThuocTinhResponse toThuocTinhResponse(ThuocTinh entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    ThuocTinhHopDong fromThuocTinhHopDongTaoRequest(ThuocTinhHopDongTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromThuocTinhHopDongCapNhatRequest(ThuocTinhHopDongCapNhatRequest request, @MappingTarget ThuocTinhHopDong entity);

    @Mapping(target = "lienKetBang", ignore = true)
    ThuocTinhHopDongResponse toThuocTinhHopDongResponse(ThuocTinhHopDong entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    DanhMucBienBan fromDanhMucBienBanTaoRequest(DanhMucBienBanTaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ngayTao", ignore = true)
    @Mapping(target = "ngayCapNhat", ignore = true)
    @Mapping(target = "ngayXoa", ignore = true)
    void updateFromDanhMucBienBanCapNhatRequest(DanhMucBienBanCapNhatRequest request, @MappingTarget DanhMucBienBan entity);

    DanhMucBienBanResponse toDanhMucBienBanResponse(DanhMucBienBan entity);

}
