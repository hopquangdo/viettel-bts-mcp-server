package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangDong;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpFormat;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NumberUtil;

import java.util.ArrayList;
import java.util.List;

/** Dòng xếp hạng nội bộ module Hợp đồng -> DTO trả cho LLM, và -> DTO cũ của đường tổng hợp (query). */
public final class HopDongToolMapper {

    private HopDongToolMapper() {
    }

    public static HopDongXepHangResponse xepHang(HopDongXepHangChieu chieu, String tieuChi, List<HopDongXepHangDong> rows,
                                                 String ghiChu) {
        if (rows == null) {
            return new HopDongXepHangResponse(chieu.label(), tieuChi, null, ghiChu);
        }
        List<HopDongXepHangItem> items = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            HopDongXepHangDong r = rows.get(i);
            String tyLe = r.soHoanThanh() != null && r.soDoiTuong() > 0
                    ? McpFormat.phanTram(NumberUtil.percent(r.soHoanThanh().intValue(), (int) r.soDoiTuong(), 1))
                    : null;
            items.add(new HopDongXepHangItem(i + 1, r.ten() != null ? r.ten() : "Chưa xác định", r.soHopDong(), r.soDoiTuong(),
                    r.soHoanThanh(), tyLe, r.soVuongMac()));
        }
        return new HopDongXepHangResponse(chieu.label(), tieuChi, items,
                items.isEmpty() && ghiChu == null ? "Không có dữ liệu xếp hạng trong phạm vi đã lọc." : ghiChu);
    }

    public static HopDongTongQuanToolResponse tongQuan(HopDongTongQuanResponse tq, double nguongChamTienDo, Long hoanThanhTrongKy) {
        List<HopDongTongQuanToolResponse.TheoKieu> theoKieu = tq.getSoLuongTheoKieu() == null ? null
                : tq.getSoLuongTheoKieu().stream()
                .map(k -> new HopDongTongQuanToolResponse.TheoKieu(k.getTen() != null ? k.getTen() : k.getMa(), k.getNhom(), k.getSoLuong()))
                .toList();
        return new HopDongTongQuanToolResponse(
                tq.getTongHopDong(),
                tq.getTongHoatDong(),
                tq.getTongDoiTuong(),
                McpFormat.phanTram(tq.getTyLeHoanThanh()),
                tq.getTienDoTheoTrangThai(),
                theoKieu,
                tq.getSoLuongTheoLoai(),
                tq.getSoHopDongChamTienDo(),
                McpFormat.phanTram(nguongChamTienDo),
                tq.getSoHopDongVuongMac(),
                hoanThanhTrongKy);
    }

    public static HopDongXepHangKhuVucToolItem khuVucItem(HopDongXepHangDong r) {
        HopDongXepHangKhuVucToolItem item = new HopDongXepHangKhuVucToolItem();
        item.setKhuVuc(r.ten());
        item.setSoHopDong(r.soHopDong() != null ? r.soHopDong() : 0);
        item.setSoDoiTuong(r.soDoiTuong());
        item.setSoVuongMac(r.soVuongMac());
        return item;
    }

    public static HopDongXepHangTinhToolItem tinhItem(HopDongXepHangDong r) {
        HopDongXepHangTinhToolItem item = new HopDongXepHangTinhToolItem();
        item.setTinh(r.ten());
        item.setSoDoiTuong(r.soDoiTuong());
        item.setSoHoanThanh(r.soHoanThanh() != null ? r.soHoanThanh() : 0);
        item.setSoVuongMac(r.soVuongMac());
        return item;
    }
}
