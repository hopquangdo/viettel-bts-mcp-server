package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.ThuocTinhHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThuocTinhResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LienKetBangLabelService {

    void applyDisplayLabels(
            List<HopDongThuocTinhResponse> values,
            Map<UUID, ThuocTinhHopDongResponse> definitions);
}
