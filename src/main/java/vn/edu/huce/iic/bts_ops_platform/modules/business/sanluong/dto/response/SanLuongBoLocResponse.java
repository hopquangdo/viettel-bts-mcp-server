package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SanLuongBoLocResponse {
    private List<SanLuongBoLocOptionResponse> partners = new ArrayList<>();
    private List<SanLuongBoLocOptionResponse> contracts = new ArrayList<>();
    private List<SanLuongBoLocOptionResponse> domains = new ArrayList<>();
}
