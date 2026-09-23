package vn.edu.huce.iic.bts_ops_platform.mcp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.bienban.BienBanQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hosodoituong.HoSoDoiTuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nganho.NganHoQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec.NguonViecQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhathau.NhaThauQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhatky.NhatKyQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.phancong.PhanCongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.sanluong.SanLuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.tramton.TramTonQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.vuongmac.VuongMacQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.handler.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.metrics.McpToolStats;
import vn.edu.huce.iic.bts_ops_platform.mcp.handler.NganHoToolHandler;
import vn.edu.huce.iic.bts_ops_platform.mcp.handler.NhaThauToolHandler;
import vn.edu.huce.iic.bts_ops_platform.mcp.handler.VuongMacToolHandler;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/v1/tools")
@RequiredArgsConstructor
@Tag(name = "Tool Test", description = "Debug: gọi trực tiếp handler của từng tool AI để xem kết quả")
public class ToolTestController {

    private final SanLuongToolHandler sanLuongToolHandler;
    private final VuongMacToolHandler vuongMacToolHandler;
    private final HopDongToolHandler hopDongToolHandler;
    private final HoSoDoiTuongToolHandler hoSoDoiTuongToolHandler;
    private final NganHoToolHandler nganHoToolHandler;
    private final NguonViecToolHandler nguonViecToolHandler;
    private final NhaThauToolHandler nhaThauToolHandler;
    private final NhatKyToolHandler nhatKyToolHandler;
    private final PhanCongToolHandler phanCongToolHandler;
    private final TramTonToolHandler tramTonToolHandler;
    private final BienBanToolHandler bienBanToolHandler;
    private final ObjectProvider<McpToolStats> toolStats;

    @GetMapping("/san-luong")
    @Operation(summary = "Test tool AI module Sản lượng")
    public ResponseEntity<ApiResponse<SanLuongQueryResponse>> sanLuong(
            @RequestParam(required = false) String maDoiTuong,
            @RequestParam(required = false) String maHopDong,
            @RequestParam(required = false) String nhaThau,
            @RequestParam(required = false) String khuVuc,
            @RequestParam(required = false) String tinhThanh,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Double nguongHoanThanhThap,
            @RequestParam(required = false) Boolean includeWithoutOutput,
            @RequestParam(required = false) String sapXep,
            @RequestParam(required = false) String loaiHopDong,
            @RequestParam(required = false) String xepHangTheo,
            @RequestParam(required = false) Boolean tangDan) {
        SanLuongQueryResponse result = sanLuongToolHandler.query(
                maDoiTuong, maHopDong, nhaThau, khuVuc, tinhThanh, fromDate, toDate, page, pageSize, nguongHoanThanhThap, includeWithoutOutput,
                sapXep, loaiHopDong, xepHangTheo, tangDan);
        return ApiResponse.<SanLuongQueryResponse>build().withData(result).toEntity();
    }

    @GetMapping("/vuong-mac")
    @Operation(summary = "Test tool AI module Vướng mắc")
    public ResponseEntity<ApiResponse<VuongMacQueryResponse>> vuongMac(
            @RequestParam(required = false) String maDoiTuong,
            @RequestParam(required = false) String maHopDong,
            @RequestParam(required = false) String nhaThau,
            @RequestParam(required = false) String khuVuc,
            @RequestParam(required = false) String tinhThanh,
            @RequestParam(required = false) String loaiHopDong,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String kieuVuongMac,
            @RequestParam(required = false) String giaiDoan,
            @RequestParam(required = false) Boolean dangMoOnly,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer top,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sinceDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer quaHanNgay,
            @RequestParam(required = false) String canBo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        VuongMacQueryResponse result = vuongMacToolHandler.query(
                maDoiTuong, maHopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, trangThai, kieuVuongMac, giaiDoan, dangMoOnly, query, top, sinceDate, page, pageSize, quaHanNgay, canBo, fromDate, toDate);
        return ApiResponse.<VuongMacQueryResponse>build().withData(result).toEntity();
    }

    @GetMapping("/hop-dong")
    @Operation(summary = "Test tool AI module Hợp đồng")
    public ResponseEntity<ApiResponse<HopDongQueryResponse>> hopDong(
            @RequestParam(required = false) String maDoiTuong,
            @RequestParam(required = false) String maHopDong,
            @RequestParam(required = false) String nhaThau,
            @RequestParam(required = false) String khuVuc,
            @RequestParam(required = false) String tinhThanh,
            @RequestParam(required = false) String loaiHopDong,
            @RequestParam(required = false) String kieuHopDong,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Double nguongChamTienDo,
            @RequestParam(required = false) Double nguongXanh,
            @RequestParam(required = false) Double nguongVang,
            @RequestParam(required = false) Integer top,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String loaiNgay) {
        HopDongQueryResponse result = hopDongToolHandler.query(
                maDoiTuong, maHopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, kieuHopDong, query, page, pageSize, nguongChamTienDo, nguongXanh, nguongVang, top, fromDate, toDate, loaiNgay);
        return ApiResponse.<HopDongQueryResponse>build().withData(result).toEntity();
    }

    @GetMapping("/ho-so-doi-tuong")
    @Operation(summary = "Test khối hoSoDoiTuong của tool AI doituong_tool")
    public ResponseEntity<ApiResponse<HoSoDoiTuongQueryResponse>> hoSoDoiTuong(
            @RequestParam(required = false) String maDoiTuong,
            @RequestParam(required = false) String maHopDong,
            @RequestParam(required = false) String nhaThau,
            @RequestParam(required = false) String khuVuc,
            @RequestParam(required = false) String tinhThanh,
            @RequestParam(required = false) String trangThaiHopDong,
            @RequestParam(required = false) Boolean coNhomUuTien,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        HoSoDoiTuongQueryResponse result = hoSoDoiTuongToolHandler.query(maDoiTuong, maHopDong, nhaThau, khuVuc, tinhThanh, trangThaiHopDong, coNhomUuTien, fromDate, toDate, page, pageSize);
        return ApiResponse.<HoSoDoiTuongQueryResponse>build().withData(result).toEntity();
    }

    @GetMapping("/ngan-ho")
    @Operation(summary = "Test tool AI module Ngân sách hợp đồng (Volume)")
    public ResponseEntity<ApiResponse<NganHoQueryResponse>> nganHo(
            @RequestParam(required = false) String maHopDong,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double heSo,
            @RequestParam(required = false) Double nguongCanhBao,
            @RequestParam(required = false) String loaiHopDong,
            @RequestParam(required = false) String statusFilter,
            @RequestParam(required = false) String khuVuc,
            @RequestParam(required = false) String tinhThanh,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        NganHoQueryResponse result = nganHoToolHandler.query(maHopDong, query, heSo, nguongCanhBao, loaiHopDong, statusFilter, khuVuc, tinhThanh, page, pageSize);
        return ApiResponse.<NganHoQueryResponse>build().withData(result).toEntity();
    }

    @GetMapping("/nguon-viec")
    @Operation(summary = "Test tool AI module Nguồn việc")
    public ResponseEntity<ApiResponse<NguonViecQueryResponse>> nguonViec(
            @RequestParam(required = false) String khuVuc,
            @RequestParam(required = false) String nhaThau,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String loaiCv,
            @RequestParam(required = false) String phapLy,
            @RequestParam(required = false) String linhVuc,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer top,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Double nguongSapHet) {
        NguonViecQueryResponse result = nguonViecToolHandler.query(
                khuVuc, nhaThau, trangThai, loaiCv, phapLy, linhVuc, tab, fromDate, toDate, query, top, page, pageSize, nguongSapHet);
        return ApiResponse.<NguonViecQueryResponse>build().withData(result).toEntity();
    }

    @GetMapping("/nha-thau")
    @Operation(summary = "Test tool AI Nhà thầu (liệt kê các nhà thầu)")
    public ResponseEntity<ApiResponse<NhaThauQueryResponse>> nhaThau(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String maHopDong,
            @RequestParam(required = false) String khuVuc,
            @RequestParam(required = false) String tinhThanh,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        NhaThauQueryResponse result = nhaThauToolHandler.query(query, maHopDong, khuVuc, tinhThanh, page, pageSize);
        return ApiResponse.<NhaThauQueryResponse>build().withData(result).toEntity();
    }

    @GetMapping("/nhat-ky")
    @Operation(summary = "Test tool AI Nhật ký thao tác (ai làm gì, lúc nào)")
    public ResponseEntity<ApiResponse<NhatKyQueryResponse>> nhatKy(
            @RequestParam(required = false) String maDoiTuong,
            @RequestParam(required = false) String maHopDong,
            @RequestParam(required = false) String hanhDong,
            @RequestParam(required = false) String nguoiThucHien,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String thuTu,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        var result = nhatKyToolHandler.query(maDoiTuong, maHopDong, hanhDong, nguoiThucHien, fromDate, toDate, thuTu, page, pageSize);
        return ApiResponse.<NhatKyQueryResponse>build().withData(result).toEntity();
    }

    @GetMapping("/phan-cong")
    @Operation(summary = "Test tool AI module Phân công")
    public ResponseEntity<ApiResponse<PhanCongQueryResponse>> phanCong(
            @RequestParam(required = false) String maVung,
            @RequestParam(required = false) String nhaThau,
            @RequestParam(required = false) String canBo,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String maHopDong,
            @RequestParam(required = false) String khuVuc,
            @RequestParam(required = false) String tinhThanh,
            @RequestParam(required = false) String maDoiTuong,
            @RequestParam(required = false) String giaiDoan,
            @RequestParam(required = false) Boolean lichSu,
            @RequestParam(required = false) Integer top,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        PhanCongQueryResponse result = phanCongToolHandler.query(
                maVung, nhaThau, canBo, query, maHopDong, khuVuc, tinhThanh, maDoiTuong, giaiDoan, lichSu, top, page, pageSize, fromDate, toDate);
        return ApiResponse.<PhanCongQueryResponse>build().withData(result).toEntity();
    }

    @GetMapping("/tram-ton")
    @Operation(summary = "Test tool AI module Trạm tồn (đối tượng tồn)")
    public ResponseEntity<ApiResponse<TramTonQueryResponse>> tramTon(
            @RequestParam(required = false) String maDoiTuong,
            @RequestParam(required = false) String maHopDong,
            @RequestParam(required = false) String nhaThau,
            @RequestParam(required = false) String khuVuc,
            @RequestParam(required = false) String tinhThanh,
            @RequestParam(required = false) String loaiHopDong,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sinceDate,
            @RequestParam(required = false) Integer quaHanNgay,
            @RequestParam(required = false) Integer top,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer soNgayThieuCapNhat,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        TramTonQueryResponse result = tramTonToolHandler.query(
                maDoiTuong, maHopDong, nhaThau, khuVuc, tinhThanh, loaiHopDong, tab, sinceDate, quaHanNgay, top, page, pageSize, soNgayThieuCapNhat, fromDate, toDate);
        return ApiResponse.<TramTonQueryResponse>build().withData(result).toEntity();
    }

    @GetMapping("/bien-ban")
    @Operation(summary = "Test tool AI module Biên bản")
    public ResponseEntity<ApiResponse<BienBanQueryResponse>> bienBan(
            @RequestParam(required = false) String khuVuc,
            @RequestParam(required = false) String tinhThanh,
            @RequestParam(required = false) String maHopDong,
            @RequestParam(required = false) String nhaThau,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String maDoiTuong,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        BienBanQueryResponse result = bienBanToolHandler.query(
                khuVuc, tinhThanh, maHopDong, nhaThau, trangThai, maDoiTuong, fromDate, toDate, page, pageSize);
        return ApiResponse.<BienBanQueryResponse>build().withData(result).toEntity();
    }

    @GetMapping("/thong-ke-hieu-nang")
    @Operation(summary = "Báo cáo hiệu năng tool MCP: số lần gọi, thời gian min/TB/p50/p95/max và số câu SQL mỗi lần gọi (từ lúc khởi động hoặc lần reset)")
    public ResponseEntity<ApiResponse<McpToolStats.Report>> thongKeHieuNang(
            @RequestParam(required = false, defaultValue = "false") boolean reset) {
        McpToolStats stats = toolStats.getIfAvailable();
        McpToolStats.Report report = stats != null ? stats.report(reset)
                : new McpToolStats.Report(java.time.Instant.now(), java.time.Instant.now(), java.util.List.of());
        return ApiResponse.<McpToolStats.Report>build().withData(report).toEntity();
    }
}
