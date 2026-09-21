package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.infrastructure.security.JwtUserPrincipal;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.dto.response.TepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.file.services.TepDinhKemService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.exception.HopDongErrorCode;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.repository.HopDongRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.entity.BienBan;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.repository.BienBanRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.BienBanPhatSinhChiTietItem;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.request.BienBanPhatSinhTaoRequest;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.response.BienBanPhatSinhResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.dto.response.BienBanPhatSinhTepDinhKemResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.entity.BienBanPhatSinh;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.entity.BienBanPhatSinhTepDinhKem;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.repository.BienBanPhatSinhRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.repository.BienBanPhatSinhTepDinhKemRepository;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.service.TramThiCongGiaiDoanSyncService;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.phatsinh.service.BienBanPhatSinhService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BienBanPhatSinhServiceImpl implements BienBanPhatSinhService {

    private static final String CATEGORY = "bien-ban-phat-sinh";
    private static final String LOAI_PHAT_SINH = "PHAT_SINH";
    private static final String LOAI_YEU_CAU_NGHIEM_THU = "YEU_CAU_NGHIEM_THU";
    private static final String LOAI_NHAN_VAT_TU = "NHAN_VAT_TU";

    private final BienBanPhatSinhRepository bienBanPhatSinhRepository;
    private final BienBanPhatSinhTepDinhKemRepository tepDinhKemLinkRepository;
    private final TepDinhKemService tepDinhKemService;
    private final HopDongRepository hopDongRepository;
    private final BienBanRepository bienBanRepository;
    private final ObjectMapper objectMapper;
    private final TramThiCongGiaiDoanSyncService tramThiCongGiaiDoanSyncService;

    @Override
    @Transactional
    public BienBanPhatSinhResponse create(UUID hopDongId, BienBanPhatSinhTaoRequest request) {
        requireHopDong(hopDongId);
        List<BienBanPhatSinhChiTietItem> chiTiet = normalizeChiTiet(request);
        boolean coTenDaiDien = request != null && request.getTenDauViec() != null && !request.getTenDauViec().isBlank();
        if (!coTenDaiDien && chiTiet.isEmpty()) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Cần nhập ít nhất 1 đầu việc phát sinh");
        }
        JwtUserPrincipal currentUser = SecurityContextHelper.currentUserOrNull();
        String loai = request.getLoai() != null && !request.getLoai().isBlank()
                ? request.getLoai().trim()
                : LOAI_PHAT_SINH;
        UUID hopDongDoiTuongId = request.getHopDongDoiTuongId();
        if (hopDongDoiTuongId != null) {
            if (LOAI_PHAT_SINH.equals(loai)) {
                requireNhanVatTuApproved(hopDongId, hopDongDoiTuongId);
            } else if (LOAI_YEU_CAU_NGHIEM_THU.equals(loai)) {
                requireNghiemThuPrerequisites(hopDongId, hopDongDoiTuongId);
            }
        } else if (LOAI_YEU_CAU_NGHIEM_THU.equals(loai)) {
            requirePhatSinhBeforeNghiemThu(hopDongId, null);
        }
        long soThuTu = bienBanPhatSinhRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDongId)
                .stream()
                .filter(item -> loai.equals(item.getLoai()))
                .count() + 1;
        String maPrefix = LOAI_YEU_CAU_NGHIEM_THU.equals(loai) ? "NT" : "PS";

        BienBanPhatSinhChiTietItem dongDau = chiTiet.isEmpty() ? null : chiTiet.get(0);
        BienBanPhatSinh entity = new BienBanPhatSinh();
        entity.setHopDongId(hopDongId);
        entity.setHopDongDoiTuongId(request.getHopDongDoiTuongId());
        entity.setLoai(loai);
        entity.setMaPhatSinh(maPrefix + "-" + String.format("%03d", soThuTu));
        entity.setTenDauViec(coTenDaiDien ? request.getTenDauViec().trim() : dongDau.getTenDauViec());
        entity.setMoTaLyDo(request.getMoTaLyDo());
        entity.setKhoiLuongPhatSinh(request.getKhoiLuongPhatSinh() != null
                ? request.getKhoiLuongPhatSinh()
                : dongDau != null ? dongDau.getKhoiLuong() : null);
        entity.setDonVi(request.getDonVi() != null ? request.getDonVi() : dongDau != null ? dongDau.getDonVi() : null);
        entity.setChiTietJson(chiTiet.isEmpty() ? null : toJson(chiTiet));
        entity.setNgayLap(request.getNgayLap() != null ? request.getNgayLap() : LocalDate.now());
        entity.setNguoiLapId(currentUser != null ? currentUser.id() : null);
        entity.setNguoiLapTen(currentUser != null ? currentUser.hoTen() : null);
        entity.setTrangThai("cho_duyet");
        bienBanPhatSinhRepository.save(entity);
        syncGiaiDoan(entity);
        return toResponse(entity, List.of());
    }

    /** Lọc bỏ dòng trống, cắt khoảng trắng — trả về danh sách dòng khối lượng hợp lệ. */
    private static List<BienBanPhatSinhChiTietItem> normalizeChiTiet(BienBanPhatSinhTaoRequest request) {
        if (request == null || request.getChiTiet() == null) {
            return List.of();
        }
        return request.getChiTiet().stream()
                .filter(item -> item != null && item.getTenDauViec() != null && !item.getTenDauViec().isBlank())
                .map(item -> new BienBanPhatSinhChiTietItem(
                        item.getTenDauViec().trim(),
                        item.getKhoiLuong(),
                        item.getDonVi() != null ? item.getDonVi().trim() : null,
                        item.getGhiChu() != null ? item.getGhiChu().trim() : null))
                .toList();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Không ghi được chi tiết phát sinh");
        }
    }

    @Override
    @Transactional
    public BienBanPhatSinhResponse kyPhuLuc(UUID hopDongId, UUID id, LocalDate ngayKy) {
        BienBanPhatSinh entity = requireBienBanPhatSinh(hopDongId, id);
        if (!LOAI_PHAT_SINH.equals(entity.getLoai() == null || entity.getLoai().isBlank() ? LOAI_PHAT_SINH : entity.getLoai())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Chỉ biên bản phát sinh khối lượng mới có phụ lục hợp đồng");
        }
        if (!"da_duyet".equals(entity.getTrangThai())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Chỉ ghi ngày ký phụ lục sau khi biên bản phát sinh đã được CĐT phê duyệt");
        }
        entity.setNgayKyPhuLuc(ngayKy != null ? ngayKy : LocalDate.now());
        bienBanPhatSinhRepository.save(entity);
        return toResponse(entity,
                tepDinhKemLinkRepository.findByBienBanPhatSinhIdInAndNgayXoaIsNullAndHoatDongTrue(List.of(entity.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienBanPhatSinhResponse> list(UUID hopDongId) {
        List<BienBanPhatSinh> rows =
                bienBanPhatSinhRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDongId);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = rows.stream().map(BienBanPhatSinh::getId).toList();
        Map<UUID, List<BienBanPhatSinhTepDinhKem>> linksByParent =
                tepDinhKemLinkRepository.findByBienBanPhatSinhIdInAndNgayXoaIsNullAndHoatDongTrue(ids).stream()
                        .collect(Collectors.groupingBy(BienBanPhatSinhTepDinhKem::getBienBanPhatSinhId));
        return rows.stream()
                .map(entity -> toResponse(entity, linksByParent.getOrDefault(entity.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional
    public BienBanPhatSinhResponse uploadTepDinhKem(
            UUID hopDongId, UUID id, MultipartFile file, String loaiTaiLieu, String ghiChu) {
        BienBanPhatSinh entity = requireBienBanPhatSinh(hopDongId, id);

        TepDinhKemResponse tepDinhKem = tepDinhKemService.upload(file, CATEGORY);

        BienBanPhatSinhTepDinhKem link = new BienBanPhatSinhTepDinhKem();
        link.setBienBanPhatSinhId(entity.getId());
        link.setTepDinhKemId(tepDinhKem.getId());
        link.setLoaiTaiLieu(loaiTaiLieu);
        link.setGhiChu(ghiChu);
        link.setHoatDong(true);
        tepDinhKemLinkRepository.save(link);

        List<BienBanPhatSinhTepDinhKem> links =
                tepDinhKemLinkRepository.findByBienBanPhatSinhIdInAndNgayXoaIsNullAndHoatDongTrue(List.of(entity.getId()));
        return toResponse(entity, links);
    }

    @Override
    @Transactional
    public BienBanPhatSinhResponse pheDuyet(UUID hopDongId, UUID id) {
        BienBanPhatSinh entity = requireBienBanPhatSinh(hopDongId, id);
        if (!"cho_duyet".equals(entity.getTrangThai())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Biên bản phát sinh không ở trạng thái chờ duyệt");
        }
        JwtUserPrincipal currentUser = SecurityContextHelper.currentUserOrNull();
        entity.setTrangThai("da_duyet");
        entity.setLyDoTuChoi(null);
        entity.setNguoiPheDuyetId(currentUser != null ? currentUser.id() : null);
        entity.setNguoiPheDuyetTen(currentUser != null ? currentUser.hoTen() : null);
        entity.setNgayPheDuyet(Instant.now());
        bienBanPhatSinhRepository.save(entity);
        syncGiaiDoan(entity);
        return toResponse(entity, tepDinhKemLinkRepository.findByBienBanPhatSinhIdInAndNgayXoaIsNullAndHoatDongTrue(List.of(entity.getId())));
    }

    @Override
    @Transactional
    public BienBanPhatSinhResponse tuChoi(UUID hopDongId, UUID id, String lyDo) {
        if (lyDo == null || lyDo.isBlank()) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Cần nhập lý do từ chối");
        }
        BienBanPhatSinh entity = requireBienBanPhatSinh(hopDongId, id);
        if (!"cho_duyet".equals(entity.getTrangThai())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID, "Biên bản phát sinh không ở trạng thái chờ duyệt");
        }
        JwtUserPrincipal currentUser = SecurityContextHelper.currentUserOrNull();
        entity.setTrangThai("tu_choi");
        entity.setLyDoTuChoi(lyDo.trim());
        entity.setNguoiPheDuyetId(currentUser != null ? currentUser.id() : null);
        entity.setNguoiPheDuyetTen(currentUser != null ? currentUser.hoTen() : null);
        entity.setNgayPheDuyet(Instant.now());
        bienBanPhatSinhRepository.save(entity);
        syncGiaiDoan(entity);
        return toResponse(entity, tepDinhKemLinkRepository.findByBienBanPhatSinhIdInAndNgayXoaIsNullAndHoatDongTrue(List.of(entity.getId())));
    }

    private void syncGiaiDoan(BienBanPhatSinh entity) {
        if (entity.getHopDongDoiTuongId() != null) {
            tramThiCongGiaiDoanSyncService.syncForDoiTuong(entity.getHopDongId(), entity.getHopDongDoiTuongId());
        }
    }

    private void requireHopDong(UUID hopDongId) {
        hopDongRepository.findByIdAndNgayXoaIsNull(hopDongId)
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_NOT_FOUND, "Không tìm thấy hợp đồng"));
    }

    private void requireNghiemThuPrerequisites(UUID hopDongId, UUID hopDongDoiTuongId) {
        requireNhanVatTuApproved(hopDongId, hopDongDoiTuongId);
        requirePhatSinhBeforeNghiemThu(hopDongId, hopDongDoiTuongId);
    }

    private void requireNhanVatTuApproved(UUID hopDongId, UUID hopDongDoiTuongId) {
        if (hopDongDoiTuongId == null) {
            return;
        }
        if (!hasApprovedPreThiCongForStation(hopDongId, hopDongDoiTuongId, LOAI_NHAN_VAT_TU)) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Cần duyệt xong biên bản nhận vật tư trước");
        }
    }

    private boolean hasApprovedPreThiCongForStation(UUID hopDongId, UUID doiTuongId, String loaiBienBan) {
        return bienBanRepository.findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDongId).stream()
                .filter(item -> loaiBienBan.equals(item.getLoaiBienBan()))
                .filter(item -> fromJsonIds(item.getHopDongDoiTuongIdsJson()).contains(doiTuongId))
                .max(Comparator.comparing(BienBan::getNgayTao))
                .map(item -> "da_duyet".equals(item.getTrangThai()))
                .orElse(false);
    }

    private List<UUID> fromJsonIds(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<UUID>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Phát sinh là "nếu có" (họp 17-8) — không bắt buộc phải có, nhưng nếu có thì phải xử lý xong
     * (duyệt hoặc từ chối) trước khi đăng ký nghiệm thu, vì phiếu nghiệm thu phải trọn cả trạm gồm
     * phần phát sinh đã duyệt. */
    private void requirePhatSinhBeforeNghiemThu(UUID hopDongId, UUID hopDongDoiTuongId) {
        boolean conChoDuyet = bienBanPhatSinhRepository
                .findByHopDongIdAndNgayXoaIsNullOrderByNgayLapDescNgayTaoDesc(hopDongId)
                .stream()
                .anyMatch(item -> isPhatSinhLoai(item.getLoai())
                        && matchesDoiTuong(item.getHopDongDoiTuongId(), hopDongDoiTuongId)
                        && "cho_duyet".equals(item.getTrangThai()));
        if (conChoDuyet) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_INVALID,
                    "Còn biên bản phát sinh đang chờ duyệt — duyệt hoặc từ chối xong mới lập yêu cầu nghiệm thu");
        }
    }

    private static boolean isPhatSinhLoai(String loai) {
        return loai == null || loai.isBlank() || LOAI_PHAT_SINH.equals(loai);
    }

    private static boolean matchesDoiTuong(UUID existingDoiTuongId, UUID requestedDoiTuongId) {
        if (requestedDoiTuongId == null) {
            return existingDoiTuongId == null;
        }
        return requestedDoiTuongId.equals(existingDoiTuongId);
    }

    private BienBanPhatSinh requireBienBanPhatSinh(UUID hopDongId, UUID id) {
        BienBanPhatSinh entity = bienBanPhatSinhRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND, "Không tìm thấy biên bản phát sinh"));
        if (!hopDongId.equals(entity.getHopDongId())) {
            throw new AppException(HopDongErrorCode.HOP_DONG_DOI_TUONG_NOT_FOUND, "Không tìm thấy biên bản phát sinh");
        }
        return entity;
    }

    private BienBanPhatSinhResponse toResponse(BienBanPhatSinh entity, List<BienBanPhatSinhTepDinhKem> links) {
        List<BienBanPhatSinhTepDinhKemResponse> files = links.stream()
                .sorted(Comparator.comparing(BienBanPhatSinhTepDinhKem::getNgayTao, Comparator.reverseOrder()))
                .map(link -> {
                    TepDinhKemResponse tep = tepDinhKemService.getById(link.getTepDinhKemId());
                    return BienBanPhatSinhTepDinhKemResponse.builder()
                            .id(link.getId())
                            .tenTep(tep.getTenTepGoc() != null ? tep.getTenTepGoc() : tep.getTenTep())
                            .url(tep.getUrl())
                            .loaiTaiLieu(link.getLoaiTaiLieu())
                            .ghiChu(link.getGhiChu())
                            .ngayTao(link.getNgayTao())
                            .build();
                })
                .toList();

        return BienBanPhatSinhResponse.builder()
                .id(entity.getId())
                .hopDongId(entity.getHopDongId())
                .hopDongDoiTuongId(entity.getHopDongDoiTuongId())
                .maPhatSinh(entity.getMaPhatSinh())
                .tenDauViec(entity.getTenDauViec())
                .moTaLyDo(entity.getMoTaLyDo())
                .khoiLuongPhatSinh(entity.getKhoiLuongPhatSinh())
                .donVi(entity.getDonVi())
                .ngayLap(entity.getNgayLap())
                .nguoiLapId(entity.getNguoiLapId())
                .nguoiLapTen(entity.getNguoiLapTen())
                .trangThai(entity.getTrangThai())
                .lyDoTuChoi(entity.getLyDoTuChoi())
                .nguoiPheDuyetId(entity.getNguoiPheDuyetId())
                .nguoiPheDuyetTen(entity.getNguoiPheDuyetTen())
                .ngayPheDuyet(entity.getNgayPheDuyet())
                .loai(entity.getLoai())
                .chiTiet(parseChiTiet(entity.getChiTietJson()))
                .ngayKyPhuLuc(entity.getNgayKyPhuLuc())
                .tepDinhKem(files)
                .build();
    }

    private List<BienBanPhatSinhChiTietItem> parseChiTiet(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<BienBanPhatSinhChiTietItem>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
