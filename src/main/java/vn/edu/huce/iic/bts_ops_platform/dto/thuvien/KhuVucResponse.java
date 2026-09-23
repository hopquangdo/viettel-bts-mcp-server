package vn.edu.huce.iic.bts_ops_platform.dto.thuvien;

import lombok.Data;
import java.util.UUID;

@Data
public class KhuVucResponse {
    private UUID id;
    private String ma;
    private String ten;
}
