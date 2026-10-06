package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.request;

import java.time.LocalDate;

/** Raw SanLuong filters; null means that the corresponding dimension or date is unrestricted. */
public record SanLuongFilter(
        String doiTuong,
        String hopDong,
        String nhaThau,
        String khuVuc,
        String tinhThanh,
        String loaiHopDong,
        LocalDate fromDate,
        LocalDate toDate) {
}