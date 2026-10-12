package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.query.HopDongTienDoQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.response.HopDongCanhBaoItemResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.response.HopDongCanhBaoTienDoResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Pure calculations for contract progress tools. */
public final class HopDongCalc {

    public static final double DEFAULT_NGUONG_XANH = 90.0;
    public static final double DEFAULT_NGUONG_VANG = 70.0;

    private HopDongCalc() {
    }

    public static HopDongCanhBaoTienDoResponse canhBaoTienDo(
            List<HopDongTienDoQuery> hopDongs,
            double nguongXanh,
            double nguongVang) {
        long soXanh = 0;
        long soVang = 0;
        long soDo = 0;
        List<HopDongCanhBaoItemResponse> hopDongDo = new ArrayList<>();

        for (HopDongTienDoQuery hopDong : hopDongs) {
            BigDecimal tongKhoiLuong = hopDong.tongKhoiLuong();
            if (tongKhoiLuong == null || tongKhoiLuong.signum() <= 0) {
                continue;
            }

            BigDecimal sanLuong = hopDong.sanLuongHoanThanh() != null
                    ? hopDong.sanLuongHoanThanh()
                    : BigDecimal.ZERO;
            double tyLe = sanLuong.divide(tongKhoiLuong, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();

            if (tyLe >= nguongXanh) {
                soXanh++;
            } else if (tyLe >= nguongVang) {
                soVang++;
            } else {
                soDo++;
                hopDongDo.add(new HopDongCanhBaoItemResponse(
                        hopDong.hopDongId(), hopDong.maHopDong(), hopDong.tenHopDong(), tyLe));
            }
        }

        return new HopDongCanhBaoTienDoResponse(soXanh, soVang, soDo, List.copyOf(hopDongDo));
    }
}