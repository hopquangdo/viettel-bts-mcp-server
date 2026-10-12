package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.request;

import vn.edu.huce.iic.bts_ops_platform.mcp.common.loc.KyLoc;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.loc.PhamViLoc;

import java.time.LocalDate;

/** Raw shared and module-specific filters for VuongMac operations. */
public record VuongMacFilter(
        PhamViLoc phamVi,
        KyLoc ky,
        String trangThai,
        String kieuVuongMac,
        String giaiDoan,
        Boolean dangMoOnly,
        String canBo,
        String thieuNhanLuc) {

    public VuongMacFilter(
            String doiTuong,
            String hopDong,
            String nhaThau,
            String khuVuc,
            String tinhThanh,
            String loaiHopDong,
            String trangThai,
            String kieuVuongMac,
            String giaiDoan,
            Boolean dangMoOnly,
            String canBo,
            LocalDate fromDate,
            LocalDate toDate) {
        this(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, trangThai, kieuVuongMac, giaiDoan, dangMoOnly,
                canBo, fromDate, toDate, null);
    }

    /** {@code thieuNhanLuc}: nguoi_xu_ly | nha_thau | bat_ky -- xem {@code VuongMacToolRepository#search}. */
    public VuongMacFilter(
            String doiTuong,
            String hopDong,
            String nhaThau,
            String khuVuc,
            String tinhThanh,
            String loaiHopDong,
            String trangThai,
            String kieuVuongMac,
            String giaiDoan,
            Boolean dangMoOnly,
            String canBo,
            LocalDate fromDate,
            LocalDate toDate,
            String thieuNhanLuc) {
        this(
                new PhamViLoc(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong),
                new KyLoc(fromDate, toDate, null),
                trangThai,
                kieuVuongMac,
                giaiDoan,
                dangMoOnly,
                canBo,
                thieuNhanLuc);
    }
}