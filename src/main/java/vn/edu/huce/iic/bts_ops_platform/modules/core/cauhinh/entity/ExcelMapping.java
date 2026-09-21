package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity;

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
@Table(name = "excel_mapping")
public class ExcelMapping extends AuditableEntity {

    @Column(name = "doi_tuong_quan_ly_id")
    private UUID doiTuongQuanLyId;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "ma", nullable = false, length = 100)
    private String ma;

    @Column(name = "mo_ta")
    private String moTa;

    /** Dòng chứa tiêu đề cột (1-based). */
    @Column(name = "dong_tieu_de", nullable = false)
    private Short dongTieuDe = 1;

    /** Dòng bắt đầu đọc dữ liệu (1-based). */
    @Column(name = "dong_bat_dau_doc", nullable = false)
    private Short dongBatDauDoc = 2;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;

    /** Cột Excel chứa tên nhóm ưu tiên (chỉ đối tượng quản lý, không áp dụng Hạng mục thi công). */
    @Column(name = "cot_nhom_uu_tien", length = 50)
    private String cotNhomUuTien;

    @Column(name = "nguoi_cap_nhat")
    private UUID nguoiCapNhat;
}
