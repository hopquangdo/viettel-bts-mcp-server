package vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NguonViecColumnLayoutDto {
    /** Thứ tự hiển thị — id cột cố định hoặc id cột custom. */
    private List<String> columnOrder = new ArrayList<>();
    /** Id cột bị ẩn. */
    private List<String> hiddenColumns = new ArrayList<>();
}
