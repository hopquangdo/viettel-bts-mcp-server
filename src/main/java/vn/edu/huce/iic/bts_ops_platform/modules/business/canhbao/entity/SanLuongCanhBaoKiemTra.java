package vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.entity;

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
@Table(name = "san_luong_canh_bao_kiem_tra")
public class SanLuongCanhBaoKiemTra extends BaseEntity {

    @Column(name = "hop_dong_doi_tuong_id", nullable = false, unique = true)
    private UUID hopDongDoiTuongId;

    @Column(name = "nguoi_kiem_tra_id")
    private UUID nguoiKiemTraId;

    @Column(name = "nguoi_kiem_tra_ten")
    private String nguoiKiemTraTen;

    @Column(name = "ngay_kiem_tra", nullable = false)
    private Instant ngayKiemTra;

    @Column(name = "ghi_chu", columnDefinition = "text")
    private String ghiChu;
}
