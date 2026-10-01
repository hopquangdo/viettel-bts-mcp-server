package vn.edu.huce.iic.bts_ops_platform.mcp.handler;

import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongQueryResponse;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

/** Contract logic cho tool AI module Sản lượng — tự thu hẹp theo filter truyền vào. */
public interface SanLuongToolHandler {

    /** Các khối của {@link SanLuongQueryResponse}; tool nhỏ chỉ yêu cầu khối cần để khỏi chạy truy vấn thừa. */
    enum Block {
        /** period, summary, progress, trend */
        TONG_QUAN,
        /** nhaThau, khuVuc, tinh, hopDong, loaiHopDong, doiTuong */
        XEP_HANG,
        /** periodTrend */
        XU_HUONG,
        /** nhaThauChuaBaoTrongKy, nhaThauDaBaoTrongKy */
        NHA_THAU_BAO,
        /** nghiemThu */
        NGHIEM_THU,
        /** doiTuongChuaCoSanLuong */
        DOI_TUONG_CHUA_CO,
        /** danhSachDoiTuong */
        DANH_SACH_DOI_TUONG,
        /** hangMucDoiTuong */
        HANG_MUC
    }

    /** Đủ mọi khối (sanluong_tool gốc). */
    default SanLuongQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                                       String khuVuc, String tinhThanh, LocalDate fromDate, LocalDate toDate,
                                       Integer page, Integer pageSize, Double nguongHoanThanhThap, Boolean includeWithoutOutput,
                                       String sapXep, String loaiHopDong, String xepHangTheo, Boolean tangDan) {
        return query(doiTuong, hopDong, nhaThau, khuVuc, tinhThanh, fromDate, toDate, page, pageSize, nguongHoanThanhThap,
                includeWithoutOutput, sapXep, loaiHopDong, xepHangTheo, tangDan, EnumSet.allOf(Block.class));
    }

    /** nhaThau: ID, mã hoặc tên nhà thầu; khuVuc/tinhThanh dùng FK chuẩn trên đối tượng hợp đồng. Khối không có trong blocks trả null. */
    SanLuongQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                                String khuVuc, String tinhThanh, LocalDate fromDate, LocalDate toDate,
                                Integer page, Integer pageSize, Double nguongHoanThanhThap, Boolean includeWithoutOutput,
                                String sapXep, String loaiHopDong, String xepHangTheo, Boolean tangDan, Set<Block> blocks);
}
