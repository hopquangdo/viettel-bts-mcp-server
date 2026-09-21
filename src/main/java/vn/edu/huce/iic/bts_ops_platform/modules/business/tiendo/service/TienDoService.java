package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.service;

import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.ChecklistDamBaoCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.DoiTuongChecklistDapUngCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.DoiTuongTienDoCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.HopDongChecklistSyncRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.KeHoachImportChiTietRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.KeHoachTrienKhaiTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.KeHoachTuChoiRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.KpiNguongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.VatTuTrangThaiCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.request.VatTuYeuCauTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.ChecklistDamBaoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.ChecklistMucResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.DoiTuongChecklistDapUngResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.HopDongChecklistResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.DoiTuongTienDoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.KeHoachTrienKhaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.KpiCanhBaoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.KpiNguongCauHinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.dto.response.VatTuYeuCauResponse;

import java.util.List;
import java.util.UUID;

public interface TienDoService {

    List<KpiCanhBaoResponse> listKpiCanhBao();

    DoiTuongTienDoResponse getDoiTuongTienDo(UUID id);

    DoiTuongTienDoResponse capNhatDoiTuongTienDo(UUID id, DoiTuongTienDoCapNhatRequest request);

    ChecklistDamBaoResponse getChecklistDamBao(UUID hopDongId);

    ChecklistDamBaoResponse capNhatChecklistDamBao(UUID hopDongId, ChecklistDamBaoCapNhatRequest request);

    HopDongChecklistResponse getHopDongChecklist(UUID hopDongId);

    HopDongChecklistResponse syncHopDongChecklist(UUID hopDongId, HopDongChecklistSyncRequest request);

    List<DoiTuongChecklistDapUngResponse> listDoiTuongChecklistDapUng(UUID hopDongId, List<UUID> doiTuongIds);

    DoiTuongChecklistDapUngResponse capNhatDoiTuongChecklistDapUng(
            UUID doiTuongId,
            DoiTuongChecklistDapUngCapNhatRequest request);

    List<KeHoachTrienKhaiResponse> listKeHoach(UUID hopDongId);

    KeHoachTrienKhaiResponse taoKeHoach(KeHoachTrienKhaiTaoRequest request);

    KeHoachTrienKhaiResponse guiKeHoachDuyet(UUID id);

    KeHoachTrienKhaiResponse duyetKeHoach(UUID id);

    KeHoachTrienKhaiResponse tuChoiKeHoach(UUID id, KeHoachTuChoiRequest request);

    KeHoachTrienKhaiResponse importKeHoachChiTiet(UUID keHoachId, KeHoachImportChiTietRequest request);

    VatTuYeuCauResponse taoVatTuYeuCau(VatTuYeuCauTaoRequest request);

    VatTuYeuCauResponse capNhatTrangThaiVatTu(UUID id, VatTuTrangThaiCapNhatRequest request);

    List<VatTuYeuCauResponse> listVatTuByDoiTuong(UUID doiTuongId);

    List<KpiNguongCauHinhResponse> listKpiNguong();

    KpiNguongCauHinhResponse capNhatKpiNguong(UUID id, KpiNguongCapNhatRequest request);
}
