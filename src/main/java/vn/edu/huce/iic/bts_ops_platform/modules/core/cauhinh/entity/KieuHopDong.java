package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "kieu_hop_dong")
public class KieuHopDong extends AuditableEntity {

    @Column(name = "loai_hop_dong_id", nullable = false)
    private UUID loaiHopDongId;

    @Column(name = "ma", nullable = false, unique = true, length = 100)
    private String ma;

    @Column(name = "ten", nullable = false)
    private String ten;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "nhom", nullable = false, length = 1)
    private String nhom = "A";

    @Column(name = "mau_sac", length = 20)
    private String mauSac;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
