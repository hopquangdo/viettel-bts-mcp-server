package vn.edu.huce.iic.bts_ops_platform.mcp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.nguonluc.NguonViecBang;

import java.util.List;
import java.util.UUID;

/**
 * Repo tool-riêng cho phần "overlay thủ công" (nguon_viec_bang: hàng tự thêm + ghi chú khu vực từ
 * UI WorkSourceManagement). Chỉ đọc entity/DTO JSON có sẵn (NguonViecBang/NguonViecBangDuLieu là
 * entity + DTO thuần, KHÔNG phải service/repository REST) — không gọi NguonLucService/
 * NguonViecBangRepository ở module ngoài, tự khai báo JpaRepository riêng cho tool giống các
 * *ToolRepository khác. Vì AI tool không có khái niệm "1 trung tâm đang xem" như UI, ta gộp overlay
 * của TẤT CẢ trung tâm còn hoạt động (không lọc theo trungTam) khi tổng hợp dữ liệu cho AI.
 */
public interface NguonViecOverlayToolRepository extends JpaRepository<NguonViecBang, UUID> {

    List<NguonViecBang> findByNgayXoaIsNull();
}
