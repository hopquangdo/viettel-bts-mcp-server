package vn.edu.huce.iic.bts_ops_platform.mcp.entity.phancong;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.entity.AuditableEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "phan_cong")
public class PhanCong extends AuditableEntity {

    @Column(name = "hop_dong_id")
    private UUID hopDongId;

    @Column(name = "hop_dong_doi_tuong_id")
    private UUID hopDongDoiTuongId;

    @Column(name = "nha_thau")
    private String nhaThau;

    @Column(name = "nguoi_dung_id")
    private UUID nguoiDungId;

    @Column(name = "giai_doan", length = 100)
    private String giaiDoan;

    @Column(name = "khu_vuc_id")
    private UUID khuVucId;

    @Column(name = "ma_vung", length = 50)
    private String maVung;

    @Column(name = "tinh_thanh_id")
    private UUID tinhThanhId;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
