package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

final class HopDongMutableTinhRow {
    final String id;
    final String province;
    String tinhMoi = "";
    String oldProvince = "";
    String contractor;
    long cc;
    /** Tư vấn thiết kế — chưa lên sản lượng (không HT/Hủy). */
    long chuaLamSanLuong;
    /** Tư vấn thiết kế — đã lên sản lượng, chưa hoàn thành (không HT/Hủy). */
    long dangLamSanLuong;
    /** Tư vấn thiết kế — đã hoàn thành (ngày HT / trạng thái hoàn thành luồng). */
    long hoanThanhSanLuong;
    /** Tổng % tiến độ từng đối tượng — chia cho cc khi ra tyLeHoanThanh. */
    double sumTyLeHoanThanh;
    BigDecimal contractValue = BigDecimal.ZERO;
    BigDecimal constructionValue = BigDecimal.ZERO;
    final Map<String, Long> statusByMa = new LinkedHashMap<>();

    HopDongMutableTinhRow(String id, String province, String contractor) {
        this.id = id;
        this.province = province;
        this.contractor = contractor;
    }
}
