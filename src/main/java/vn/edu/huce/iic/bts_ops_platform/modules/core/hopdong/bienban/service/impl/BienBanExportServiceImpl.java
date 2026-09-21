package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.service.impl;

import vn.edu.huce.iic.bts_ops_platform.infrastructure.events.AppEventContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.dto.PageResponse;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.storage.LocalFileStorageService;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.dto.response.VolumeTramRow;
import vn.edu.huce.iic.bts_ops_platform.modules.business.volume.services.VolumeService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.entity.TepDinhKem;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.repository.TepDinhKemRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDoiTuongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThuocTinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.services.HopDongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanLichSuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanNguoiKyRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto.BienBanThayTheResult;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.entity.BienBan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.repository.BienBanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service.TramThiCongGiaiDoanSyncService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.service.BienBanExportService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.entity.HoSoTramTepDinhKem;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.repository.HoSoTramTepDinhKemRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.entity.BienBanPhatSinh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.entity.BienBanPhatSinhTepDinhKem;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.repository.BienBanPhatSinhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.repository.BienBanPhatSinhTepDinhKemRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.service.HopDongDoiTuongService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.MoTaTinh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity.TinhThanh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.MoTaTinhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.repository.TinhThanhRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Điền dữ liệu vào ĐÚNG mẫu Word gốc (Biên bản số 1 / Nhật ký thi công / Biên bản số 2) —
 * không thay đổi cấu trúc/nội dung mẫu, chỉ thay các placeholder "@Token" đã có sẵn trong mẫu
 * bằng dữ liệu thật. Các placeholder về số liệu kiểm định kết cấu (@SoDayCo, @SoKhoaCap,
 * @CauKienBeTong, @DoNghieng, @SieuAm, @SoLanDo, @Anten) không có nguồn dữ liệu tương ứng
 * trong DHSX_VTK (đối tượng "Trạm" không lưu các phép đo này) nên để trống — KHÔNG bịa số liệu.
 */
@Service
@RequiredArgsConstructor
public class BienBanExportServiceImpl implements BienBanExportService {

    private static final String TEMPLATE_BIEN_BAN_SO_1 = "templates/bienban/bien-ban-so-1.docx";
    private static final String TEMPLATE_NHAT_KY = "templates/bienban/nhat-ky-thi-cong.docx";
    private static final String TEMPLATE_BIEN_BAN_SO_2 = "templates/bienban/bien-ban-so-2.docx";
    private static final String TEMPLATE_PHU_LUC_DANH_SACH = "templates/bienban/phu-luc-danh-sach-tram.xlsx";
    private static final String TEMPLATE_BIA_KHAO_SAT = "templates/baocao/BaoCaoKhaoSat/1. Bia.dotx";
    private static final String TEMPLATE_THUYET_MINH_KHAO_SAT = "templates/baocao/BaoCaoKhaoSat/Thuyet minh BCKS.dotx";
    /** Dòng dữ liệu đầu tiên trong sheet "tgg" — khớp đúng dongbatdau=8 (1-indexed) của hệ thống gốc. */
    private static final int PHU_LUC_FIRST_DATA_ROW = 7;

    private static final String LOAI_BIEN_BAN_SO_1 = "BIEN_BAN_SO_1";
    private static final String LOAI_NHAT_KY = "NHAT_KY_THI_CONG";
    private static final String LOAI_BIEN_BAN_SO_2 = "BIEN_BAN_SO_2";
    private static final String LOAI_PHU_LUC = "PHU_LUC";
    private static final String LOAI_BAO_CAO_KHAO_SAT = "BAO_CAO_KHAO_SAT";
    private static final String LOAI_BAN_GIAO_MAT_BANG = "BAN_GIAO_MAT_BANG";
    private static final String LOAI_YEU_CAU_VAT_TU = "YEU_CAU_VAT_TU";
    private static final String LOAI_NHAN_VAT_TU = "NHAN_VAT_TU";
    private static final List<String> PRE_THI_CONG_ORDER = List.of(
            LOAI_BAN_GIAO_MAT_BANG, LOAI_YEU_CAU_VAT_TU, LOAI_NHAN_VAT_TU);
    private static final Set<String> PRE_THI_CONG_LOAI = Set.copyOf(PRE_THI_CONG_ORDER);
    private static final Set<String> ALL_LOAI_BIEN_BAN = Set.of(
            LOAI_BIEN_BAN_SO_1, LOAI_NHAT_KY, LOAI_BIEN_BAN_SO_2, LOAI_PHU_LUC, LOAI_BAO_CAO_KHAO_SAT);

    private final HopDongService hopDongService;
    private final HopDongDoiTuongService hopDongDoiTuongService;
    private final VolumeService volumeService;
    private final BienBanRepository bienBanRepository;
    private final TinhThanhRepository tinhThanhRepository;
    private final MoTaTinhRepository moTaTinhRepository;
    private final ObjectMapper objectMapper;
    private final AppEventContext appEventContext;
    private final HoSoTramTepDinhKemRepository hoSoTramTepDinhKemRepository;
    private final vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.repository.DanhMucBienBanRepository danhMucBienBanRepository;
    private final BienBanPhatSinhRepository bienBanPhatSinhRepository;
    private final BienBanPhatSinhTepDinhKemRepository bienBanPhatSinhTepDinhKemRepository;
    private final TepDinhKemRepository tepDinhKemRepository;
    private final LocalFileStorageService localFileStorageService;
    private final TramThiCongGiaiDoanSyncService tramThiCongGiaiDoanSyncService;

    @Override
    @Transactional
    public byte[] xuatHangLoat(
            UUID hopDongId, List<UUID> hopDongDoiTuongIds, BienBanNguoiKyRequest nguoiKy, List<String> loaiBienBan) {
        return xuatHangLoatInternal(hopDongId, hopDongDoiTuongIds, nguoiKy, loaiBienBan, null);
    }

    /** bienBanGocId != null khi được gọi từ {@link #xuatLai} — ghi lịch sử trỏ về bản gốc bị từ chối. */
    private byte[] xuatHangLoatInternal(
            UUID hopDongId, List<UUID> hopDongDoiTuongIds, BienBanNguoiKyRequest nguoiKy, List<String> loaiBienBan,
            UUID bienBanGocId) {
        if (hopDongDoiTuongIds == null || hopDongDoiTuongIds.isEmpty()) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Chưa chọn đối tượng để xuất biên bản");
        }
        Set<String> loaiSelected = loaiBienBan == null || loaiBienBan.isEmpty()
                ? ALL_LOAI_BIEN_BAN
                : Set.copyOf(loaiBienBan);
        if (loaiSelected.stream().noneMatch(ALL_LOAI_BIEN_BAN::contains)) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Chưa chọn loại biên bản để xuất");
        }
        HopDongResponse hopDong = hopDongService.getById(hopDongId);
        BienBanNguoiKyRequest ky = nguoiKy != null ? nguoiKy : new BienBanNguoiKyRequest();

        Set<UUID> wanted = Set.copyOf(hopDongDoiTuongIds);
        PageResponse<VolumeTramRow> page = volumeService.danhSachTram(hopDongId, "", null, "all", 0, 100_000);
        List<VolumeTramRow> selected = page.getItems().stream()
                .filter(row -> wanted.contains(row.getId()))
                .toList();
        if (selected.isEmpty()) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND,
                    "Không tìm thấy dữ liệu khối lượng cho các đối tượng đã chọn");
        }

        Map<UUID, HopDongDoiTuongResponse> fullById = hopDongDoiTuongService.getByIds(wanted).stream()
                .collect(Collectors.toMap(HopDongDoiTuongResponse::getId, Function.identity(), (a, b) -> a));

        String chuDauTu = resolveChuDauTu(hopDong);
        String tenTinh = selected.get(0).getDiaDiem();
        JwtUserPrincipal currentUser = SecurityContextHelper.requireCurrentUser();
        LocalDate today = LocalDate.now();

        String[] phamVi = resolvePhamViXuat(selected);
        String phamViXuat = phamVi[0];
        String phamViChiTiet = phamVi[1];
        String nguoiKyJson = toJson(ky);
        List<UUID> batchIds = selected.stream().map(VolumeTramRow::getId).toList();

        try (ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(zipBytes)) {

            if (loaiSelected.contains(LOAI_BIEN_BAN_SO_1) || loaiSelected.contains(LOAI_NHAT_KY)) {
                for (VolumeTramRow row : selected) {
                    HopDongDoiTuongResponse full = fullById.get(row.getId());
                    Map<String, String> values = buildStationValues(chuDauTu, row, full, ky);
                    List<UUID> rowIds = List.of(row.getId());
                    if (loaiSelected.contains(LOAI_BIEN_BAN_SO_1)) {
                        writeEntry(zip, "BienBanSo1_" + safeFileName(row.getMaTram()) + ".docx",
                                fillTemplate(TEMPLATE_BIEN_BAN_SO_1, values));
                        ghiLichSu(hopDongId, LOAI_BIEN_BAN_SO_1, "BB01", today, currentUser, 1,
                                phamViXuat, phamViChiTiet, rowIds, nguoiKyJson, bienBanGocId,
                                resolveTrangThaiTramTen(rowIds, fullById));
                    }
                    if (loaiSelected.contains(LOAI_NHAT_KY)) {
                        writeEntry(zip, "NhatKyThiCong_" + safeFileName(row.getMaTram()) + ".docx",
                                fillTemplate(TEMPLATE_NHAT_KY, values));
                        ghiLichSu(hopDongId, LOAI_NHAT_KY, "NK", today, currentUser, 1,
                                phamViXuat, phamViChiTiet, rowIds, nguoiKyJson, bienBanGocId,
                                resolveTrangThaiTramTen(rowIds, fullById));
                    }
                }
            }

            if (loaiSelected.contains(LOAI_BIEN_BAN_SO_2)) {
                Map<String, String> batchValues = buildBatchValues(chuDauTu, tenTinh, selected, ky);
                writeEntry(zip, "BienBanSo2_TongHop.docx", fillTemplate(TEMPLATE_BIEN_BAN_SO_2, batchValues));
                ghiLichSu(hopDongId, LOAI_BIEN_BAN_SO_2, "BB02", today, currentUser, selected.size(),
                        phamViXuat, phamViChiTiet, batchIds, nguoiKyJson, bienBanGocId,
                        resolveTrangThaiTramTen(batchIds, fullById));
            }

            // BB số 2 và phụ lục danh sách trạm là 1 bộ (kết luận họp 17-8) — chọn BB2 là tự kèm phụ lục.
            if (loaiSelected.contains(LOAI_PHU_LUC) || loaiSelected.contains(LOAI_BIEN_BAN_SO_2)) {
                writeEntry(zip, "PhuLucDanhSachTram.xlsx", buildPhuLucExcel(selected, fullById));
            }

            if (loaiSelected.contains(LOAI_BAO_CAO_KHAO_SAT)) {
                Map<String, String> khaoSatValues = buildKhaoSatValues(
                        hopDong, tenTinh, currentUser != null ? currentUser.hoTen() : null);
                writeEntry(zip, "1_Bia.docx", fillTemplate(TEMPLATE_BIA_KHAO_SAT, khaoSatValues));
                writeEntry(zip, "2_ThuyetMinhBCKS.docx", fillTemplate(TEMPLATE_THUYET_MINH_KHAO_SAT, khaoSatValues));
                ghiLichSu(hopDongId, LOAI_BAO_CAO_KHAO_SAT, "BCKS", today, currentUser, selected.size(),
                        phamViXuat, phamViChiTiet, batchIds, nguoiKyJson, bienBanGocId,
                        resolveTrangThaiTramTen(batchIds, fullById));
            }

            zip.finish();
            return zipBytes.toByteArray();
        } catch (IOException e) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Không xuất được biên bản: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public BienBanLichSuResponse xuatDonTruocThiCong(
            UUID hopDongId,
            List<UUID> hopDongDoiTuongIds,
            String loaiBienBan,
            LocalDate ngayLap,
            BienBanNguoiKyRequest nguoiKy) {
        if (hopDongDoiTuongIds == null || hopDongDoiTuongIds.isEmpty()) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Chưa chọn trạm để xuất biên bản");
        }
        if (loaiBienBan == null || (!PRE_THI_CONG_LOAI.contains(loaiBienBan) && !isDanhMucKhongMauHopLe(loaiBienBan))) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Loại biên bản không hợp lệ hoặc đã có mẫu Word (dùng luồng xuất hồ sơ thay vì tạo bản ghi)");
        }
        if (hopDongDoiTuongIds.size() != 1) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Biên bản dạng bản ghi chỉ tạo theo từng trạm");
        }
        UUID doiTuongId = hopDongDoiTuongIds.get(0);
        requirePreviousPreThiCongApproved(hopDongId, doiTuongId, loaiBienBan);

        BienBan existing = latestPreThiCongForStation(hopDongId, doiTuongId, loaiBienBan);
        if (existing != null && Boolean.TRUE.equals(existing.getHoatDong())) {
            if ("cho_duyet".equals(existing.getTrangThai())) {
                throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID,
                        "Trạm đã có biên bản loại này đang chờ duyệt");
            }
            if ("da_duyet".equals(existing.getTrangThai())) {
                throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID,
                        "Biên bản đã duyệt — hãy dùng Tạo bản thay thế");
            }
        }

        hopDongService.getById(hopDongId);
        PageResponse<VolumeTramRow> page = volumeService.danhSachTram(hopDongId, "", null, "all", 0, 100_000);
        VolumeTramRow row = page.getItems().stream()
                .filter(item -> doiTuongId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND,
                        "Không tìm thấy trạm trong hợp đồng"));

        JwtUserPrincipal currentUser = SecurityContextHelper.requireCurrentUser();
        LocalDate ngayThucTe = ngayLap != null ? ngayLap : LocalDate.now();
        String[] phamVi = resolvePhamViXuat(List.of(row));
        String loaiCode = switch (loaiBienBan) {
            case LOAI_BAN_GIAO_MAT_BANG -> "BGMB";
            case LOAI_YEU_CAU_VAT_TU -> "YCVT";
            case LOAI_NHAN_VAT_TU -> "NVT";
            default -> shortCodeFromLoai(loaiBienBan);
        };
        UUID bienBanGocId = existing != null ? existing.getId() : null;
        ghiLichSu(
                hopDongId,
                loaiBienBan,
                loaiCode,
                ngayThucTe,
                currentUser,
                1,
                phamVi[0],
                phamVi[1],
                List.of(doiTuongId),
                toJson(nguoiKy != null ? nguoiKy : new BienBanNguoiKyRequest()),
                bienBanGocId,
                null);

        BienBan saved = latestPreThiCongForStation(hopDongId, doiTuongId, loaiBienBan);
        if (saved == null) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID, "Không ghi được lịch sử biên bản");
        }
        try {
            syncGiaiDoanForBienBan(hopDongId, saved);
            BienBan refreshed = latestPreThiCongForStation(hopDongId, doiTuongId, loaiBienBan);
            if (refreshed != null) {
                saved = refreshed;
            }
        } catch (RuntimeException ignored) {
            // Đồng bộ giai đoạn trạm không chặn luồng xuất biên bản.
        }
        return toLichSuResponse(saved);
    }

    /** Mã danh mục động hợp lệ để tạo bản ghi (không sinh file): đang hoạt động và CHƯA có mẫu
     * Word — loại đã có mẫu phải đi luồng xuất hồ sơ để sinh file thật. */
    private boolean isDanhMucKhongMauHopLe(String loaiBienBan) {
        return danhMucBienBanRepository.findByMaAndNgayXoaIsNull(loaiBienBan)
                .filter(dm -> Boolean.TRUE.equals(dm.getHoatDong()))
                .filter(dm -> !Boolean.TRUE.equals(dm.getCoMauWord()))
                .isPresent();
    }

    /** Sinh mã ngắn từ mã danh mục động: lấy chữ cái đầu mỗi từ (BAN_GIAO_VAT_TU_A_CAP → BGVTAC). */
    private static String shortCodeFromLoai(String loai) {
        StringBuilder sb = new StringBuilder();
        for (String part : loai.split("_")) {
            if (!part.isEmpty()) {
                sb.append(part.charAt(0));
            }
        }
        return sb.isEmpty() ? "BB" : sb.toString();
    }

    private void requirePreviousPreThiCongApproved(UUID hopDongId, UUID doiTuongId, String loaiBienBan) {
        int index = PRE_THI_CONG_ORDER.indexOf(loaiBienBan);
        if (index <= 0) {
            return;
        }
        String previousLoai = PRE_THI_CONG_ORDER.get(index - 1);
        BienBan previous = latestPreThiCongForStation(hopDongId, doiTuongId, previousLoai);
        if (previous == null || !"da_duyet".equals(previous.getTrangThai())
                || !Boolean.TRUE.equals(previous.getHoatDong())) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID,
                    "Cần duyệt xong biên bản bước trước mới xuất được bước này");
        }
    }

    private BienBan latestPreThiCongForStation(UUID hopDongId, UUID doiTuongId, String loaiBienBan) {
        return bienBanRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDongId).stream()
                .filter(item -> loaiBienBan.equals(item.getLoaiBienBan()))
                .filter(item -> Boolean.TRUE.equals(item.getHoatDong()))
                .filter(item -> fromJsonIdsOrEmpty(item.getHopDongDoiTuongIdsJson()).contains(doiTuongId))
                .max(Comparator.comparing(BienBan::getNgayTao, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    private void ghiLichSu(
            UUID hopDongId, String loaiBienBan, String loaiCode, LocalDate ngayLap,
            JwtUserPrincipal currentUser, int soLuongDoiTuong,
            String phamViXuat, String phamViChiTiet, List<UUID> doiTuongIds, String nguoiKyJson, UUID bienBanGocId,
            String trangThaiTramTen) {
        long soThuTu = bienBanRepository.countByHopDongIdAndLoaiBienBanAndNgayXoaIsNull(hopDongId, loaiBienBan) + 1;
        BienBan record = new BienBan();
        record.setHopDongId(hopDongId);
        record.setLoaiBienBan(loaiBienBan);
        record.setMaBienBan(loaiCode + "-" + String.format("%03d", soThuTu));
        record.setNgayLap(ngayLap);
        record.setNguoiLapId(currentUser != null ? currentUser.id() : null);
        record.setNguoiLapTen(currentUser != null ? currentUser.hoTen() : null);
        record.setTrangThai("cho_duyet");
        record.setSoLuongDoiTuong(soLuongDoiTuong);
        record.setPhamViXuat(phamViXuat);
        record.setPhamViChiTiet(phamViChiTiet);
        record.setHopDongDoiTuongIdsJson(toJson(doiTuongIds));
        record.setNguoiKyJson(nguoiKyJson);
        record.setBienBanGocId(bienBanGocId);
        record.setTrangThaiTramTen(trangThaiTramTen);
        bienBanRepository.save(record);
    }

    private static String resolveTrangThaiTramTen(List<UUID> doiTuongIds, Map<UUID, HopDongDoiTuongResponse> fullById) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (UUID id : doiTuongIds) {
            HopDongDoiTuongResponse dto = fullById.get(id);
            if (dto != null && dto.getTrangThaiTen() != null && !dto.getTrangThaiTen().isBlank()) {
                names.add(dto.getTrangThaiTen().trim());
            }
        }
        if (names.isEmpty()) {
            return null;
        }
        return String.join(", ", names);
    }

    private void syncGiaiDoanForBienBan(UUID hopDongId, BienBan entity) {
        List<UUID> doiTuongIds = fromJsonIdsOrEmpty(entity.getHopDongDoiTuongIdsJson());
        tramThiCongGiaiDoanSyncService.syncForDoiTuongIds(hopDongId, doiTuongIds);
        if (doiTuongIds.size() == 1) {
            String giaiDoan = tramThiCongGiaiDoanSyncService.syncForDoiTuong(hopDongId, doiTuongIds.get(0));
            if (giaiDoan != null) {
                entity.setTrangThaiTramTen(giaiDoan);
                bienBanRepository.save(entity);
            }
        }
    }

    /** Suy ra phạm vi xuất từ tập đối tượng đã chọn: cùng 1 tỉnh -> theo_tinh, cùng 1 nhà thầu
     * (nhưng khác tỉnh) -> theo_nha_thau, còn lại -> da_chon. Trả về [phamViXuat, phamViChiTiet]. */
    private static String[] resolvePhamViXuat(List<VolumeTramRow> selected) {
        Set<String> tinhSet = selected.stream()
                .map(VolumeTramRow::getDiaDiem).filter(v -> v != null && !v.isBlank())
                .collect(Collectors.toSet());
        if (tinhSet.size() == 1) {
            return new String[]{"theo_tinh", tinhSet.iterator().next()};
        }
        Set<String> nhaThauSet = selected.stream()
                .map(VolumeTramRow::getNhaThau).filter(v -> v != null && !v.isBlank())
                .collect(Collectors.toSet());
        if (nhaThauSet.size() == 1) {
            return new String[]{"theo_nha_thau", nhaThauSet.iterator().next()};
        }
        return new String[]{"da_chon", selected.size() + " đối tượng"};
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<UUID> fromJsonIds(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<UUID>>() {
            });
        } catch (IOException e) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID,
                    "Không đọc được dữ liệu đối tượng đã lưu của biên bản gốc");
        }
    }

    /** Dùng khi hiển thị lịch sử — không throw nếu thiếu/hỏng dữ liệu (biên bản tạo trước khi có
     * field này), chỉ trả về danh sách rỗng. */
    private List<UUID> fromJsonIdsOrEmpty(String json) {
        if (json == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<UUID>>() {
            });
        } catch (IOException e) {
            return List.of();
        }
    }

    private BienBanNguoiKyRequest fromJsonNguoiKy(String json) {
        if (json == null) {
            return new BienBanNguoiKyRequest();
        }
        try {
            return objectMapper.readValue(json, BienBanNguoiKyRequest.class);
        } catch (IOException e) {
            return new BienBanNguoiKyRequest();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienBanLichSuResponse> getLichSu(UUID hopDongId) {
        return bienBanRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDongId).stream()
                .sorted(Comparator.comparing(BienBan::getNgayTao, Comparator.reverseOrder()))
                .map(this::toLichSuResponse)
                .toList();
    }

    @Override
    @Transactional
    public BienBanLichSuResponse pheDuyet(UUID hopDongId, UUID bienBanId) {
        BienBan entity = requireBienBan(hopDongId, bienBanId);
        if (!"cho_duyet".equals(entity.getTrangThai())) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID, "Biên bản không ở trạng thái chờ duyệt");
        }
        JwtUserPrincipal currentUser = SecurityContextHelper.requireCurrentUser();
        entity.setTrangThai("da_duyet");
        entity.setLyDoTuChoi(null);
        entity.setNguoiPheDuyetId(currentUser != null ? currentUser.id() : null);
        entity.setNguoiPheDuyetTen(currentUser != null ? currentUser.hoTen() : null);
        entity.setNgayPheDuyet(Instant.now());
        BienBan saved = bienBanRepository.save(entity);
        syncGiaiDoanForBienBan(hopDongId, saved);
        return toLichSuResponse(saved);
    }

    @Override
    @Transactional
    public BienBanLichSuResponse tuChoi(UUID hopDongId, UUID bienBanId, String lyDo) {
        if (lyDo == null || lyDo.isBlank()) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID, "Cần nhập lý do từ chối");
        }
        BienBan entity = requireBienBan(hopDongId, bienBanId);
        if (!"cho_duyet".equals(entity.getTrangThai())) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID, "Biên bản không ở trạng thái chờ duyệt");
        }
        JwtUserPrincipal currentUser = SecurityContextHelper.requireCurrentUser();
        entity.setTrangThai("tu_choi");
        entity.setLyDoTuChoi(lyDo.trim());
        entity.setNguoiPheDuyetId(currentUser != null ? currentUser.id() : null);
        entity.setNguoiPheDuyetTen(currentUser != null ? currentUser.hoTen() : null);
        entity.setNgayPheDuyet(Instant.now());
        BienBan saved = bienBanRepository.save(entity);
        syncGiaiDoanForBienBan(hopDongId, saved);
        return toLichSuResponse(saved);
    }

    @Override
    @Transactional
    public List<BienBanLichSuResponse> pheDuyetHangLoat(UUID hopDongId, List<UUID> bienBanIds) {
        if (bienBanIds == null || bienBanIds.isEmpty()) {
            return List.of();
        }
        JwtUserPrincipal currentUser = SecurityContextHelper.requireCurrentUser();
        Instant now = Instant.now();
        List<BienBan> entities = bienBanRepository.findByIdInAndNgayXoaIsNull(bienBanIds).stream()
                .filter(b -> hopDongId.equals(b.getHopDongId()))
                .filter(b -> "cho_duyet".equals(b.getTrangThai()))
                .toList();
        for (BienBan entity : entities) {
            entity.setTrangThai("da_duyet");
            entity.setLyDoTuChoi(null);
            entity.setNguoiPheDuyetId(currentUser != null ? currentUser.id() : null);
            entity.setNguoiPheDuyetTen(currentUser != null ? currentUser.hoTen() : null);
            entity.setNgayPheDuyet(now);
        }
        List<BienBanLichSuResponse> result = bienBanRepository.saveAll(entities).stream().map(this::toLichSuResponse).toList();
        auditBienBan("PHE_DUYET_BIEN_BAN", "Phê duyệt " + entities.size() + " biên bản", null, hopDongId, entities);
        return result;
    }

    @Override
    @Transactional
    public List<BienBanLichSuResponse> tuChoiHangLoat(UUID hopDongId, List<UUID> bienBanIds, String lyDo) {
        if (lyDo == null || lyDo.isBlank()) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID, "Cần nhập lý do từ chối");
        }
        if (bienBanIds == null || bienBanIds.isEmpty()) {
            return List.of();
        }
        JwtUserPrincipal currentUser = SecurityContextHelper.requireCurrentUser();
        Instant now = Instant.now();
        String reason = lyDo.trim();
        List<BienBan> entities = bienBanRepository.findByIdInAndNgayXoaIsNull(bienBanIds).stream()
                .filter(b -> hopDongId.equals(b.getHopDongId()))
                .filter(b -> "cho_duyet".equals(b.getTrangThai()))
                .toList();
        for (BienBan entity : entities) {
            entity.setTrangThai("tu_choi");
            entity.setLyDoTuChoi(reason);
            entity.setNguoiPheDuyetId(currentUser != null ? currentUser.id() : null);
            entity.setNguoiPheDuyetTen(currentUser != null ? currentUser.hoTen() : null);
            entity.setNgayPheDuyet(now);
        }
        List<BienBanLichSuResponse> result = bienBanRepository.saveAll(entities).stream().map(this::toLichSuResponse).toList();
        auditBienBan("TU_CHOI_BIEN_BAN", "Từ chối " + entities.size() + " biên bản", "Lý do: " + reason, hopDongId, entities);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] taiVeBienBan(UUID hopDongId, UUID bienBanId) {
        BienBan entity = requireBienBan(hopDongId, bienBanId);
        if (!PRE_THI_CONG_LOAI.contains(entity.getLoaiBienBan())) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID, "Loại biên bản này chưa hỗ trợ tải trực tiếp");
        }
        List<UUID> ids = fromJsonIdsOrEmpty(entity.getHopDongDoiTuongIdsJson());
        if (ids.isEmpty()) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID, "Biên bản không có thông tin trạm để tạo file");
        }
        UUID doiTuongId = ids.get(0);
        HopDongResponse hopDong = hopDongService.getById(hopDongId);
        PageResponse<VolumeTramRow> page = volumeService.danhSachTram(hopDongId, "", null, "all", 0, 100_000);
        VolumeTramRow row = page.getItems().stream()
                .filter(item -> doiTuongId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND,
                        "Không tìm thấy trạm trong hợp đồng"));
        HopDongDoiTuongResponse full = hopDongDoiTuongService.getByIds(List.of(doiTuongId)).stream()
                .findFirst()
                .orElse(null);
        BienBanNguoiKyRequest ky = fromJsonNguoiKy(entity.getNguoiKyJson());
        String chuDauTu = resolveChuDauTu(hopDong);
        Map<String, String> values = buildStationValues(chuDauTu, row, full, ky);
        values.put("@MaBienBan", nonBlank(entity.getMaBienBan(), "—"));
        values.put("@LoaiBienBan", tenLoaiBienBan(entity.getLoaiBienBan()));
        try {
            return fillTemplate(TEMPLATE_BIEN_BAN_SO_1, values);
        } catch (IOException e) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID, "Không tạo được file biên bản: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public byte[] xuatLai(UUID hopDongId, UUID bienBanGocId) {
        BienBan goc = requireBienBan(hopDongId, bienBanGocId);
        if (!"tu_choi".equals(goc.getTrangThai())) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID, "Chỉ có thể xuất lại biên bản đã bị từ chối");
        }
        if (goc.getHopDongDoiTuongIdsJson() == null) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID,
                    "Biên bản này được tạo trước khi hệ thống hỗ trợ xuất lại, không có đủ dữ liệu để tạo lại");
        }
        List<UUID> ids = fromJsonIds(goc.getHopDongDoiTuongIdsJson());
        BienBanNguoiKyRequest ky = fromJsonNguoiKy(goc.getNguoiKyJson());
        return xuatHangLoatInternal(hopDongId, ids, ky, List.of(goc.getLoaiBienBan()), goc.getId());
    }

    @Override
    @Transactional
    public BienBanThayTheResult taoBanThayThe(UUID hopDongId, UUID bienBanId, String lyDo) {
        BienBan goc = requireBienBan(hopDongId, bienBanId);
        if (!"da_duyet".equals(goc.getTrangThai())) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID,
                    "Chỉ có thể tạo bản thay thế cho biên bản đã duyệt");
        }
        if (!Boolean.TRUE.equals(goc.getHoatDong())) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID, "Biên bản đã bị hủy hiệu lực");
        }
        if (lyDo == null || lyDo.isBlank()) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID, "Bắt buộc nhập lý do thay thế");
        }

        revokeBienBan(goc, lyDo.trim());

        if (PRE_THI_CONG_LOAI.contains(goc.getLoaiBienBan()) || isDanhMucKhongMauHopLe(goc.getLoaiBienBan())) {
            return BienBanThayTheResult.builder()
                    .record(taoBanThayTheDon(goc))
                    .build();
        }

        if (goc.getHopDongDoiTuongIdsJson() == null) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID,
                    "Biên bản này không có đủ dữ liệu để tạo bản thay thế");
        }
        List<UUID> ids = fromJsonIds(goc.getHopDongDoiTuongIdsJson());
        BienBanNguoiKyRequest ky = fromJsonNguoiKy(goc.getNguoiKyJson());
        byte[] zip = xuatHangLoatInternal(hopDongId, ids, ky, List.of(goc.getLoaiBienBan()), goc.getId());
        BienBan newest = bienBanRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDongId)
                .stream()
                .filter(b -> goc.getId().equals(b.getBienBanGocId()))
                .findFirst()
                .orElseThrow(() -> new AppException(HopDongErrorCode.BIEN_BAN_INVALID,
                        "Không ghi được bản thay thế"));
        return BienBanThayTheResult.builder()
                .record(toLichSuResponse(newest))
                .zipContent(zip)
                .build();
    }

    private BienBanLichSuResponse taoBanThayTheDon(BienBan goc) {
        List<UUID> doiTuongIds = fromJsonIdsOrEmpty(goc.getHopDongDoiTuongIdsJson());
        if (doiTuongIds.size() != 1) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID,
                    "Biên bản dạng bản ghi chỉ thay thế theo từng trạm");
        }
        UUID doiTuongId = doiTuongIds.get(0);
        JwtUserPrincipal currentUser = SecurityContextHelper.requireCurrentUser();
        LocalDate ngayLap = LocalDate.now();
        String loaiCode = loaiCodeFromLoai(goc.getLoaiBienBan());
        ghiLichSu(
                goc.getHopDongId(),
                goc.getLoaiBienBan(),
                loaiCode,
                ngayLap,
                currentUser,
                1,
                goc.getPhamViXuat(),
                goc.getPhamViChiTiet(),
                List.of(doiTuongId),
                goc.getNguoiKyJson(),
                goc.getId(),
                goc.getTrangThaiTramTen());

        BienBan saved = latestPreThiCongForStation(goc.getHopDongId(), doiTuongId, goc.getLoaiBienBan());
        if (saved == null) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_INVALID, "Không ghi được bản thay thế");
        }
        try {
            syncGiaiDoanForBienBan(goc.getHopDongId(), saved);
            BienBan refreshed = latestPreThiCongForStation(goc.getHopDongId(), doiTuongId, goc.getLoaiBienBan());
            if (refreshed != null) {
                saved = refreshed;
            }
        } catch (RuntimeException ignored) {
            // Đồng bộ giai đoạn trạm không chặn luồng thay thế biên bản.
        }
        return toLichSuResponse(saved);
    }

    private void revokeBienBan(BienBan entity, String lyDo) {
        JwtUserPrincipal currentUser = SecurityContextHelper.requireCurrentUser();
        entity.setHoatDong(false);
        entity.setLyDoHuyHieuLuc(lyDo);
        entity.setNgayHuyHieuLuc(Instant.now());
        entity.setNguoiHuyId(currentUser.id());
        entity.setNguoiHuyTen(currentUser.hoTen());
        bienBanRepository.save(entity);
    }

    private static String loaiCodeFromLoai(String loaiBienBan) {
        return switch (loaiBienBan) {
            case LOAI_BAN_GIAO_MAT_BANG -> "BGMB";
            case LOAI_YEU_CAU_VAT_TU -> "YCVT";
            case LOAI_NHAN_VAT_TU -> "NVT";
            default -> shortCodeFromLoai(loaiBienBan);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] xuatGopTheoTram(UUID hopDongId, List<UUID> hopDongDoiTuongIds) {
        if (hopDongDoiTuongIds == null || hopDongDoiTuongIds.isEmpty()) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Chưa chọn trạm để xuất hồ sơ");
        }
        Set<UUID> wanted = Set.copyOf(hopDongDoiTuongIds);
        PageResponse<VolumeTramRow> page = volumeService.danhSachTram(hopDongId, "", null, "all", 0, 100_000);
        List<VolumeTramRow> selected = page.getItems().stream()
                .filter(row -> wanted.contains(row.getId()))
                .toList();
        if (selected.isEmpty()) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND,
                    "Không tìm thấy dữ liệu cho các trạm đã chọn");
        }

        // trạm id -> loại biên bản -> bản ghi ĐÃ DUYỆT mới nhất áp dụng cho trạm đó (BB2/khảo sát
        // là bản ghi theo lô, dùng chung cho nhiều trạm — không nhân bản dữ liệu, chỉ tái tạo 1 lần).
        Map<UUID, Map<String, BienBan>> latestByTramAndLoai = new LinkedHashMap<>();
        for (BienBan record : bienBanRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDongId)) {
            if (!"da_duyet".equals(record.getTrangThai()) || !Boolean.TRUE.equals(record.getHoatDong())) {
                continue;
            }
            for (UUID tramId : fromJsonIdsOrEmpty(record.getHopDongDoiTuongIdsJson())) {
                if (!wanted.contains(tramId)) {
                    continue;
                }
                Map<String, BienBan> byLoai = latestByTramAndLoai.computeIfAbsent(tramId, k -> new LinkedHashMap<>());
                BienBan existing = byLoai.get(record.getLoaiBienBan());
                if (existing == null || isNewerBienBan(record, existing)) {
                    byLoai.put(record.getLoaiBienBan(), record);
                }
            }
        }

        List<BienBanPhatSinh> approvedPhatSinh = bienBanPhatSinhRepository
                .findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDongId).stream()
                .filter(p -> "da_duyet".equals(p.getTrangThai()))
                .filter(p -> p.getHopDongDoiTuongId() != null && wanted.contains(p.getHopDongDoiTuongId()))
                .toList();
        Map<UUID, List<BienBanPhatSinh>> phatSinhByTram = approvedPhatSinh.stream()
                .collect(Collectors.groupingBy(BienBanPhatSinh::getHopDongDoiTuongId));
        Map<UUID, List<BienBanPhatSinhTepDinhKem>> phatSinhFilesByPhatSinh = approvedPhatSinh.isEmpty()
                ? Map.of()
                : bienBanPhatSinhTepDinhKemRepository
                        .findByBienBanPhatSinhIdInAndNgayXoaIsNullAndHoatDongTrue(
                                approvedPhatSinh.stream().map(BienBanPhatSinh::getId).toList())
                        .stream()
                        .collect(Collectors.groupingBy(BienBanPhatSinhTepDinhKem::getBienBanPhatSinhId));

        Map<UUID, List<HoSoTramTepDinhKem>> hoSoTramByTram = hoSoTramTepDinhKemRepository
                .findByHopDongDoiTuongIdInAndNgayXoaIsNullAndHoatDongTrue(wanted).stream()
                .collect(Collectors.groupingBy(HoSoTramTepDinhKem::getHopDongDoiTuongId));

        Set<UUID> tepDinhKemIds = new LinkedHashSet<>();
        hoSoTramByTram.values().forEach(list -> list.forEach(u -> tepDinhKemIds.add(u.getTepDinhKemId())));
        phatSinhFilesByPhatSinh.values().forEach(list -> list.forEach(l -> tepDinhKemIds.add(l.getTepDinhKemId())));
        Map<UUID, TepDinhKem> tepById = tepDinhKemIds.isEmpty()
                ? Map.of()
                : tepDinhKemRepository.findByIdInAndNgayXoaIsNull(List.copyOf(tepDinhKemIds)).stream()
                        .collect(Collectors.toMap(TepDinhKem::getId, Function.identity()));

        Map<UUID, Map<String, byte[]>> regenCache = new LinkedHashMap<>();

        try (ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(zipBytes)) {

            for (VolumeTramRow row : selected) {
                String tramFolder = safeFileName(row.getMaTram()) + "/";
                Map<String, BienBan> byLoai = latestByTramAndLoai.getOrDefault(row.getId(), Map.of());
                List<HoSoTramTepDinhKem> uploadsForTram = hoSoTramByTram.getOrDefault(row.getId(), List.of());

                writeManualFolder(zip, tramFolder, "01_Ban_giao_mat_bang", uploadsForTram, "BAN_GIAO_MAT_BANG", tepById);
                writeManualFolder(zip, tramFolder, "02_Giao_nhiem_vu_GSTC", uploadsForTram, "GIAO_NHIEM_VU_GSTC", tepById);
                writeRegeneratedFolder(zip, tramFolder, "03_Bao_cao_khao_sat", LOAI_BAO_CAO_KHAO_SAT, byLoai, regenCache);
                writeManualFolder(zip, tramFolder, "04_Nghiem_thu_vat_lieu", uploadsForTram, "NGHIEM_THU_VAT_LIEU", tepById);
                writeManualFolder(zip, tramFolder, "05_Ho_so_thiet_ke", uploadsForTram, "HO_SO_THIET_KE", tepById);
                writeRegeneratedFolder(zip, tramFolder, "06_Bien_ban_so_1", LOAI_BIEN_BAN_SO_1, byLoai, regenCache);
                writeRegeneratedFolder(zip, tramFolder, "07_Nhat_ky_thi_cong", LOAI_NHAT_KY, byLoai, regenCache);
                writeRegeneratedFolder(zip, tramFolder, "08_Bien_ban_so_2", LOAI_BIEN_BAN_SO_2, byLoai, regenCache);
                writePhatSinhFolder(zip, tramFolder, phatSinhByTram.getOrDefault(row.getId(), List.of()),
                        phatSinhFilesByPhatSinh, tepById);
                writeManualFolder(zip, tramFolder, "10_Thi_cong", uploadsForTram, "THI_CONG", tepById);
            }

            zip.finish();
            return zipBytes.toByteArray();
        } catch (IOException e) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Không xuất được hồ sơ: " + e.getMessage());
        }
    }

    private static boolean isNewerBienBan(BienBan candidate, BienBan current) {
        int cmp = candidate.getNgayLap().compareTo(current.getNgayLap());
        if (cmp != 0) {
            return cmp > 0;
        }
        return candidate.getNgayTao().isAfter(current.getNgayTao());
    }

    /** Tái tạo lại đúng nội dung + người ký đã lưu tại lần duyệt — dùng cho "xuất hồ sơ đầy đủ",
     * KHÔNG ghi thêm lịch sử mới (khác {@link #xuatLai}, vốn tạo bản ghi lịch sử mới trỏ về gốc). */
    private Map<String, byte[]> regenerateContent(BienBan record) throws IOException {
        List<UUID> ids = fromJsonIdsOrEmpty(record.getHopDongDoiTuongIdsJson());
        if (ids.isEmpty()) {
            return Map.of();
        }
        Set<UUID> idSet = Set.copyOf(ids);
        PageResponse<VolumeTramRow> page =
                volumeService.danhSachTram(record.getHopDongId(), "", null, "all", 0, 100_000);
        List<VolumeTramRow> rows = page.getItems().stream().filter(r -> idSet.contains(r.getId())).toList();
        if (rows.isEmpty()) {
            return Map.of();
        }
        HopDongResponse hopDong = hopDongService.getById(record.getHopDongId());
        String chuDauTu = resolveChuDauTu(hopDong);
        BienBanNguoiKyRequest ky = fromJsonNguoiKy(record.getNguoiKyJson());
        String tenTinh = rows.get(0).getDiaDiem();

        return switch (record.getLoaiBienBan()) {
            case LOAI_BIEN_BAN_SO_1 -> Map.of("BienBanSo1.docx",
                    fillTemplate(TEMPLATE_BIEN_BAN_SO_1, buildStationValues(chuDauTu, rows.get(0), resolveFull(idSet, rows.get(0)), ky)));
            case LOAI_NHAT_KY -> Map.of("NhatKyThiCong.docx",
                    fillTemplate(TEMPLATE_NHAT_KY, buildStationValues(chuDauTu, rows.get(0), resolveFull(idSet, rows.get(0)), ky)));
            case LOAI_BIEN_BAN_SO_2 -> Map.of("BienBanSo2_TongHop.docx",
                    fillTemplate(TEMPLATE_BIEN_BAN_SO_2, buildBatchValues(chuDauTu, tenTinh, rows, ky)));
            case LOAI_BAO_CAO_KHAO_SAT -> {
                Map<String, String> values = buildKhaoSatValues(hopDong, tenTinh, record.getNguoiLapTen());
                Map<String, byte[]> out = new LinkedHashMap<>();
                out.put("1_Bia.docx", fillTemplate(TEMPLATE_BIA_KHAO_SAT, values));
                out.put("2_ThuyetMinhBCKS.docx", fillTemplate(TEMPLATE_THUYET_MINH_KHAO_SAT, values));
                yield out;
            }
            default -> Map.of();
        };
    }

    private HopDongDoiTuongResponse resolveFull(Set<UUID> idSet, VolumeTramRow row) {
        return hopDongDoiTuongService.getByIds(idSet).stream()
                .filter(r -> r.getId().equals(row.getId()))
                .findFirst()
                .orElse(null);
    }

    private void writeRegeneratedFolder(
            ZipOutputStream zip, String tramFolder, String subFolder, String loaiKey,
            Map<String, BienBan> byLoai, Map<UUID, Map<String, byte[]>> regenCache) throws IOException {
        BienBan record = byLoai.get(loaiKey);
        if (record == null) {
            return;
        }
        Map<String, byte[]> files = regenCache.get(record.getId());
        if (files == null) {
            files = regenerateContent(record);
            regenCache.put(record.getId(), files);
        }
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            writeEntry(zip, tramFolder + subFolder + "/" + entry.getKey(), entry.getValue());
        }
    }

    private void writeManualFolder(
            ZipOutputStream zip, String tramFolder, String subFolder,
            List<HoSoTramTepDinhKem> uploads, String danhMuc, Map<UUID, TepDinhKem> tepById) throws IOException {
        for (HoSoTramTepDinhKem upload : uploads) {
            if (!danhMuc.equals(upload.getDanhMuc())) {
                continue;
            }
            TepDinhKem tep = tepById.get(upload.getTepDinhKemId());
            byte[] bytes = readTepDinhKemBytes(tep);
            if (bytes == null) {
                continue;
            }
            String name = tep.getTenTepGoc() != null ? tep.getTenTepGoc() : tep.getTenTep();
            writeEntry(zip, tramFolder + subFolder + "/" + safeFileName(name), bytes);
        }
    }

    private void writePhatSinhFolder(
            ZipOutputStream zip, String tramFolder, List<BienBanPhatSinh> phatSinhForTram,
            Map<UUID, List<BienBanPhatSinhTepDinhKem>> filesByPhatSinh, Map<UUID, TepDinhKem> tepById)
            throws IOException {
        for (BienBanPhatSinh phatSinh : phatSinhForTram) {
            for (BienBanPhatSinhTepDinhKem link : filesByPhatSinh.getOrDefault(phatSinh.getId(), List.of())) {
                TepDinhKem tep = tepById.get(link.getTepDinhKemId());
                byte[] bytes = readTepDinhKemBytes(tep);
                if (bytes == null) {
                    continue;
                }
                String name = tep.getTenTepGoc() != null ? tep.getTenTepGoc() : tep.getTenTep();
                writeEntry(zip, tramFolder + "09_Bien_ban_phat_sinh/" + safeFileName(phatSinh.getMaPhatSinh()) + "_" + safeFileName(name), bytes);
            }
        }
    }

    /** Đọc lại byte file vật lý đã lưu — trả về null nếu thiếu bản ghi/file (bị xoá thủ công ngoài
     * hệ thống chẳng hạn) thay vì làm hỏng cả gói xuất vì 1 file lỗi. */
    private byte[] readTepDinhKemBytes(TepDinhKem tep) {
        if (tep == null) {
            return null;
        }
        try {
            Path path = localFileStorageService.resolveRelativePath(tep.getDuongDan());
            return Files.readAllBytes(path);
        } catch (IOException e) {
            return null;
        }
    }

    private BienBan requireBienBan(UUID hopDongId, UUID bienBanId) {
        BienBan entity = bienBanRepository.findById(bienBanId)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new AppException(HopDongErrorCode.BIEN_BAN_NOT_FOUND, "Không tìm thấy biên bản"));
        if (!hopDongId.equals(entity.getHopDongId())) {
            throw new AppException(HopDongErrorCode.BIEN_BAN_NOT_FOUND, "Không tìm thấy biên bản");
        }
        return entity;
    }

    private BienBanLichSuResponse toLichSuResponse(BienBan entity) {
        return BienBanLichSuResponse.builder()
                .id(entity.getId())
                .maBienBan(entity.getMaBienBan())
                .loaiBienBan(entity.getLoaiBienBan())
                .loaiBienBanTen(tenLoaiBienBan(entity.getLoaiBienBan()))
                .ngayLap(entity.getNgayLap())
                .nguoiLapId(entity.getNguoiLapId())
                .nguoiLapTen(entity.getNguoiLapTen())
                .trangThai(entity.getTrangThai())
                .soLuongDoiTuong(entity.getSoLuongDoiTuong())
                .phamViXuat(entity.getPhamViXuat())
                .phamViChiTiet(entity.getPhamViChiTiet())
                .lyDoTuChoi(entity.getLyDoTuChoi())
                .nguoiPheDuyetId(entity.getNguoiPheDuyetId())
                .nguoiPheDuyetTen(entity.getNguoiPheDuyetTen())
                .ngayPheDuyet(entity.getNgayPheDuyet())
                .bienBanGocId(entity.getBienBanGocId())
                .coTheXuatLai(coTheXuatLai(entity))
                .coTheTaoBanThayThe(coTheTaoBanThayThe(entity))
                .hoatDong(entity.getHoatDong())
                .lyDoHuyHieuLuc(entity.getLyDoHuyHieuLuc())
                .ngayHuyHieuLuc(entity.getNgayHuyHieuLuc())
                .nguoiHuyTen(entity.getNguoiHuyTen())
                .hopDongDoiTuongIds(fromJsonIdsOrEmpty(entity.getHopDongDoiTuongIdsJson()))
                .trangThaiTramTen(entity.getTrangThaiTramTen())
                .build();
    }

    private static String tenLoaiBienBan(String loaiBienBan) {
        return switch (loaiBienBan) {
            case "BIEN_BAN_SO_1" -> "Biên bản nghiệm thu khối lượng";
            case "NHAT_KY_THI_CONG" -> "Nhật ký thi công";
            case "BIEN_BAN_SO_2" -> "Biên bản nghiệm thu tổng hợp";
            case "BAO_CAO_KHAO_SAT" -> "Báo cáo khảo sát (Bìa + Thuyết minh)";
            case "BAN_GIAO_MAT_BANG" -> "Biên bản bàn giao mặt bằng";
            case "YEU_CAU_VAT_TU" -> "Biên bản yêu cầu cung cấp vật tư";
            case "NHAN_VAT_TU" -> "Biên bản nhận vật tư";
            default -> loaiBienBan;
        };
    }

    private static boolean coTheXuatLai(BienBan entity) {
        return "tu_choi".equals(entity.getTrangThai()) && Boolean.TRUE.equals(entity.getHoatDong());
    }

    private static boolean coTheTaoBanThayThe(BienBan entity) {
        return "da_duyet".equals(entity.getTrangThai()) && Boolean.TRUE.equals(entity.getHoatDong());
    }

    /** Bìa + Thuyết minh khảo sát dùng chung 1 bộ token — lập 1 lần cho cả lô/tỉnh đã chọn,
     * khớp đúng hành vi ExportBaoCaoKhaoSat() của hệ thống gốc (không lặp theo từng trạm). */
    private Map<String, String> buildKhaoSatValues(HopDongResponse hopDong, String tenTinh, String nguoiLapTen) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("@SoHopDong", nonBlank(hopDong.getMaHopDong(), "—"));
        values.put("@TenTinh", tenTinh != null && !tenTinh.isBlank() ? "Tỉnh " + tenTinh : "—");
        values.put("@NguoiLapBaoCao", nonBlank(nguoiLapTen, "—"));
        values.put("@DonViQuanLy", findHopDongAttr(hopDong, "—", "đơn vị quản lý", "don vi quan ly"));
        values.put("@NoiDungHopDong", findHopDongAttr(hopDong, "—", "nội dung hợp đồng", "noi dung hop dong"));

        MoTaTinh moTa = resolveMoTaTinh(tenTinh);
        values.put("@ViTriDiaLy", moTa != null ? nonBlank(moTa.getViTriDiaLy(), "—") : "—");
        values.put("@DacDiemDiaHinh", moTa != null ? nonBlank(moTa.getDacDiemDiaHinh(), "—") : "—");
        values.put("@KhiHau", moTa != null ? nonBlank(moTa.getKhiHau(), "—") : "—");
        values.put("@DieuKienKinhTe", moTa != null ? nonBlank(moTa.getDieuKienKinhTe(), "—") : "—");
        return values;
    }

    /** Tra "tỉnh" theo đúng tên hiển thị (@TenTinh không kèm tiền tố "Tỉnh ") — không tìm thấy thì
     * để trống thay vì đoán, vì đây là dữ liệu do người dùng tự soạn theo từng tỉnh cụ thể. */
    private MoTaTinh resolveMoTaTinh(String tenTinh) {
        if (tenTinh == null || tenTinh.isBlank()) {
            return null;
        }
        String normalized = tenTinh.trim();
        return tinhThanhRepository.findByNgayXoaIsNullOrderByMaAsc().stream()
                .filter(t -> normalized.equalsIgnoreCase(t.getTen()))
                .map(TinhThanh::getTinhThanhId)
                .findFirst()
                .flatMap(moTaTinhRepository::findByTinhThanhIdAndNgayXoaIsNull)
                .orElse(null);
    }

    /** Dùng cho Biên bản số 1 + Nhật ký thi công — cả 2 mẫu dùng chung 1 bộ token per-trạm. */
    private Map<String, String> buildStationValues(
            String chuDauTu, VolumeTramRow row, HopDongDoiTuongResponse full, BienBanNguoiKyRequest ky) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("@ChuDauTu", chuDauTu);
        values.put("@NhaThau", row.getNhaThau());
        values.put("@MaTram", row.getMaTram());
        values.put("@DiaDiem", nonBlank(row.getDiaChi(), row.getDiaDiem()));
        values.put("@TenTinh", row.getDiaDiem());

        // Số liệu kết cấu — có thật trong thuộc tính EAV của đối tượng "Trạm" (nếu kiểu HĐ này
        // có cấu hình các thuộc tính tương ứng); rỗng nếu đối tượng không có thuộc tính đó.
        values.put("@LoaiCot", findAttr(full, "—", "Loại cột"));
        values.put("@ChieuCaoCot", findAttr(full, "—", "Chiều cao", "Chiều cao cột"));
        values.put("@SoMong", findAttr(full, "—", "Số móng", "Số móng co"));
        values.put("@SoTangDayCo", findAttr(full, "—", "Số tầng dây co"));
        values.put("@GCX", findAttr(full, "—", "Tầng giá xoay", "Số gá chống xoay", "Gá chống xoay"));

        // Số liệu đo đạc kiểm định kết cấu (lực căng dây co, khóa cáp, siêu âm...) — không có
        // thuộc tính tương ứng trong DHSX_VTK, để trống thay vì bịa số.
        for (String token : STRUCTURAL_MEASUREMENT_TOKENS) {
            values.put(token, "—");
        }

        putSignatories(values, ky);
        return values;
    }

    /** Dùng cho Biên bản số 2 — tổng hợp cả lô, không có thông tin riêng từng trạm. */
    private Map<String, String> buildBatchValues(
            String chuDauTu, String tenTinh, List<VolumeTramRow> rows, BienBanNguoiKyRequest ky) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("@ChuDauTu", chuDauTu);
        values.put("@NhaThau", rows.get(0).getNhaThau());
        values.put("@TenTinh", tenTinh);
        values.put("@SoTram", String.valueOf(rows.size()));
        putSignatories(values, ky);
        return values;
    }

    private static final String[] STRUCTURAL_MEASUREMENT_TOKENS = {
            "@SoDayCo", "@SoKhoaCap", "@CauKienBeTong", "@DoNghieng", "@SieuAm", "@SoLanDo", "@Anten",
    };

    /** Tìm giá trị thuộc tính EAV theo tên (không phân biệt hoa/thường) — trả về fallback nếu không có. */
    private static String findAttr(HopDongDoiTuongResponse obj, String fallback, String... nameCandidates) {
        if (obj == null || obj.getGiaTri() == null) {
            return fallback;
        }
        for (HopDongDoiTuongGiaTriResponse item : obj.getGiaTri()) {
            if (item == null || item.getTenThuocTinh() == null) {
                continue;
            }
            if (Boolean.FALSE.equals(item.getHoatDong())) {
                continue;
            }
            for (String candidate : nameCandidates) {
                if (item.getTenThuocTinh().equalsIgnoreCase(candidate)) {
                    String giaTri = item.getGiaTri();
                    return giaTri != null && !giaTri.isBlank() ? giaTri : fallback;
                }
            }
        }
        return fallback;
    }

    private void putSignatories(Map<String, String> values, BienBanNguoiKyRequest ky) {
        values.put("@CDT_NhanVien", nonBlank(ky.getCdtNguoiGiamSat(), "……………………"));
        values.put("@CDT_ChucVu3", nonBlank(ky.getCdtChucVuGiamSat(), "……………………"));
        values.put("@CDT_GiamDoc", nonBlank(ky.getCdtGiamDoc(), "……………………"));
        values.put("@CDT_ChucVu1", nonBlank(ky.getCdtChucVuGiamDoc(), "……………………"));
        values.put("@CDT_PhuTrach", nonBlank(ky.getCdtNguoiPhuTrach(), "……………………"));
        values.put("@CDT_ChucVu2", nonBlank(ky.getCdtChucVuPhuTrach(), "……………………"));
        values.put("@NT_PhuTrach", nonBlank(ky.getNtNguoiPhuTrach(), "……………………"));
        values.put("@NT_ChucVu2", nonBlank(ky.getNtChucVuPhuTrach(), "……………………"));
        values.put("@NT_GiamDoc", nonBlank(ky.getNtGiamDoc(), "……………………"));
        values.put("@NT_ChucVu1", nonBlank(ky.getNtChucVuGiamDoc(), "……………………"));
    }

    private byte[] fillTemplate(String classpathTemplate, Map<String, String> values) throws IOException {
        byte[] filled;
        try (InputStream in = new ClassPathResource(classpathTemplate).getInputStream();
             XWPFDocument doc = new XWPFDocument(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BienBanTemplateFiller.fill(doc, values);
            doc.write(out);
            filled = out.toByteArray();
        }
        return patchContentType(
                filled,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.template.main+xml",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml");
    }

    /**
     * Mẫu PL4.1 gốc là file .xltx (Excel Template, sheet "tgg") — điền 1 dòng/trạm bắt đầu từ
     * dòng 8 (khớp đúng dongbatdau=8 của hệ thống VB gốc), tra cột theo TÊN ở dòng tiêu đề đầu
     * tiên (m_MaTram, m_LoaiCot...) giống hệt cơ chế insertvalue() gốc — không đổi cấu trúc mẫu.
     */
    private byte[] buildPhuLucExcel(List<VolumeTramRow> selected, Map<UUID, HopDongDoiTuongResponse> fullById)
            throws IOException {
        byte[] filled;
        try (InputStream in = new ClassPathResource(TEMPLATE_PHU_LUC_DANH_SACH).getInputStream();
             XSSFWorkbook wb = new XSSFWorkbook(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.getSheet("tgg");
            if (sheet == null) {
                sheet = wb.getSheetAt(0);
            }
            Map<String, Integer> columnByKey = new LinkedHashMap<>();
            Row header = sheet.getRow(0);
            for (Cell cell : header) {
                String key = cell.getStringCellValue();
                if (key != null && !key.isBlank()) {
                    columnByKey.put(key, cell.getColumnIndex());
                }
            }

            int rowIndex = PHU_LUC_FIRST_DATA_ROW;
            int stt = 1;
            for (VolumeTramRow row : selected) {
                HopDongDoiTuongResponse full = fullById.get(row.getId());
                Row dataRow = sheet.getRow(rowIndex) != null ? sheet.getRow(rowIndex) : sheet.createRow(rowIndex);
                String maTram = row.getMaTram() != null ? row.getMaTram() : "";
                String tinhFromMaTram = maTram.length() >= 3 ? maTram.substring(0, 3).toUpperCase(Locale.ROOT) : maTram;

                setCell(dataRow, columnByKey, "m_STT", String.valueOf(stt));
                setCell(dataRow, columnByKey, "m_Tinh", tinhFromMaTram);
                setCell(dataRow, columnByKey, "m_MaTram", maTram);
                setCell(dataRow, columnByKey, "m_DiaChi", nonBlank(row.getDiaChi(), row.getDiaDiem()));
                setCell(dataRow, columnByKey, "m_LoaiCot", findAttr(full, "", "Loại cột"));
                setCell(dataRow, columnByKey, "m_DoCao", findAttr(full, "", "Chiều cao", "Chiều cao cột"));
                setCell(dataRow, columnByKey, "m_SoMong", findAttr(full, "", "Số móng", "Số móng co"));
                setCell(dataRow, columnByKey, "m_TangCo", findAttr(full, "", "Số tầng dây co"));
                setCell(dataRow, columnByKey, "m_GCX", findAttr(full, "", "Tầng giá xoay", "Số gá chống xoay", "Gá chống xoay"));
                setCell(dataRow, columnByKey, "m_KH", "1");
                setCell(dataRow, columnByKey, "m_GhiChu", "");
                setCell(dataRow, columnByKey, "m_CongTrinh", "1");
                setCell(dataRow, columnByKey, "m_KheHo", "1");
                setCell(dataRow, columnByKey, "m_HeThong", "1");
                // Không có dữ liệu trong DHSX_VTK — để trống thay vì bịa số, giữ nguyên cấu trúc cột.
                setCell(dataRow, columnByKey, "m_Long", "");
                setCell(dataRow, columnByKey, "m_Lat", "");
                setCell(dataRow, columnByKey, "m_SoDayCo", "");
                setCell(dataRow, columnByKey, "m_SoKhoaCap", "");
                setCell(dataRow, columnByKey, "m_CauKienBeTong", "");
                setCell(dataRow, columnByKey, "m_DoNghieng", "");
                setCell(dataRow, columnByKey, "m_SieuAmThanhCanh", "");

                rowIndex++;
                stt++;
            }

            wb.setActiveSheet(wb.getSheetIndex(sheet));
            wb.write(out);
            filled = out.toByteArray();
        }
        return patchContentType(
                filled,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.template.main+xml",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml");
    }

    private static void setCell(Row row, Map<String, Integer> columnByKey, String key, String value) {
        Integer col = columnByKey.get(key);
        if (col == null) {
            return;
        }
        Cell cell = row.getCell(col) != null ? row.getCell(col) : row.createCell(col);
        cell.setCellValue(value == null ? "" : value);
    }

    /**
     * Mẫu gốc (.dotx/.xltx) là file Template Office — POI giữ nguyên content-type khai báo
     * "template" khi ghi lại, nhưng ta lưu kết quả với đuôi .docx/.xlsx nên Word/Excel báo file
     * hỏng vì nội dung khai báo không khớp đuôi file. Sửa đúng 1 dòng trong [Content_Types].xml
     * để nó thực sự là tài liệu thường — không đụng gì khác trong file.
     */
    private static byte[] patchContentType(byte[] filled, String fromType, String toType) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(filled))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                entries.put(entry.getName(), zin.readAllBytes());
            }
        }
        byte[] contentTypes = entries.get("[Content_Types].xml");
        if (contentTypes != null) {
            String xml = new String(contentTypes, StandardCharsets.UTF_8).replace(fromType, toType);
            entries.put("[Content_Types].xml", xml.getBytes(StandardCharsets.UTF_8));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zout = new ZipOutputStream(out)) {
            for (Map.Entry<String, byte[]> e : entries.entrySet()) {
                zout.putNextEntry(new ZipEntry(e.getKey()));
                zout.write(e.getValue());
                zout.closeEntry();
            }
        }
        return out.toByteArray();
    }

    private void writeEntry(ZipOutputStream zip, String name, byte[] content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content);
        zip.closeEntry();
    }

    private static String resolveChuDauTu(HopDongResponse hopDong) {
        return findHopDongAttr(hopDong, "—", "chủ đầu tư", "chu dau tu");
    }

    /** Tìm giá trị thuộc tính EAV của HỢP ĐỒNG theo tên (không phân biệt hoa/thường, khớp từng
     * phần) — trả về fallback nếu kiểu hợp đồng này không cấu hình thuộc tính tương ứng. */
    private static String findHopDongAttr(HopDongResponse hopDong, String fallback, String... nameKeywordsLower) {
        List<HopDongThuocTinhResponse> thuocTinh = hopDong.getThuocTinhGiaTri();
        if (thuocTinh == null) {
            return fallback;
        }
        for (HopDongThuocTinhResponse attr : thuocTinh) {
            String ten = attr.getTenThuocTinh() != null ? attr.getTenThuocTinh().toLowerCase(Locale.ROOT) : "";
            for (String keyword : nameKeywordsLower) {
                if (ten.contains(keyword)) {
                    String giaTri = attr.getGiaTri();
                    return giaTri != null && !giaTri.isBlank() ? giaTri : fallback;
                }
            }
        }
        return fallback;
    }

    private static String nonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private static String safeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "khong-xac-dinh";
        }
        return value.trim().replaceAll("[\\\\/:*?\"<>|]", "-");
    }

    /** Audit duyệt/từ chối biên bản — bung hopDongDoiTuongIdsJson để mỗi trạm liên quan có 1 dòng
     * trong Lịch sử chỉnh sửa trạm. Best-effort, không được làm hỏng thao tác gốc. */
    private void auditBienBan(String action, String label, String detail, UUID hopDongId, List<BienBan> entities) {
        try {
            java.util.Set<UUID> doiTuongIds = new java.util.LinkedHashSet<>();
            for (BienBan entity : entities) {
                String json = entity.getHopDongDoiTuongIdsJson();
                if (json == null || json.isBlank()) {
                    continue;
                }
                for (String raw : objectMapper.readValue(json, String[].class)) {
                    try {
                        doiTuongIds.add(UUID.fromString(raw));
                    } catch (IllegalArgumentException ignored) {
                        // phần tử không phải UUID — bỏ qua
                    }
                }
            }
            java.util.Map<String, Object> attrs = new java.util.HashMap<>();
            attrs.put("hopDongId", hopDongId);
            attrs.put("doiTuongIds", new java.util.ArrayList<>(doiTuongIds));
            appEventContext.audit(action, label, detail, attrs);
        } catch (Exception ignored) {
            appEventContext.audit(action, label, detail, java.util.Map.of("hopDongId", hopDongId));
        }
    }
}
