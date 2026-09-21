package vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.util;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hangmuc.dto.HangMucRowType;

import java.math.BigDecimal;
import java.util.UUID;

final class HangMucCalcGridRow {
    int rowNumber;
    UUID id;
    UUID nhomId;
    UUID parentItemId;
    HangMucRowType type;
    String ma;
    String ten;
    BigDecimal khoiLuong;
    BigDecimal donGia;
    String congThucKhoiLuong;
    String congThucDonGia;
    String congThucThanhTien;
}
