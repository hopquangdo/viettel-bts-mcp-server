package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SanLuongAnhNhomResponse {
    private String id;
    private String title;
    private String status;
    private List<SanLuongAnhResponse> photos = new ArrayList<>();
}
