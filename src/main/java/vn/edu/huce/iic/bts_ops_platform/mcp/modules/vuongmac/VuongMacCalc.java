package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac;

import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.query.VuongMacTongQuanQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class VuongMacCalc {

    private static final List<String> KIEU_VUONG_MAC = List.of(
            "giai_phong_mat_bang", "thiet_ke", "thu_tuc_phap_ly", "nguon_vat_lieu", "thi_cong");

    public VuongMacTongQuanResponse tongQuan(VuongMacTongQuanQuery query) {
        Map<String, Long> theoLoai = new LinkedHashMap<>();
        for (String kieu : KIEU_VUONG_MAC) {
            theoLoai.put(StatusLabels.kieuVuongMac(kieu), 0L);
        }
        if (query.soTheoKieu() != null) {
            query.soTheoKieu().forEach((kieu, soLuong) ->
                    theoLoai.put(StatusLabels.kieuVuongMac(kieu), soLuong));
        }

        return new VuongMacTongQuanResponse(
                query.tongSo(),
                query.choXuLy(),
                query.dangXuLy(),
                query.daXuLy(),
                query.tuChoi(),
                query.choXuLy() + query.dangXuLy(),
                query.quaHan30Ngay(),
                query.daXuLyTrongKy(),
                Collections.unmodifiableMap(theoLoai),
                query.soHopDongCoVuongMacMo(),
                query.soDoiTuongCoVuongMacMo());
    }
}