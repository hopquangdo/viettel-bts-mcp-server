package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.lienket.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongLienKetCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongLienKetResponse;

import java.util.List;
import java.util.UUID;

public interface HopDongLienKetService {

    List<HopDongLienKetResponse> listAll();

    List<HopDongLienKetResponse> listByHopDongId(UUID hopDongId);

    List<HopDongLienKetResponse> sync(UUID hopDongId, HopDongLienKetCapNhatRequest request);
}
