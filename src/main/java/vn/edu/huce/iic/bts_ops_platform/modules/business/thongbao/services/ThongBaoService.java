package vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.services;

import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.dto.response.ThongBaoResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.dto.response.ThongBaoUnreadCountResponse;

import java.util.List;
import java.util.UUID;

public interface ThongBaoService {

    PageResponse<ThongBaoResponse> inbox(Integer page, Integer size);

    List<ThongBaoResponse> listMine(int limit);

    long demChuaDoc();

    ThongBaoUnreadCountResponse unreadCount();

    ThongBaoResponse danhDauDaDoc(UUID id);

    void markRead(UUID id);

    void markAllRead();

    void thongBaoChinhSuaThongSoChoDuyet(
            UUID hopDongDoiTuongId,
            String maTram,
            String noiDungTomTat,
            UUID nguoiDeXuatId);

    void thongBaoChinhSuaThongSoDaDuyet(UUID nguoiNhanId, UUID hopDongDoiTuongId, String maTram, String tenThuocTinh);

    void thongBaoChinhSuaThongSoTuChoi(
            UUID nguoiNhanId,
            UUID hopDongDoiTuongId,
            String maTram,
            String tenThuocTinh,
            String lyDo);
}
