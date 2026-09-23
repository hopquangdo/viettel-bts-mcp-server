package vn.edu.huce.iic.bts_ops_platform.entity.cauhinh;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

@Getter
@Setter
@Entity
@Table(name = "doi_tuong_quan_ly")
public class DoiTuongQuanLy extends AuditableEntity {

    @Column(name = "ma", nullable = false, length = 50)
    private String ma;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "bieu_tuong", length = 20)
    private String bieuTuong;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;

    @Column(name = "hien_thi_tren_giao_dien", nullable = false)
    private Boolean hienThiTrenGiaoDien = true;
}
