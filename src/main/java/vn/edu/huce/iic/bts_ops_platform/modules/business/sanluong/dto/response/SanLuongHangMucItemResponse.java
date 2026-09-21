package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class SanLuongHangMucItemResponse {
    private String id;
    private UUID hangMucCongViecId;
    private UUID hangMucChiTietId;
    private int order;
    private LocalDate date;
    private String name;
    /** Mã hạng mục — dùng hiển thị khi name rỗng */
    private String code;
    private BigDecimal unitPrice;
    private String status;
    private BigDecimal amount;
    /** Khối lượng ghi nhận trong ngày hiện tại. */
    private BigDecimal todayAmount;
    /** dat | khong_dat | null (chưa nghiệm thu) — kết quả nghiệm thu của lần ghi nhận sản lượng mới nhất. */
    private String ketQuaNghiemThu;
    private String lyDoKhongDat;
    private UUID nguoiNghiemThuId;
    private String nguoiNghiemThuTen;
    private Instant ngayNghiemThu;
    /** Lý do không đạt gần nhất khi hạng mục đã bị reset (không còn bản ghi active). */
    private String lyDoKhongDatGanNhat;
}
