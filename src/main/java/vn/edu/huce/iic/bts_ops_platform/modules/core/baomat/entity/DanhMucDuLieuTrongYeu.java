package vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

@Getter
@Setter
@Entity
@Table(name = "danh_muc_du_lieu_trong_yeu")
public class DanhMucDuLieuTrongYeu extends AuditableEntity {

    @Column(name = "ma", nullable = false, unique = true, length = 100)
    private String ma;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "bang_du_lieu", length = 100)
    private String bangDuLieu;

    @Column(name = "cot_du_lieu", length = 100)
    private String cotDuLieu;

    @Column(name = "muc_do", nullable = false, length = 20)
    private String mucDo;

    @Column(name = "chu_so_huu")
    private String chuSoHuu;

    @Column(name = "mo_ta", columnDefinition = "text")
    private String moTa;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
