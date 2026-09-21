package vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.service;

import vn.edu.huce.iic.bts_ops_platform.modules.core.baomat.dto.response.DanhMucDuLieuTrongYeuResponse;

import java.util.List;

public interface DanhMucDuLieuTrongYeuService {

    List<DanhMucDuLieuTrongYeuResponse> list(Boolean activeOnly);
}
