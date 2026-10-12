package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Dữ liệu nội bộ module Nguồn việc (đơn vị tỷ đồng) -> DTO trả cho LLM (tiền/%/ngày đã định dạng). */
public final class NguonViecToolMapper {

    private NguonViecToolMapper() {
    }

    public static NguonViecXepHangResponse xepHang(NguonViecXepHangChieu chieu, String tieuChi, LocalDate tuNgay, LocalDate denNgay,
                                                   List<RankedItemResponse> rows) {
        List<NguonViecXepHangItem> items = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            RankedItemResponse r = rows.get(i);
            double value = r.getValue() instanceof Number n ? n.doubleValue() : 0d;
            if (chieu == NguonViecXepHangChieu.RUI_RO) {
                items.add(new NguonViecXepHangItem(i + 1, r.getLabel(), value, null, null, null));
            } else {
                Map<String, Object> extra = r.getExtra() != null ? r.getExtra() : Map.of();
                Object giaTri = extra.get("giaTriHD");
                Object soHd = extra.get("soHopDong");
                items.add(new NguonViecXepHangItem(i + 1, r.getLabel(), null, McpFormat.tienTuTy(value),
                        giaTri instanceof Number g ? McpFormat.tienTuTy(g.doubleValue()) : null,
                        soHd instanceof Number n ? n.intValue() : null));
            }
        }
        return new NguonViecXepHangResponse(McpFormat.ky(tuNgay, denNgay), chieu.label(), tieuChi, items,
                items.isEmpty() ? "Không có hợp đồng nguồn việc nào trong kỳ." : null);
    }

    public static NguonViecTongQuanResponse tongQuan(LocalDate tuNgay, LocalDate denNgay, NguonViecSummaryToolDto sum,
                                                     NguonViecTabCountsToolDto tab) {
        return new NguonViecTongQuanResponse(McpFormat.ky(tuNgay, denNgay),
                McpFormat.tienTuTy(sum.getGiaTriHD()), McpFormat.tienTuTy(sum.getSxTong()), McpFormat.tienTuTy(sum.getDtTong()),
                McpFormat.tienTuTy(sum.getDtConSL()), McpFormat.tienTuTy(sum.getSlConHD()), McpFormat.tienTuTy(sum.getChuaKhaThi()),
                tab.getAll(), tab.getActive(), tab.getNearly());
    }

    public static NguonViecSapHetGiaTriResponse sapHetGiaTri(LocalDate tuNgay, LocalDate denNgay, double nguongPhanTram,
                                                             List<NguonViecRowToolItem> rows) {
        List<NguonViecSapHetGiaTriItem> items = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            NguonViecRowToolItem r = rows.get(i);
            items.add(new NguonViecSapHetGiaTriItem(i + 1, r.getMaHD(), r.getChuongTrinh(),
                    McpFormat.tienTuTy(r.getSlConHD()), McpFormat.tienTuTy(r.getGiaTriHD()),
                    r.getGiaTriHD() > 0 ? McpFormat.phanTram(r.getSlConHD() / r.getGiaTriHD() * 100.0) : null,
                    r.getTrungTam() != null && !r.getTrungTam().isBlank() ? r.getTrungTam() : null));
        }
        return new NguonViecSapHetGiaTriResponse(McpFormat.ky(tuNgay, denNgay), McpFormat.phanTram(nguongPhanTram), items.size(),
                items, items.isEmpty() ? "Không có hợp đồng nào còn dưới " + McpFormat.phanTram(nguongPhanTram)
                + " giá trị được giao trong kỳ." : null);
    }
}
