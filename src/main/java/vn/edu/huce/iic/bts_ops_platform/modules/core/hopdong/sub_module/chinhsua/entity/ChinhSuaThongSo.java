package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.chinhsua.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.time.Instant;
import java.util.UUID;

/** Lịch sử / đề xuất chỉnh sửa thông số trạm (thuộc tính động). */
@Getter
@Setter
@Entity
@Table(name = "chinh_sua_thong_so")
public class ChinhSuaThongSo extends AuditableEntity {

    @Column(name = "hop_dong_id")
    private UUID hopDongId;

    @Column(name = "hop_dong_doi_tuong_id", nullable = false)
    private UUID hopDongDoiTuongId;

    @Column(name = "thuoc_tinh_id", nullable = false)
    private UUID thuocTinhId;

    @Column(name = "ten_thuoc_tinh")
    private String tenThuocTinh;

    @Column(name = "gia_tri_cu", columnDefinition = "text")
    private String giaTriCu;

    @Column(name = "gia_tri_moi", columnDefinition = "text")
    private String giaTriMoi;

    @Column(name = "giai_trinh", columnDefinition = "text")
    private String giaiTrinh;

    /** da_ap_dung | cho_duyet | tu_choi */
    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "da_ap_dung";

    @Column(name = "lan_chinh_sua", nullable = false)
    private Integer lanChinhSua = 1;

    @Column(name = "nguoi_de_xuat_id")
    private UUID nguoiDeXuatId;

    @Column(name = "nguoi_de_xuat_ten")
    private String nguoiDeXuatTen;

    @Column(name = "nguoi_phe_duyet_id")
    private UUID nguoiPheDuyetId;

    @Column(name = "nguoi_phe_duyet_ten")
    private String nguoiPheDuyetTen;

    @Column(name = "ngay_phe_duyet")
    private Instant ngayPheDuyet;

    @Column(name = "ly_do_tu_choi", columnDefinition = "text")
    private String lyDoTuChoi;
}
