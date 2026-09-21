package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository;

import java.math.BigDecimal;

/** Kết quả 1 dòng của HopDongRepository.tongQuanAggregate — xem VolumeServiceImpl.tongQuan(). */
public interface VolumeTongQuanAggregateRow {
    long getSoHopDong();
    Long getGiaTriHopDong();
    BigDecimal getTongThanhTienThiCong();
    BigDecimal getTongQuyetToan();
    long getSoVuotNguong();
    long getSoCanhBao();
    long getSoThieu();
    long getSoThua();
    long getSoCanBang();
    long getSoDaQuyetToan();
    long getSoDangQuyetToan();
    long getSoChuaQuyetToan();
    long getTongTram();
    long getSoHopDongDaThiCong();
    long getTongTramDaThiCong();
    long getSoTramBatThuong();
    long getSoHopDongThieuLon();
    long getTongTramDaQuyetToan();
    long getTongTramDangQuyetToan();
    long getTongTramChuaQuyetToan();
    Long getGiaTriDaQuyetToan();
    Long getGiaTriDangQuyetToan();
    Long getGiaTriChuaQuyetToan();
}
