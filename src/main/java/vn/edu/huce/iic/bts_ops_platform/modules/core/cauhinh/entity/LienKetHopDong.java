package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.BaseEntity;

import java.time.Instant;
import java.util.UUID;

/** Liên kết giữa các kiểu hợp đồng nguồn/đích (vd: hợp đồng thi công -> hợp đồng bổ sung). */
@Getter
@Setter
@Entity
@Table(name = "lien_ket_hop_dong")
public class LienKetHopDong extends BaseEntity {

    @Column(name = "loai_lien_ket", nullable = false, length = 20)
    private String loaiLienKet;

    @Column(name = "kieu_nguon_id", nullable = false)
    private UUID kieuNguonId;

    @Column(name = "kieu_dich_id", nullable = false)
    private UUID kieuDichId;

    @Column(name = "ten_kieu_nguon")
    private String tenKieuNguon;

    @Column(name = "ten_kieu_dich")
    private String tenKieuDich;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private Instant ngayTao;

    @Column(name = "ngay_xoa")
    private Instant ngayXoa;
}
