package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BienBanXuatDonRequest {
    private List<UUID> hopDongDoiTuongIds;
    /** BAN_GIAO_MAT_BANG | YEU_CAU_VAT_TU | NHAN_VAT_TU, hoặc mã bất kỳ trong danh mục biên bản
     * đang hoạt động và chưa có mẫu Word (tạo bản ghi quản lý, đính file ký ngoài). */
    private String loaiBienBan;
    /** Ngày thực tế của mốc (bàn giao, nhận vật tư…) — bỏ trống lấy ngày hiện tại. Dùng so KPI. */
    private LocalDate ngayLap;
    private BienBanNguoiKyRequest nguoiKy;
}
