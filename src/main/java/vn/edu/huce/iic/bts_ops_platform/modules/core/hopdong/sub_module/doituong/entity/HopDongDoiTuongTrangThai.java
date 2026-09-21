package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.entity;

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
@Table(name = "hop_dong_doi_tuong_trang_thai")
public class HopDongDoiTuongTrangThai extends AuditableEntity {

    @Column(name = "kieu_hop_dong_id")
    private UUID kieuHopDongId;

    @Column(name = "loai_hop_dong_id")
    private UUID loaiHopDongId;

    @Column(name = "luong_trang_thai_id")
    private UUID luongTrangThaiId;

    @Column(name = "doi_tuong_id")
    private UUID doiTuongId;

    @Column(name = "trang_thai_id")
    private UUID trangThaiId;

    @Column(name = "nhan_hien_thi", nullable = false)
    private String nhanHienThi;

    @Column(name = "mau_sac", length = 20)
    private String mauSac;

    @Column(name = "mau_nen", length = 20)
    private String mauNen;

    @Column(name = "gia_tri_loc", length = 50)
    private String giaTriLoc;

    @Column(name = "thu_tu", nullable = false)
    private Short thuTu = 0;

    @Column(name = "ghi_de", nullable = false)
    private Boolean ghiDe = false;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
