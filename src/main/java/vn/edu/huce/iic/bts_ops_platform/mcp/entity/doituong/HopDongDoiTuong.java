package vn.edu.huce.iic.bts_ops_platform.mcp.entity.doituong;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hop_dong_doi_tuong")
public class HopDongDoiTuong extends AuditableEntity {

    @Column(name = "hop_dong_id", nullable = false)
    private UUID hopDongId;

    @Column(name = "doi_tuong_quan_ly_id", nullable = false)
    private UUID doiTuongQuanLyId;

    @Column(name = "trang_thai_hop_dong_id")
    private UUID trangThaiHopDongId;

    /** Trạng thái trước CXN_HUY / CXN_HT — rollback khi từ chối yêu cầu xác nhận. */
    @Column(name = "trang_thai_truoc_xac_nhan_id")
    private UUID trangThaiTruocXacNhanId;

    @Column(name = "hop_dong_nhom_uu_tien_id")
    private UUID hopDongNhomUuTienId;

    /** Quyết toán thực (VND) — dùng đối soát volume. */
    @Column(name = "quyet_toan_thuc", precision = 18, scale = 2)
    private BigDecimal quyetToanThuc;

    /** Bổ sung / điều chỉnh sản lượng tích lũy (VND) — cộng vào SL báo khi đối soát CL HĐ. */
    @Column(name = "bo_sung_san_luong", precision = 18, scale = 2)
    private BigDecimal boSungSanLuong = BigDecimal.ZERO;

    /** Ngày hoàn thành thi công — dùng tính số ngày tồn (trạm tồn). */
    @Column(name = "ngay_ht_tc")
    private LocalDate ngayHtTc;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;

    /**
     * Denormalize từ MAX(SanLuong.ngayThucHien WHERE trangThai='done') — ngày thi công gần
     * nhất. Dùng làm tiêu chí sort trong danh sách sản lượng để đưa trạm/tuyến thi công mới
     * lên đầu. Chỉ update khi ngày mới >= ngày đang lưu (idempotent).
     * Phải tự cập nhật ở mọi chỗ ghi SanLuong (xem {@code HopDongDoiTuongService.touchConstructionDate}).
     */
    @Column(name = "ngay_thi_cong_gan_nhat")
    private LocalDate ngayThiCongGanNhat;

    /** Denormalize từ MIN(SanLuong.ngayThucHien WHERE trangThai='done') — ngày khởi công; null: chưa khởi công. Do business-api cập nhật. */
    @Column(name = "ngay_khoi_cong")
    private LocalDate ngayKhoiCong;

    /**
     * Denormalize từ SUM(SanLuong.khoiLuongHoanThanh × donGia) + boSungSanLuong — xem
     * HopDongDoiTuongRepository.recalculateSanLuongHieuLuc. Tự cập nhật ở SanLuongConsumer
     * (mọi lần sanluong-changed) và khi boSungSanLuong đổi trực tiếp.
     */
    @Column(name = "san_luong_hieu_luc", precision = 18, scale = 2)
    private BigDecimal sanLuongHieuLuc;

    /**
     * Denormalize từ VuongMac đang mở (pending/in_progress) của đối tượng này — xem
     * VuongMacConsumer.applyVuongMacSnapshotSlice. Dùng để loại đối tượng "đang vướng mắc"
     * khỏi truy vấn "chờ quyết toán"/"quá hạn" ngay trong SQL (xem HopDongDoiTuongTonService).
     */
    @Column(name = "co_vuong_mac_mo", nullable = false)
    private Boolean coVuongMacMo = false;

    /**
     * Nhà thầu phụ trách đối tượng (FK nguoi_dung) — nguồn chân lý duy nhất, thay cho việc
     * suy luận qua phan_cong/thuộc tính động (EAV) như trước. Sửa được sau khi tạo qua API
     * cập nhật đối tượng thường (không tự đồng bộ từ phan_cong nữa).
     */
    @Column(name = "nha_thau_id")
    private UUID nhaThauId;

    /**
     * Khu vực phụ trách đối tượng (FK khu_vuc) — nguồn chân lý duy nhất, giống nhaThauId,
     * thay cho việc suy luận qua thuộc tính động (EAV) mỗi lần enrich (xem
     * HopDongDanhSachDoiTuongGroupSupport.resolveObjectGeo).
     */
    @Column(name = "khu_vuc_id")
    private UUID khuVucId;

    /**
     * Tỉnh/thành phụ trách đối tượng (FK tinh_thanh) — nguồn chân lý duy nhất, giống
     * nhaThauId, thay cho việc suy luận qua thuộc tính động (EAV) mỗi lần enrich.
     */
    @Column(name = "tinh_thanh_id")
    private UUID tinhThanhId;

    /** Lý do hủy — chỉ có ý nghĩa khi trangThaiHopDongId đang trỏ tới bước "Hủy" của luồng
     * trạng thái; giữ nguyên (không tự xoá) nếu sau đó chuyển sang trạng thái khác. */
    @Column(name = "ly_do_huy", columnDefinition = "text")
    private String lyDoHuy;

    /** Ngày bàn giao mặt bằng — dùng KPI tiến độ HĐ thi công. */
    @Column(name = "ngay_ban_giao_mat_bang")
    private LocalDate ngayBanGiaoMatBang;

    @Column(name = "trang_thai_vcontract_bgm", length = 20)
    private String trangThaiVcontractBgm;

    @Column(name = "ngay_yeu_cau_vat_tu_b")
    private LocalDate ngayYeuCauVatTuB;

    @Column(name = "ngay_hoan_thanh_vat_tu_b")
    private LocalDate ngayHoanThanhVatTuB;

    /** CHUA_DAM_BAO | DA_DAM_BAO | ... */
    @Column(name = "trang_thai_vat_tu_a", length = 30)
    private String trangThaiVatTuA = "CHUA_DAM_BAO";

    /** CHUA_YEU_CAU | DA_YEU_CAU | DA_NHAN | ... */
    @Column(name = "trang_thai_vat_tu_b", length = 30)
    private String trangThaiVatTuB = "CHUA_YEU_CAU";

    @Column(name = "co_phat_sinh", nullable = false)
    private Boolean coPhatSinh = false;

    /** CHUA_NT | DANG_NT | DA_NT | ... */
    @Column(name = "trang_thai_nghiem_thu", length = 30)
    private String trangThaiNghiemThu = "CHUA_NT";

    @Column(name = "ngay_nghiem_thu")
    private LocalDate ngayNghiemThu;

    @Column(name = "da_chot_tham", nullable = false)
    private Boolean daChotTham = false;

    @Column(name = "gia_tri_chot_tham", precision = 18, scale = 2)
    private BigDecimal giaTriChotTham;

    @Column(name = "da_giao_thau_phu", nullable = false)
    private Boolean daGiaoThauPhu = false;

    @Column(name = "da_ky_hd_thau_phu", nullable = false)
    private Boolean daKyHdThauPhu = false;

    @Column(name = "ngay_ky_hd_thau_phu")
    private LocalDate ngayKyHdThauPhu;

    /** Khóa sửa danh sách/trạm khi đã bắt đầu thi công. */
    @Column(name = "danh_sach_khoa", nullable = false)
    private Boolean danhSachKhoa = false;

    /** Giai đoạn trạm theo luồng biên bản thi công — tự cập nhật khi xuất/duyệt biên bản. */
    @Column(name = "giai_doan_thi_cong_ten", length = 120)
    private String giaiDoanThiCongTen;
}
