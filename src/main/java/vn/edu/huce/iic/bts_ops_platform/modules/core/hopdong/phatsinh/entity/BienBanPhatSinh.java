package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Biên bản phát sinh khối lượng — ghi nhận đầu việc/khối lượng phát sinh ngoài hạng mục gốc,
 * kèm hồ sơ thiết kế/dự toán điều chỉnh (qua BienBanPhatSinhTepDinhKem). Có luồng duyệt/từ chối
 * riêng, độc lập với BienBan (biên bản nghiệm thu xuất theo mẫu Word). */
@Getter
@Setter
@Entity
@Table(name = "bien_ban_phat_sinh")
public class BienBanPhatSinh extends AuditableEntity {

    @Column(name = "hop_dong_id", nullable = false)
    private UUID hopDongId;

    /** Trạm/đối tượng liên quan — null nếu phát sinh áp dụng chung cho cả hợp đồng. */
    @Column(name = "hop_dong_doi_tuong_id")
    private UUID hopDongDoiTuongId;

    @Column(name = "ma_phat_sinh", nullable = false, length = 100)
    private String maPhatSinh;

    @Column(name = "ten_dau_viec", nullable = false)
    private String tenDauViec;

    @Column(name = "mo_ta_ly_do", columnDefinition = "text")
    private String moTaLyDo;

    @Column(name = "khoi_luong_phat_sinh", precision = 18, scale = 2)
    private BigDecimal khoiLuongPhatSinh;

    @Column(name = "don_vi", length = 50)
    private String donVi;

    /** JSON [{tenDauViec, khoiLuong, donVi, ghiChu}] — 1 lần phát sinh thường kéo theo nhiều đầu
     * việc, lưu bảng con dạng JSON (cùng pattern hopDongDoiTuongIdsJson của BienBan) để khỏi thêm
     * bảng + query phụ. tenDauViec/khoiLuongPhatSinh ở trên giữ dòng đầu để tương thích ngược. */
    @Column(name = "chi_tiet_json", columnDefinition = "text")
    private String chiTietJson;

    /** Ngày ký phụ lục hợp đồng cho phần phát sinh — chỉ có sau khi CĐT đã duyệt. */
    @Column(name = "ngay_ky_phu_luc")
    private LocalDate ngayKyPhuLuc;

    @Column(name = "ngay_lap", nullable = false)
    private LocalDate ngayLap;

    @Column(name = "nguoi_lap_id")
    private UUID nguoiLapId;

    @Column(name = "nguoi_lap_ten")
    private String nguoiLapTen;

    /** cho_duyet | da_duyet | tu_choi */
    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "cho_duyet";

    /** PHAT_SINH | YEU_CAU_NGHIEM_THU */
    @Column(name = "loai", nullable = false, length = 30)
    private String loai = "PHAT_SINH";

    @Column(name = "ly_do_tu_choi", columnDefinition = "text")
    private String lyDoTuChoi;

    @Column(name = "nguoi_phe_duyet_id")
    private UUID nguoiPheDuyetId;

    @Column(name = "nguoi_phe_duyet_ten")
    private String nguoiPheDuyetTen;

    @Column(name = "ngay_phe_duyet")
    private Instant ngayPheDuyet;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
