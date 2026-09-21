package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.services;

import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.request.PhanCongGanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface PhanCongDashboardService {

    PhanCongBoLocResponse boLoc();

    PhanCongTongQuanResponse tongQuan(
            UUID hopDongId,
            UUID nhaThauId,
            String giaiDoan,
            UUID khuVucId,
            String search);

    List<PhanCongNhaThauItemResponse> theoNhaThau(
            UUID hopDongId,
            UUID nhaThauId,
            String giaiDoan,
            UUID khuVucId,
            String search);

    List<PhanCongGiaiDoanItemResponse> theoGiaiDoan(
            UUID hopDongId,
            UUID nhaThauId,
            String giaiDoan,
            UUID khuVucId,
            String search);

    List<PhanCongMaVungItemResponse> theoMaVung(
            UUID hopDongId,
            UUID nhaThauId,
            String giaiDoan,
            UUID khuVucId,
            String search);

    PageResponse<PhanCongTramItemResponse> danhSachTram(
            UUID hopDongId,
            UUID nhaThauId,
            String giaiDoan,
            UUID khuVucId,
            String search,
            Integer page,
            Integer size);

    PageResponse<PhanCongLichSuResponse> lichSu(
            UUID hopDongId,
            UUID hopDongDoiTuongId,
            UUID nhaThauId,
            Integer page,
            Integer size);

    int ganNhaThau(PhanCongGanRequest request);
}
