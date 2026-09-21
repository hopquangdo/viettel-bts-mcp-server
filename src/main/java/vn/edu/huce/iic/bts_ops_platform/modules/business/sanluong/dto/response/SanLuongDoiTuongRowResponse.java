package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class SanLuongDoiTuongRowResponse {
    private UUID id;
    private UUID hopDongId;
    private UUID hopDongDoiTuongId;
    private UUID doiTuongQuanLyId;
    private String objectCode;
    private String contract;
    /** Số hợp đồng (mã) — FE hiển thị "số trên, tên dưới". */
    private String contractCode;
    private String contractor;
    /** UUID nhà thầu — dùng cho bộ lọc FE (contractorId). */
    private UUID contractorId;
    private String provinceCode;
    /** Mã tỉnh cũ (trước sáp nhập) — xem HopDongDoiTuongSnapshot.oldProvince. */
    private String provinceCodeOld;
    private String region;
    private LocalDate surveyDate;
    private LocalDate constructionDate;
    private BigDecimal totalOutput;
    private BigDecimal todayOutput;
    private int itemsDone;
    private int itemsTotal;
    private int completionPercent;
    private int issueCount;
    private Instant lastUpdated;
    private String rowStatus;
    private BigDecimal routeLengthKm;
    private String startStation;
    private String endStation;
    /** Đối tượng đã xác nhận hoàn thành — không bổ sung sản lượng. */
    private boolean outputLocked;
    /** Denormalize — có vướng mắc mở chặn bổ sung sản lượng. */
    private boolean coVuongMacMo;
}
