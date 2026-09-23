package vn.edu.huce.iic.bts_ops_platform.mcp.handler.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.util.VietnamDateUtils;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.DoiTuongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.common.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.hopdong.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhatky.NhatKyItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhatky.NhatKyQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nhatky.NhatKyRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.exception.ToolErrorCode;
import vn.edu.huce.iic.bts_ops_platform.mcp.handler.NhatKyToolHandler;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.NhatKyToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Impl auditlog_tool: đọc audit_log (ai làm gì, lúc nào) theo hợp đồng, đối tượng, hành động, người thực hiện và khoảng ngày.
 * Chỉ các hành động nghiệp vụ trong {@link #HANH_DONG}; nhật ký đăng nhập/tài khoản không được trả về.
 */
@Component
@RequiredArgsConstructor
public class NhatKyToolHandlerImpl implements NhatKyToolHandler {

    /** Hành động được phép xem -> nhãn tiếng Việt. */
    static final Map<String, String> HANH_DONG = new LinkedHashMap<>();

    static {
        HANH_DONG.put("NHAP_SAN_LUONG", "Nhập sản lượng");
        HANH_DONG.put("SUA_SAN_LUONG", "Sửa sản lượng");
        HANH_DONG.put("XOA_SAN_LUONG", "Xóa sản lượng");
        HANH_DONG.put("NGHIEM_THU_SAN_LUONG", "Nghiệm thu sản lượng");
        HANH_DONG.put("CAP_NHAT_DOI_TUONG", "Cập nhật đối tượng");
        HANH_DONG.put("DOI_TRANG_THAI_DOI_TUONG", "Chuyển trạng thái đối tượng");
        HANH_DONG.put("THEM_DOI_TUONG", "Thêm đối tượng");
        HANH_DONG.put("XOA_DOI_TUONG", "Xóa đối tượng");
        HANH_DONG.put("IMPORT_DOI_TUONG", "Nhập đối tượng từ Excel");
        HANH_DONG.put("THEM_THUOC_TINH", "Thêm thuộc tính");
        HANH_DONG.put("SUA_THUOC_TINH", "Sửa thuộc tính");
        HANH_DONG.put("CAP_NHAT_QUYET_TOAN", "Cập nhật quyết toán");
        HANH_DONG.put("TAO_HOP_DONG", "Tạo hợp đồng");
        HANH_DONG.put("CAP_NHAT_HOP_DONG", "Cập nhật hợp đồng");
        HANH_DONG.put("XOA_HOP_DONG", "Xóa hợp đồng");
    }

    private static final List<String> SAN_LUONG = List.of("NHAP_SAN_LUONG", "SUA_SAN_LUONG", "XOA_SAN_LUONG", "NGHIEM_THU_SAN_LUONG");
    private static final String NHOM_SAN_LUONG = "SAN_LUONG";

    private final NhatKyToolRepository repository;
    private final DoiTuongComponent doiTuongComponent;
    private final HopDongComponent hopDongComponent;
    private final McpParallel parallel;

    @Override
    public NhatKyQueryResponse query(String doiTuong, String hopDong, String hanhDong, String nguoiThucHien,
                                     LocalDate fromDate, LocalDate toDate, String thuTu, Integer page, Integer pageSize) {
        var f_doiTuong = parallel.async(() -> doiTuongComponent.resolve(doiTuong));
        var f_hopDong = parallel.async(() -> hopDongComponent.resolve(hopDong));
        List<String> actions = parseHanhDong(hanhDong);
        boolean cuNhatTruoc = "cu_nhat".equals(ResolveSupport.enumValue("thuTu", thuTu, "moi_nhat", "cu_nhat"));
        UUID doiTuongId = McpParallel.get(f_doiTuong).id();
        UUID hopDongId = McpParallel.get(f_hopDong).id();
        String actionsCsv = String.join(",", actions);
        String nguoi = ResolveSupport.likePattern(nguoiThucHien);
        Instant tuFrom = fromDate != null ? fromDate.atStartOfDay(VietnamDateUtils.ZONE).toInstant() : null;
        Instant denTo = toDate != null ? toDate.plusDays(1).atStartOfDay(VietnamDateUtils.ZONE).toInstant() : null;
        var pageable = PagingUtil.toPageRequest(page, pageSize, PagingUtil.DEFAULT_PAGE_SIZE, PagingUtil.MAX_PAGE_SIZE);
        var one = PagingUtil.toPageRequest(0, 1, 1, 1);

        var fList = parallel.async(() -> repository.danhSach(actionsCsv, hopDongId, doiTuongId, nguoi, tuFrom, denTo, cuNhatTruoc, pageable));
        var fFirst = parallel.async(() -> repository.danhSach(actionsCsv, hopDongId, doiTuongId, nguoi, tuFrom, denTo, true, one));
        var fLast = parallel.async(() -> repository.danhSach(actionsCsv, hopDongId, doiTuongId, nguoi, tuFrom, denTo, false, one));
        boolean coSanLuong = actions.stream().anyMatch(SAN_LUONG::contains);
        var fMoc = coSanLuong ? parallel.async(() -> repository.mocSomNhat(String.join(",", SAN_LUONG))) : null;

        Page<NhatKyRow> list = McpParallel.get(fList);
        List<NhatKyRow> first = McpParallel.get(fFirst).getContent();
        List<NhatKyRow> last = McpParallel.get(fLast).getContent();

        List<UUID> ids = new ArrayList<>();
        for (List<NhatKyRow> rows : List.of(list.getContent(), first, last)) {
            rows.stream().map(NhatKyRow::getDoiTuongId).filter(Objects::nonNull).forEach(ids::add);
        }
        Map<UUID, String> maDoiTuong = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] r : repository.maKhoaChinh(ids.stream().distinct().toList())) {
                maDoiTuong.putIfAbsent((UUID) r[0], (String) r[1]);
            }
        }

        Instant moc = fMoc != null ? McpParallel.get(fMoc) : null;
        String luuY = null;
        if (coSanLuong) {
            luuY = moc == null
                    ? "Chưa có thao tác sản lượng nào được ghi nhật ký; sản lượng nhập trước khi bật tính năng này không có thông tin người thực hiện."
                    : "Nhật ký thao tác sản lượng chỉ có từ " + moc.atZone(VietnamDateUtils.ZONE).toLocalDate()
                            + "; sản lượng nhập hoặc sửa trước mốc này không có thông tin người thực hiện.";
        }
        return NhatKyQueryResponse.builder()
                .tongSo(list.getTotalElements())
                .lanDau(first.isEmpty() ? null : toItem(first.get(0), maDoiTuong))
                .ganNhat(last.isEmpty() ? null : toItem(last.get(0), maDoiTuong))
                .danhSach(PagedResult.of(list.getContent().stream().map(r -> toItem(r, maDoiTuong)).toList(),
                        list.getNumber(), list.getSize(), list.getTotalElements()))
                .sanLuongGhiTuLuc(moc)
                .luuY(luuY)
                .build();
    }

    /** Danh sách hành động (mã cách nhau bởi dấu phẩy, hoặc nhóm san_luong); không truyền thì lấy mọi hành động nghiệp vụ. */
    private static List<String> parseHanhDong(String value) {
        String normalized = ResolveSupport.normalize(value);
        if (normalized == null) {
            return List.copyOf(HANH_DONG.keySet());
        }
        List<String> out = new ArrayList<>();
        for (String part : normalized.split("[,;|]")) {
            String code = part.trim().toUpperCase(Locale.ROOT);
            if (code.isEmpty()) {
                continue;
            }
            if (NHOM_SAN_LUONG.equals(code)) {
                for (String c : SAN_LUONG) {
                    if (!out.contains(c)) {
                        out.add(c);
                    }
                }
            } else if (HANH_DONG.containsKey(code)) {
                if (!out.contains(code)) {
                    out.add(code);
                }
            } else {
                throw new AppException(ToolErrorCode.FILTER_INVALID, "Hành động '" + part.trim() + "' không hợp lệ. Giá trị hợp lệ: "
                        + String.join(", ", HANH_DONG.keySet()) + " hoặc san_luong (gồm cả 4 thao tác sản lượng).");
            }
        }
        return out.isEmpty() ? List.copyOf(HANH_DONG.keySet()) : out;
    }

    private static NhatKyItem toItem(NhatKyRow r, Map<UUID, String> maDoiTuong) {
        return NhatKyItem.builder()
                .thoiGian(r.getNgay())
                .nguoiThucHien(r.getNguoiThucHien())
                .hanhDong(r.getHanhDong())
                .tenHanhDong(HANH_DONG.getOrDefault(r.getHanhDong(), r.getHanhDong()))
                .moTa(r.getMoTa())
                .ip(r.getIp())
                .hopDong(r.getHopDongId() != null ? HopDongInfo.of(r.getHopDongId(), r.getMaHopDong(), r.getTenHopDong()) : null)
                .loaiDoiTuong(r.getLoaiDoiTuong())
                .maDoiTuong(r.getDoiTuongId() != null ? maDoiTuong.get(r.getDoiTuongId()) : null)
                .build();
    }
}
