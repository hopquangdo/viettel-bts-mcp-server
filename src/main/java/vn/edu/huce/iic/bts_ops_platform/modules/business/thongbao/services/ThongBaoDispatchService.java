package vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.services;

import vn.edu.huce.iic.bts_ops_platform.modules.business.thongbao.entity.ThongBao;

import java.util.Collection;
import java.util.UUID;

public interface ThongBaoDispatchService {

    ThongBao notifyUser(UUID nguoiNhanId, String loai, String tieuDe, String noiDung, String lienKet, UUID thamChieuId);

    void notifyUsers(Collection<UUID> nguoiNhanIds, String loai, String tieuDe, String noiDung, String lienKet, UUID thamChieuId);

    void notifyUsersWithQuyenHan(String quyenHanMa, String loai, String tieuDe, String noiDung, String lienKet, UUID thamChieuId);
}
