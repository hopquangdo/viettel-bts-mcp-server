package vn.edu.huce.iic.bts_ops_platform.entity.vuongmac;

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
@Table(name = "vuong_mac")
public class VuongMac extends AuditableEntity {

    @Column(name = "ma", nullable = false, length = 50)
    private String ma;

    @Column(name = "du_lieu_doi_tuong_id")
    private UUID duLieuDoiTuongId;

    @Column(name = "hop_dong_id")
    private UUID hopDongId;

    @Column(name = "giai_doan", nullable = false, length = 100)
    private String giaiDoan;

    /** Kiểu vướng mắc — phân loại, không quyết định bổ sung sản lượng. */
    @Column(name = "kieu_vuong_mac", nullable = false, length = 50)
    private String kieuVuongMac = "giai_phong_mat_bang";

    /** User chọn khi ghi nhận: true = được tiếp tục bổ sung SL khi VM còn mở. */
    @Column(name = "co_the_bo_sung_san_luong", nullable = false)
    private Boolean coTheBoSungSanLuong = false;

    @Column(name = "mo_ta", nullable = false)
    private String moTa;

    @Column(name = "mo_ta_day_du")
    private String moTaDayDu;

    @Column(name = "nguoi_bao_cao_id")
    private UUID nguoiBaoCaoId;

    @Column(name = "ten_nguoi_bao_cao")
    private String tenNguoiBaoCao;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "pending";

    @Column(name = "nguoi_xu_ly_id")
    private UUID nguoiXuLyId;

    @Column(name = "ghi_chu_giai_quyet")
    private String ghiChuGiaiQuyet;

    @Column(name = "ly_do_tu_choi")
    private String lyDoTuChoi;

    @Column(name = "qua_han_30_ngay", nullable = false)
    private Boolean quaHan30Ngay = false;

    @Column(name = "tep_dinh_kem_id")
    private UUID tepDinhKemId;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
