package vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.services;

import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.dto.request.CanhBaoNguongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.dto.request.SanLuongDaKiemTraRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.dto.response.TyLeHuyTramResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface CanhBaoService {

    TyLeHuyTramResponse tyLeHuyTram();

    BigDecimal getNguongTyLeHuy();

    BigDecimal capNhatNguongTyLeHuy(CanhBaoNguongCapNhatRequest request);

    void danhDauSanLuongDaKiemTra(UUID hopDongDoiTuongId, SanLuongDaKiemTraRequest request);

    void quetCanhBaoTuDong();
}
