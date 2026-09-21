package vn.edu.huce.iic.bts_ops_platform.modules.business.dashboard.dto;

import java.util.UUID;

/** Tra cứu thuộc tính khóa chính / khu vực / tỉnh thành của một đối tượng quản lý. */
public record ThuocTinhLookup(UUID primaryAttrId, UUID khuVucAttrId, UUID tinhThanhAttrId) {
}
