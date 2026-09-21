package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "thuoc_tinh_hop_dong")
public class ThuocTinhHopDong extends AuditableEntity {

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "kieu_du_lieu_id", nullable = false, length = 20)
    private String kieuDuLieuId;

    @Column(name = "la_khoa_chinh", nullable = false)
    private Boolean laKhoaChinh = false;

    @Column(name = "bat_buoc", nullable = false)
    private Boolean batBuoc = false;

    @Column(name = "don_vi", length = 50)
    private String donVi;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tuy_chon", columnDefinition = "jsonb")
    private List<String> tuyChon;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
