package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class SanLuongTaoHangLoatRequest {
    private LocalDate ngayThucHien;
    private UUID hopDongId;
    private UUID hopDongDoiTuongId;
    private List<SanLuongHangMucTaoItem> entries = new ArrayList<>();
    private String ghiChu;
}
