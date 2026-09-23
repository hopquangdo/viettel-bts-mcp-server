package vn.edu.huce.iic.bts_ops_platform.mcp.dto.tinh;

import java.util.UUID;

public record TinhInfo(
        UUID id,
        String ma,
        String ten,
        String maTinhCu
) {
}
