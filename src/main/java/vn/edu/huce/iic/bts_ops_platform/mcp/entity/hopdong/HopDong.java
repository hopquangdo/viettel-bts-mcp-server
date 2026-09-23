package vn.edu.huce.iic.bts_ops_platform.mcp.entity.hopdong;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hop_dong")
public class HopDong extends AuditableEntity {

    @Column(name = "ma_hop_dong", nullable = false, length = 100)
    private String maHopDong;

    @Column(name = "ma", length = 100)
    private String ma;

    @Column(name = "ten", length = 500)
    private String ten;

    /** Bên ký hợp đồng A — nguồn chính hiển thị Chủ đầu tư trên UI. */
    @Column(name = "ben_ky_a", length = 500)
    private String benKyA;

    @Column(name = "gia_tri_hd")
    private Long giaTriHd;

    @Column(name = "ngay_thuc_hien")
    private LocalDate ngayThucHien;

    @Column(name = "so_ngay_thuc_hien")
    private Integer soNgayThucHien;

    /** Tự tính: ngayThucHien + soNgayThucHien (calendar days). */
    @Column(name = "han_hop_dong")
    private LocalDate hanHopDong;

    @Column(name = "heso_nguong")
    private BigDecimal hesoNguong;

    @Column(name = "loai_hop_dong_id")
    private UUID loaiHopDongId;

    @Column(name = "kieu_hop_dong_id")
    private UUID kieuHopDongId;

    @Column(name = "trang_thai_phap_ly")
    private Short trangThaiPhapLy;

    /** CHUA_TC | DANG_TC | HOAN_THANH | HUY */
    @Column(name = "trang_thai_thi_cong", length = 20)
    private String trangThaiThiCong = "CHUA_TC";

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;

    /** Denormalize từ cây hạng mục (HangMucThanhTienCalculator) — xem HopDongThanhTienConsumer. */
    @Column(name = "tong_thanh_tien_thi_cong")
    private BigDecimal tongThanhTienThiCong;

    /** Checklist điều kiện đảm bảo triển khai — gắn hợp đồng chính. */
    @Column(name = "checklist_doi_tac", nullable = false)
    private Boolean checklistDoiTac = false;

    @Column(name = "checklist_ccdc", nullable = false)
    private Boolean checklistCcdc = false;

    @Column(name = "checklist_atld", nullable = false)
    private Boolean checklistAtld = false;

    @Column(name = "checklist_vat_tu_a", nullable = false)
    private Boolean checklistVatTuA = false;

    @Column(name = "checklist_vat_tu_b", nullable = false)
    private Boolean checklistVatTuB = false;

    @Column(name = "checklist_ghi_chu", columnDefinition = "text")
    private String checklistGhiChu;
}
