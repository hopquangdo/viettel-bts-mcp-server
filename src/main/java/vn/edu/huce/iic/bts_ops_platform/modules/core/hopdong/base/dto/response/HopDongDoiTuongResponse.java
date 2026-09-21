package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class HopDongDoiTuongResponse {
    private UUID id;
    private UUID hopDongId;
    private UUID doiTuongQuanLyId;
    private UUID trangThaiHopDongId;
    private UUID hopDongNhomUuTienId;
    private UUID nhaThauId;
    private String nhaThauTen;
    private UUID khuVucId;
    private UUID tinhThanhId;
    private String nhomUuTienTen;
    private String nhomUuTienMauSac;
    private String trangThaiMa;
    private String trangThaiTen;
    private String trangThaiMauSac;
    private String doiTuongMa;
    private String doiTuongTen;
    private BigDecimal quyetToanThuc;
    private BigDecimal boSungSanLuong;
    /** Denormalize — xem HopDongDoiTuong.sanLuongHieuLuc (SUM san_luong + boSungSanLuong). */
    private BigDecimal sanLuongHieuLuc;
    /** Denormalize — xem HopDongDoiTuong.coVuongMacMo. */
    private Boolean coVuongMacMo;
    private LocalDate ngayHtTc;
    private LocalDate constructionDate;
    private Boolean hoatDong;
    private String lyDoHuy;
    private String trangThaiVatTuA;
    private String trangThaiVatTuB;
    private Boolean daGiaoThauPhu;
    private Boolean daKyHdThauPhu;
    private LocalDate ngayKyHdThauPhu;
    private LocalDate ngayHoanThanhVatTuB;
    /** Giai đoạn trạm theo luồng biên bản thi công. */
    private String giaiDoanThiCongTen;
    private List<HopDongDoiTuongGiaTriResponse> giaTri = new ArrayList<>();
    private Instant ngayTao;
    private Instant ngayCapNhat;
}
