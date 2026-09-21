package vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.services;

import vn.edu.huce.iic.bts_ops_platform.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response.NguonLucDangTrienKhaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.request.NguonViecLuuBangRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.nguonluc.dto.response.NguonViecDanhSachResponse;

import java.time.LocalDate;
import java.util.List;

public interface NguonLucService {

    NguonLucDangTrienKhaiResponse dangTrienKhai();

    /**
     * Top N hợp đồng xếp hạng theo 1 tiêu chí: "vuong" (số vướng mắc mở, mặc định), "huy" (số đối
     * tượng hủy), "sanluong_thap" (tỷ lệ sản lượng thực hiện/giá trị HĐ thấp nhất), "at_risk" (đang
     * vướng pháp lý — status vuong_phap_ly).
     */
    List<RankedItemResponse> topHopDong(String metric, int top);

    NguonViecDanhSachResponse danhSach(
            String trungTam,
            String loaiCv,
            String trangThai,
            String phapLy,
            String nhanSu,
            String linhVuc,
            String search,
            String tab,
            LocalDate tuNgay,
            LocalDate denNgay);

    NguonViecDanhSachResponse luuBang(NguonViecLuuBangRequest request);
}
