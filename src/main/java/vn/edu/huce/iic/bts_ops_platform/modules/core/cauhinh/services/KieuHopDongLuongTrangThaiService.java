package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.services;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.request.KieuHopDongLuongTrangThaiGanRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.KieuHopDongLuongTrangThaiResponse;

import java.util.List;
import java.util.UUID;

public interface KieuHopDongLuongTrangThaiService {

    List<KieuHopDongLuongTrangThaiResponse> list(UUID loaiHopDongId, UUID kieuHopDongId);

    KieuHopDongLuongTrangThaiResponse getByKieuAndLoai(UUID kieuHopDongId, UUID loaiHopDongId);

    List<KieuHopDongLuongTrangThaiResponse> listByLuongTrangThaiId(UUID luongTrangThaiId);

    KieuHopDongLuongTrangThaiResponse gan(KieuHopDongLuongTrangThaiGanRequest request);

    void remove(UUID kieuHopDongId, UUID loaiHopDongId);
}
