package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SanLuongCapNhatDoiTuongRequest {
    private List<SanLuongCapNhatHangMucItem> workItems = new ArrayList<>();
}
