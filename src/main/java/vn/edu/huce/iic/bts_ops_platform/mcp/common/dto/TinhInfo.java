package vn.edu.huce.iic.bts_ops_platform.mcp.common.dto;

import java.util.UUID;

public record TinhInfo(
        UUID id,
        String ma,
        String ten,
        String maTinhCu
) {
}
