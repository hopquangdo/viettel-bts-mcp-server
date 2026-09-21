package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services;

import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachHopDongItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachMoRongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTinhChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTramRowResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface HopDongDanhSachService {

    HopDongDanhSachTongQuanResponse getTongQuan(UUID loaiHopDongId, boolean includeArchive);

    PageResponse<HopDongDanhSachHopDongItemResponse> listHopDong(
            UUID kieuHopDongId,
            UUID loaiHopDongId,
            String search,
            Integer page,
            Integer size,
            boolean includeArchive);

    HopDongDanhSachMoRongResponse getMoRong(UUID hopDongId);

    HopDongDanhSachTinhChiTietResponse getTinhChiTiet(
            UUID hopDongId,
            String khuVuc,
            String tinh,
            String nhaThau);

    /** Toàn bộ trạm của 1 hợp đồng, không giới hạn theo tỉnh — dùng cho trang "Biên bản & hồ sơ theo trạm". */
    List<HopDongDanhSachTramRowResponse> getDanhSachTramHopDong(UUID hopDongId);

    /** Luồng trạng thái (các bước) thực sự gắn với 1 kiểu hợp đồng — [] nếu kiểu chưa được gán luồng. */
    List<LuongTrangThaiBuocResponse> getFlowBuocForKieu(UUID loaiHopDongId, UUID kieuHopDongId);

    /**
     * Bản batch của {@link #getFlowBuocForKieu(UUID, UUID)}: nạp luồng trạng thái của MỌI kiểu
     * hợp đồng bằng 4 query cố định, thay cho việc gọi hàm trên một lần mỗi kiểu.
     * Key là {@code kieuHopDongId} (mỗi kiểu thuộc đúng 1 loại). Kiểu chưa gán luồng không có
     * mặt trong map.
     */
    Map<UUID, List<LuongTrangThaiBuocResponse>> getFlowBuocForAllKieu();
}
