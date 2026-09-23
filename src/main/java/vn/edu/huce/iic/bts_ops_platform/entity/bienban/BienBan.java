package vn.edu.huce.iic.bts_ops_platform.entity.bienban;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Lưu vết mỗi lần xuất biên bản — phục vụ tra cứu lịch sử ("Danh sách biên bản") và luồng
 * phê duyệt/từ chối; không lưu lại nội dung file (file được tạo lại theo yêu cầu từ
 * {@code hopDongDoiTuongIdsJson}/{@code nguoiKyJson} khi "xuất lại"). */
@Getter
@Setter
@Entity
@Table(name = "bien_ban")
public class BienBan extends AuditableEntity {

    @Column(name = "hop_dong_id", nullable = false)
    private UUID hopDongId;

    @Column(name = "ma_bien_ban", nullable = false, length = 100)
    private String maBienBan;

    /** BIEN_BAN_SO_1 | NHAT_KY_THI_CONG | BIEN_BAN_SO_2 | BAO_CAO_KHAO_SAT */
    @Column(name = "loai_bien_ban", nullable = false, length = 50)
    private String loaiBienBan;

    @Column(name = "ngay_lap", nullable = false)
    private LocalDate ngayLap;

    @Column(name = "nguoi_lap_id")
    private UUID nguoiLapId;

    /** Snapshot họ tên người lập tại thời điểm xuất — tránh phải join lại nguoi_dung khi hiển thị. */
    @Column(name = "nguoi_lap_ten")
    private String nguoiLapTen;

    /** cho_duyet | da_duyet | tu_choi */
    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "cho_duyet";

    @Column(name = "so_luong_doi_tuong")
    private Integer soLuongDoiTuong;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;

    /** da_chon | theo_tinh | theo_nha_thau — suy ra tự động từ tập đối tượng đã chọn khi xuất. */
    @Column(name = "pham_vi_xuat", length = 20)
    private String phamViXuat;

    /** Mô tả phạm vi ở dạng người đọc được (tên tỉnh, tên nhà thầu, hoặc "N đối tượng"). */
    @Column(name = "pham_vi_chi_tiet")
    private String phamViChiTiet;

    @Column(name = "ly_do_tu_choi", columnDefinition = "text")
    private String lyDoTuChoi;

    @Column(name = "nguoi_phe_duyet_id")
    private UUID nguoiPheDuyetId;

    @Column(name = "nguoi_phe_duyet_ten")
    private String nguoiPheDuyetTen;

    @Column(name = "ngay_phe_duyet")
    private Instant ngayPheDuyet;

    /** Trỏ về biên bản bị từ chối mà bản ghi này được "xuất lại" từ đó — null nếu là lần xuất gốc. */
    @Column(name = "bien_ban_goc_id")
    private UUID bienBanGocId;

    @Column(name = "ly_do_huy_hieu_luc", columnDefinition = "text")
    private String lyDoHuyHieuLuc;

    @Column(name = "ngay_huy_hieu_luc")
    private Instant ngayHuyHieuLuc;

    @Column(name = "nguoi_huy_id")
    private UUID nguoiHuyId;

    @Column(name = "nguoi_huy_ten")
    private String nguoiHuyTen;

    /** Snapshot JSON danh sách hopDongDoiTuongId đã dùng khi xuất — cho phép "xuất lại" tạo đúng
     * lại tài liệu mà không phải hỏi lại người dùng chọn đối tượng. */
    @Column(name = "hop_dong_doi_tuong_ids_json", columnDefinition = "text")
    private String hopDongDoiTuongIdsJson;

    /** Snapshot JSON thông tin người ký đã dùng khi xuất — dùng lại khi "xuất lại". */
    @Column(name = "nguoi_ky_json", columnDefinition = "text")
    private String nguoiKyJson;

    /** Tên trạng thái luồng của trạm tại thời điểm xuất — snapshot cho cột "Giai đoạn" trên FE. */
    @Column(name = "trang_thai_tram_ten")
    private String trangThaiTramTen;
}
