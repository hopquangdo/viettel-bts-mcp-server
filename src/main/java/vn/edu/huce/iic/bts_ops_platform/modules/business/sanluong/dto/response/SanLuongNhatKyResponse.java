package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SanLuongNhatKyResponse {
    private List<SanLuongNhatKyColumnResponse> columns = new ArrayList<>();
    private List<SanLuongNhatKyEntryResponse> entries = new ArrayList<>();
}
