package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.dto.response.TramThiCongBuocResponse;

import java.util.List;
import java.util.UUID;

public interface TramThiCongBuocService {

    List<TramThiCongBuocResponse> list(UUID hopDongId, UUID hopDongDoiTuongId);

    TramThiCongBuocResponse guiDuyet(UUID hopDongId, UUID hopDongDoiTuongId, String maBuoc);

    TramThiCongBuocResponse duyet(UUID hopDongId, UUID hopDongDoiTuongId, String maBuoc);

    TramThiCongBuocResponse tuChoi(UUID hopDongId, UUID hopDongDoiTuongId, String maBuoc, String lyDoTuChoi);
}
