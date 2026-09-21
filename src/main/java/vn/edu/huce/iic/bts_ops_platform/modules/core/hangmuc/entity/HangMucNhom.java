package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hang_muc_nhom")
public class HangMucNhom extends AuditableEntity {

    @Column(name = "hop_dong_id")
    private UUID hopDongId;

    @Column(name = "ma", nullable = false, length = 50)
    private String ma;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "thu_tu", nullable = false)
    private Short thuTu = 0;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
