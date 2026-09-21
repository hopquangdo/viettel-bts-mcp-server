package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "vat_tu_yeu_cau")
public class VatTuYeuCau extends AuditableEntity {

    @Column(name = "hop_dong_doi_tuong_id", nullable = false)
    private UUID hopDongDoiTuongId;

    /** B_CAP | A_CAP */
    @Column(name = "loai", nullable = false, length = 20)
    private String loai = "B_CAP";

    /** TAO | GUI | SAN_XUAT | DA_NHAN | ... */
    @Column(name = "trang_thai", nullable = false, length = 30)
    private String trangThai = "TAO";

    @Column(name = "ngay_yeu_cau", nullable = false)
    private LocalDate ngayYeuCau;

    @Column(name = "ngay_hoan_thanh")
    private LocalDate ngayHoanThanh;

    @Column(name = "ghi_chu", columnDefinition = "text")
    private String ghiChu;
}
