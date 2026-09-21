package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository;

/** Projection cho HopDongDoiTuongRepository.breakdownTheoLoaiKhuVuc — dùng cho tool AI tra cứu tiến độ theo loại HĐ + khu vực. */
public interface HopDongDoiTuongBreakdownRow {
    Long getTong();

    Long getHoanThanh();

    Long getDangThiCong();

    Long getVuongMac();
}
