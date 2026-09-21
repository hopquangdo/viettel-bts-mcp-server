package vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.mapper;

import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.dto.response.HopDongLuuTruLichSuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.dto.response.HopDongLuuTruResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.entity.HopDongLuuTru;
import vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.entity.HopDongLuuTruLichSu;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity.HopDong;

@Component
public class LuuTruMapper {

    public HopDongLuuTruResponse toResponse(
            HopDong hopDong,
            HopDongLuuTru luuTru,
            String khuVuc,
            long soTram,
            double tyLeHoanThanh) {
        String trangThai = luuTru != null && luuTru.getTrangThai() != null
                ? luuTru.getTrangThai()
                : "active";
        return HopDongLuuTruResponse.builder()
                .hopDongId(hopDong.getId())
                .maHopDong(hopDong.getMaHopDong())
                .ten(hopDong.getTen())
                .loaiHopDongId(hopDong.getLoaiHopDongId())
                .kieuHopDongId(hopDong.getKieuHopDongId())
                .trangThai(trangThai)
                .ngayArchive(luuTru != null ? luuTru.getNgayArchive() : null)
                .nguoiArchiveId(luuTru != null ? luuTru.getNguoiArchiveId() : null)
                .nguoiArchiveTen(luuTru != null ? luuTru.getNguoiArchiveTen() : null)
                .ghiChu(luuTru != null ? luuTru.getGhiChu() : null)
                .ngayTao(luuTru != null ? luuTru.getNgayTao() : hopDong.getNgayTao())
                .ngayCapNhat(luuTru != null ? luuTru.getNgayCapNhat() : hopDong.getNgayCapNhat())
                .khuVuc(khuVuc)
                .soTram(soTram)
                .tyLeHoanThanh(tyLeHoanThanh)
                .giaTriHd(hopDong.getGiaTriHd())
                .build();
    }

    public HopDongLuuTruResponse toResponse(HopDongLuuTru entity) {
        return HopDongLuuTruResponse.builder()
                .hopDongId(entity.getHopDongId())
                .trangThai(entity.getTrangThai())
                .ngayArchive(entity.getNgayArchive())
                .nguoiArchiveId(entity.getNguoiArchiveId())
                .nguoiArchiveTen(entity.getNguoiArchiveTen())
                .ghiChu(entity.getGhiChu())
                .ngayTao(entity.getNgayTao())
                .ngayCapNhat(entity.getNgayCapNhat())
                .build();
    }

    public HopDongLuuTruLichSuResponse toLichSuResponse(HopDongLuuTruLichSu entity) {
        return HopDongLuuTruLichSuResponse.builder()
                .hopDongId(entity.getHopDongId())
                .trangThai(entity.getTrangThai())
                .ngayArchive(entity.getNgayArchive())
                .nguoiArchiveId(entity.getNguoiArchiveId())
                .nguoiArchiveTen(entity.getNguoiArchiveTen())
                .ghiChu(entity.getGhiChu())
                .ngayTao(entity.getNgayTao())
                .ngayCapNhat(entity.getNgayTao())
                .build();
    }
}
