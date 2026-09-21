package vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "thong_bao")
public class ThongBao extends BaseEntity {

    @Column(name = "nguoi_nhan_id", nullable = false)
    private UUID nguoiNhanId;

    /** PHAN_CONG | DEADLINE_KPI | WORKFLOW | CANH_BAO_TY_LE_HUY | CANH_BAO_SAN_LUONG */
    @Column(name = "loai", nullable = false, length = 50)
    private String loai;

    @Column(name = "tieu_de", nullable = false)
    private String tieuDe;

    @Column(name = "noi_dung", columnDefinition = "text")
    private String noiDung;

    @Column(name = "lien_ket", length = 500)
    private String lienKet;

    @Column(name = "tham_chieu_id")
    private UUID thamChieuId;

    @Column(name = "da_doc", nullable = false)
    private Boolean daDoc = false;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private Instant ngayTao;

    @Column(name = "ngay_doc")
    private Instant ngayDoc;
}
