package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity;

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
@Table(name = "mat_khau_lich_su")
public class MatKhauLichSu extends BaseEntity {

    @Column(name = "nguoi_dung_id", nullable = false)
    private UUID nguoiDungId;

    @Column(name = "mat_khau_hash", nullable = false, length = 255)
    private String matKhauHash;

    @Column(name = "ngay_tao", nullable = false)
    private Instant ngayTao = Instant.now();
}
