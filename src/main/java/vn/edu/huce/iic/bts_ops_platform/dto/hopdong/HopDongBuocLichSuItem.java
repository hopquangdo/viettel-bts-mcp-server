package vn.edu.huce.iic.bts_ops_platform.dto.hopdong;

import lombok.Data;

import java.time.Instant;

/**
 * 1 dòng lịch sử chuyển bước lấy từ audit_log (audit_log.hop_dong_id) — HIỆN TẠI hệ thống CHƯA
 * phát sinh event audit khi đổi trạng_thai_hop_dong_id của hop_dong_doi_tuong (audit_log chỉ có
 * hạ tầng consumer, chưa có nơi publish loại event này), nên danh sách này thường RỖNG cho tới
 * khi nghiệp vụ đó được nối dây publish event AUDIT. Field vẫn trả về (không xoá) để sẵn sàng khi
 * dữ liệu có mà không cần đổi hợp đồng API.
 */
@Data
public class HopDongBuocLichSuItem {
    private Instant ngay;
    private String hanhDong;
    private String moTa;
    private String nguoiThucHien;
}
