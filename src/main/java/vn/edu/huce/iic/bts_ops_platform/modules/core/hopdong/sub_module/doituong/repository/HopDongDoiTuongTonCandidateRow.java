package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Projection cho HopDongDoiTuongRepository.findChoQuyetToan/findQuaHan. */
public interface HopDongDoiTuongTonCandidateRow {
    UUID getId();

    UUID getHopDongId();

    String getMaHopDong();

    Long getGiaTriHd();

    LocalDate getResolvedNgayHt();

    /** Denormalize — xem HopDongDoiTuong.sanLuongHieuLuc. Ưu tiên hơn chiaDeuGiaTriHd. */
    BigDecimal getSanLuongHieuLuc();
}
