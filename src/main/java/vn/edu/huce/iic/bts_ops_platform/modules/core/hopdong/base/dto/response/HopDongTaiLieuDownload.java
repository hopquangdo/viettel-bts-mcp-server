package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

/**
 * Nội dung file tài liệu hợp đồng để stream về client.
 */
public record HopDongTaiLieuDownload(byte[] content, String fileName, String contentType) {
}
