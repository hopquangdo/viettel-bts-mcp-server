package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 1 đợt quyết toán thực tế của 1 đối tượng (trạm/tuyến) — quyetToanThuc trên HopDongDoiTuong là
 * tổng denormalize của các đợt còn hoạt động (xem
 * HopDongDoiTuongRepository.recalculateQuyetToanThuc).
 */
@Getter
@Setter
@Entity
@Table(name = "hop_dong_doi_tuong_quyet_toan_dot")
public class HopDongDoiTuongQuyetToanDot extends AuditableEntity {

    @Column(name = "hop_dong_doi_tuong_id", nullable = false)
    private UUID hopDongDoiTuongId;

    @Column(name = "so_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTien;

    @Column(name = "ngay_quyet_toan")
    private LocalDate ngayQuyetToan;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
