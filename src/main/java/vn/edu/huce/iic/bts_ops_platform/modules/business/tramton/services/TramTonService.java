package vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.services;

import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response.TramTonChiTietResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response.TramTonItemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.tramton.dto.response.TramTonTongQuanResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface TramTonService {

    TramTonTongQuanResponse tongQuan(
            UUID loaiHopDongId,
            String trungTam,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer quaHanNgay,
            LocalDate ngayBaoCao);

    PageResponse<TramTonItemResponse> danhSach(
            UUID loaiHopDongId,
            String trungTam,
            LocalDate dateFrom,
            LocalDate dateTo,
            String tab,
            String search,
            Integer page,
            Integer size,
            Integer quaHanNgay,
            LocalDate ngayBaoCao);

    TramTonChiTietResponse chiTiet(UUID doiTuongId, Integer quaHanNgay, LocalDate ngayBaoCao);

    TramTonChiTietResponse chiTiet(
            UUID doiTuongId,
            Integer quaHanNgay,
            LocalDate ngayBaoCao,
            boolean refresh);
}
