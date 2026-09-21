package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class SanLuongDoiTuongChiTietResponse {
    private UUID hopDongDoiTuongId;
    private UUID hopDongId;
    private UUID doiTuongQuanLyId;
    private String objectCode;
    private String contract;
    private String contractor;
    private String region;
    private LocalDate surveyDate;
    private BigDecimal totalOutput;
    private BigDecimal routeLengthKm;
    private String startStation;
    private String endStation;
    private List<SanLuongHangMucItemResponse> workItems = new ArrayList<>();
    private Integer workItemsTotal;
    private List<SanLuongAnhNhomResponse> photoSections = new ArrayList<>();
    /** Đối tượng đã xác nhận hoàn thành — không bổ sung sản lượng. */
    private boolean outputLocked;
    /** Denormalize — có vướng mắc mở chặn bổ sung sản lượng. */
    private boolean coVuongMacMo;
}
