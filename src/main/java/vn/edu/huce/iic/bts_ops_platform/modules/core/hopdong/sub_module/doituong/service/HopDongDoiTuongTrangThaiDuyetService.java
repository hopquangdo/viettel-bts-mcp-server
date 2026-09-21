package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service;

import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.request.HopDongDoiTuongTrangThaiBatchRequest;

public interface HopDongDoiTuongTrangThaiDuyetService {

    int yeuCauHuy(HopDongDoiTuongTrangThaiBatchRequest request);

    int yeuCauHoanThanh(HopDongDoiTuongTrangThaiBatchRequest request);

    int xacNhanHuy(HopDongDoiTuongTrangThaiBatchRequest request);

    int xacNhanHoanThanh(HopDongDoiTuongTrangThaiBatchRequest request);

    /** Từ chối yêu cầu chờ xác nhận — trả về trạng thái trước đó. */
    int tuChoiXacNhan(HopDongDoiTuongTrangThaiBatchRequest request);
}
