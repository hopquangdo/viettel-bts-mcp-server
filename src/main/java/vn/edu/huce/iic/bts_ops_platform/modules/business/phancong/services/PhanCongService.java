package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.services;

import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.request.PhanCongCapNhatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.request.PhanCongTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response.PhanCongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.dto.response.PhanCongThongKeResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PhanCongService {

    List<PhanCongResponse> list(String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongId);

    PageResponse<PhanCongResponse> listPage(
            String search, Boolean activeOnly, boolean includeDeleted, UUID hopDongId, Integer page, Integer size);

    /**
     * Danh sách phân công đang hoạt động của 1 cán bộ, tìm theo tên (LIKE, không phân biệt hoa
     * thường) — khớp cả theo nguoiDungId lẫn theo tên nhà thầu (xem PhanCongRepository.
     * findActiveForContractor). Trả về rỗng nếu không tìm thấy cán bộ khớp tên.
     */
    List<PhanCongResponse> theoCanBo(String tenCanBo);

    PhanCongResponse getById(UUID id);

    PhanCongResponse create(PhanCongTaoRequest request);

    PhanCongResponse update(UUID id, PhanCongCapNhatRequest request);

    void delete(UUID id);

    PhanCongThongKeResponse thongKe();

    Map<UUID, String> mapActiveContractorByHopDongDoiTuongId();

    Map<UUID, String> mapActiveContractorByHopDongDoiTuongIds(Collection<UUID> hopDongDoiTuongIds);

    List<String> listDistinctActiveContractors();

    List<String> listDistinctActiveContractors(Collection<UUID> hopDongDoiTuongIds);
}
