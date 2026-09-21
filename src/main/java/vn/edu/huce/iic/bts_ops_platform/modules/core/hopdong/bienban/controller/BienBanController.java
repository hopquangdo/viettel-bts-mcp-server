package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.huce.iic.bts_ops_platform.common.dto.ApiResponse;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanHangLoatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanLichSuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanTaoBanThayTheRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanTuChoiHangLoatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanTuChoiRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanXuatDonRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanXuatGopTheoTramRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanXuatRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.service.BienBanExportService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hop-dong")
@RequiredArgsConstructor
@Tag(name = "Biên bản nghiệm thu")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
@RequiresPermission(QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG)
public class BienBanController {

    private final BienBanExportService bienBanExportService;

    @PostMapping("/{hopDongId}/xuat-bien-ban-don")
    @Operation(summary = "Tạo 1 biên bản dạng bản ghi (trước thi công hoặc danh mục chưa có mẫu Word) — ghi lịch sử chờ duyệt")
    public ResponseEntity<ApiResponse<BienBanLichSuResponse>> xuatBienBanDon(
            @PathVariable UUID hopDongId,
            @RequestBody BienBanXuatDonRequest request) {
        return ApiResponse.<BienBanLichSuResponse>build()
                .withData(bienBanExportService.xuatDonTruocThiCong(
                        hopDongId,
                        request.getHopDongDoiTuongIds(),
                        request.getLoaiBienBan(),
                        request.getNgayLap(),
                        request.getNguoiKy()))
                .withMessage("Đã tạo biên bản")
                .toEntity();
    }

    @PostMapping("/{hopDongId}/xuat-bien-ban")
    @Operation(summary = "Xuất hồ sơ nghiệm thu (.zip) cho danh sách đối tượng đã chọn trong 1 hợp đồng")
    public ResponseEntity<byte[]> xuatBienBan(
            @PathVariable UUID hopDongId,
            @RequestBody BienBanXuatRequest request) {
        byte[] zip = bienBanExportService.xuatHangLoat(
                hopDongId, request.getHopDongDoiTuongIds(), request.getNguoiKy(), request.getLoaiBienBan());

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("HoSoNghiemThu.zip", StandardCharsets.UTF_8)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }

    @GetMapping("/{hopDongId}/bien-ban/lich-su")
    @Operation(summary = "Lịch sử các lần xuất biên bản của 1 hợp đồng")
    public ResponseEntity<ApiResponse<List<BienBanLichSuResponse>>> lichSu(@PathVariable UUID hopDongId) {
        return ApiResponse.<List<BienBanLichSuResponse>>build()
                .withData(bienBanExportService.getLichSu(hopDongId))
                .toEntity();
    }

    @PostMapping("/{hopDongId}/bien-ban/{bienBanId}/phe-duyet")
    @Operation(summary = "Duyệt 1 biên bản đang ở trạng thái chờ duyệt")
    @RequiresPermission(QuyenHanMa.PHE_DUYET_BIEN_BAN)
    public ResponseEntity<ApiResponse<BienBanLichSuResponse>> pheDuyet(
            @PathVariable UUID hopDongId, @PathVariable UUID bienBanId) {
        return ApiResponse.<BienBanLichSuResponse>build()
                .withData(bienBanExportService.pheDuyet(hopDongId, bienBanId))
                .toEntity();
    }

    @PostMapping("/{hopDongId}/bien-ban/{bienBanId}/tu-choi")
    @Operation(summary = "Từ chối 1 biên bản đang ở trạng thái chờ duyệt — bắt buộc có lý do")
    @RequiresPermission(QuyenHanMa.PHE_DUYET_BIEN_BAN)
    public ResponseEntity<ApiResponse<BienBanLichSuResponse>> tuChoi(
            @PathVariable UUID hopDongId, @PathVariable UUID bienBanId, @RequestBody BienBanTuChoiRequest request) {
        return ApiResponse.<BienBanLichSuResponse>build()
                .withData(bienBanExportService.tuChoi(hopDongId, bienBanId, request.getLyDo()))
                .toEntity();
    }

    @PostMapping("/{hopDongId}/bien-ban/phe-duyet-hang-loat")
    @Operation(summary = "Duyệt hàng loạt các biên bản đang chờ duyệt")
    @RequiresPermission(QuyenHanMa.PHE_DUYET_BIEN_BAN)
    public ResponseEntity<ApiResponse<List<BienBanLichSuResponse>>> pheDuyetHangLoat(
            @PathVariable UUID hopDongId, @RequestBody BienBanHangLoatRequest request) {
        return ApiResponse.<List<BienBanLichSuResponse>>build()
                .withData(bienBanExportService.pheDuyetHangLoat(hopDongId, request.getIds()))
                .toEntity();
    }

    @PostMapping("/{hopDongId}/bien-ban/tu-choi-hang-loat")
    @Operation(summary = "Từ chối hàng loạt các biên bản đang chờ duyệt — dùng chung 1 lý do")
    @RequiresPermission(QuyenHanMa.PHE_DUYET_BIEN_BAN)
    public ResponseEntity<ApiResponse<List<BienBanLichSuResponse>>> tuChoiHangLoat(
            @PathVariable UUID hopDongId, @RequestBody BienBanTuChoiHangLoatRequest request) {
        return ApiResponse.<List<BienBanLichSuResponse>>build()
                .withData(bienBanExportService.tuChoiHangLoat(hopDongId, request.getIds(), request.getLyDo()))
                .toEntity();
    }

    @GetMapping("/{hopDongId}/bien-ban/{bienBanId}/tai-ve")
    @Operation(summary = "Tải file biên bản trước thi công (.docx)")
    public ResponseEntity<byte[]> taiVeBienBan(@PathVariable UUID hopDongId, @PathVariable UUID bienBanId) {
        byte[] docx = bienBanExportService.taiVeBienBan(hopDongId, bienBanId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("BienBan.docx", StandardCharsets.UTF_8)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(docx);
    }

    @PostMapping("/{hopDongId}/bien-ban/{bienBanId}/xuat-lai")
    @Operation(summary = "Xuất lại (.zip) 1 biên bản đã bị từ chối, dùng đúng đối tượng/người ký đã lưu ở lần xuất gốc")
    public ResponseEntity<byte[]> xuatLai(@PathVariable UUID hopDongId, @PathVariable UUID bienBanId) {
        byte[] zip = bienBanExportService.xuatLai(hopDongId, bienBanId);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("HoSoNghiemThu_XuatLai.zip", StandardCharsets.UTF_8)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }

    @PostMapping("/{hopDongId}/bien-ban/{bienBanId}/tao-ban-thay-the")
    @Operation(summary = "Hủy hiệu lực biên bản đã duyệt và tạo bản thay thế chờ duyệt")
    public ResponseEntity<?> taoBanThayThe(
            @PathVariable UUID hopDongId,
            @PathVariable UUID bienBanId,
            @RequestBody BienBanTaoBanThayTheRequest request) {
        var result = bienBanExportService.taoBanThayThe(hopDongId, bienBanId, request.getLyDo());
        if (result.getZipContent() != null) {
            ContentDisposition disposition = ContentDisposition.attachment()
                    .filename("HoSoNghiemThu_ThayThe.zip", StandardCharsets.UTF_8)
                    .build();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(disposition);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(result.getZipContent());
        }
        return ApiResponse.<BienBanLichSuResponse>build()
                .withData(result.getRecord())
                .withMessage("Đã tạo bản thay thế")
                .toEntity();
    }

    @PostMapping("/{hopDongId}/bien-ban/xuat-gop-theo-tram")
    @Operation(summary = "Ghép hồ sơ hoàn chỉnh (.zip) cho các trạm đã chọn — mỗi trạm 1 thư mục "
            + "riêng theo mã trạm, bên trong đủ các loại hồ sơ đã duyệt/đã tải lên")
    public ResponseEntity<byte[]> xuatGopTheoTram(
            @PathVariable UUID hopDongId,
            @RequestBody BienBanXuatGopTheoTramRequest request) {
        byte[] zip = bienBanExportService.xuatGopTheoTram(hopDongId, request.getHopDongDoiTuongIds());

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("HoSoDayDuTheoTram.zip", StandardCharsets.UTF_8)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }
}
