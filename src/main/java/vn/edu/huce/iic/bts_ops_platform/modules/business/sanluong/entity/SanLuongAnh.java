package vn.edu.huce.iic.bts_ops_platform.modules.business.sanluong.entity;

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
@Table(name = "san_luong_anh")
public class SanLuongAnh extends AuditableEntity {

    @Column(name = "hop_dong_doi_tuong_id", nullable = false)
    private UUID hopDongDoiTuongId;

    @Column(name = "hang_muc_chi_tiet_id")
    private UUID hangMucChiTietId;

    @Column(name = "hang_muc_cong_viec_id")
    private UUID hangMucCongViecId;

    @Column(name = "tep_dinh_kem_id", nullable = false)
    private UUID tepDinhKemId;

    @Column(name = "loai_anh", length = 50)
    private String loaiAnh;

    @Column(name = "da_hoan_thanh", nullable = false)
    private Boolean daHoanThanh = false;

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
