package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.lienket.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hop_dong_lien_ket")
public class HopDongLienKet extends BaseEntity {

    @Column(name = "hop_dong_id", nullable = false)
    private UUID hopDongId;

    @Column(name = "hop_dong_lien_ket_id", nullable = false)
    private UUID hopDongLienKetId;

    @Column(name = "ghi_chu", length = 100)
    private String ghiChu;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private Instant ngayTao;

    @Column(name = "ngay_xoa")
    private Instant ngayXoa;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = Instant.now();
        }
    }
}
