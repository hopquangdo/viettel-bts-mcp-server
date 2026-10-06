package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacXepHangChieu;

import java.util.ArrayList;
import java.util.List;

/** Dòng xếp hạng nội bộ module Vướng mắc -> DTO trả cho LLM. */
public final class VuongMacToolMapper {

    public static final String TIEU_CHI_XEP_HANG = "Số vướng mắc đang mở, nhiều xuống ít";

    private VuongMacToolMapper() {
    }

    public static VuongMacTongQuanToolResponse tongQuan(VuongMacTongQuanDto tq) {
        return new VuongMacTongQuanToolResponse(tq.getTotal(), tq.getPending(), tq.getInProgress(), tq.getResolved(),
                tq.getRejected(), tq.getPending() + tq.getInProgress(), tq.getOverdue30Days(), tq.getResolvedTrongKy(), tq.getCountsByKieu(),
                tq.getSoHopDongDangMo(), tq.getSoDoiTuongDangMo());
    }

    public static VuongMacXepHangResponse xepHang(VuongMacXepHangChieu chieu, List<RankedItemDto> rows, String ghiChu) {
        if (rows == null) {
            return new VuongMacXepHangResponse(chieu.label(), TIEU_CHI_XEP_HANG, null, ghiChu);
        }
        List<VuongMacXepHangItem> items = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            RankedItemDto r = rows.get(i);
            boolean coTen = r.getName() != null && !r.getName().isBlank();
            items.add(new VuongMacXepHangItem(i + 1, coTen ? r.getLabel() : null, coTen ? r.getName() : r.getLabel(), r.getValue()));
        }
        return new VuongMacXepHangResponse(chieu.label(), TIEU_CHI_XEP_HANG, items,
                items.isEmpty() && ghiChu == null ? "Không có vướng mắc đang mở trong phạm vi đã lọc." : ghiChu);
    }
}
