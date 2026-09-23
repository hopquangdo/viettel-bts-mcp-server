package vn.edu.huce.iic.bts_ops_platform.mcp.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.doituong.DoiTuongInfoProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.exception.ToolErrorCode;
import vn.edu.huce.iic.bts_ops_platform.mcp.helps.UuidHelp;
import vn.edu.huce.iic.bts_ops_platform.mcp.repository.DanhMucToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Resolve các danh mục nhỏ thành id: loại hợp đồng, kiểu hợp đồng, bước trạng thái hợp đồng. Bảng chỉ vài dòng nên lấy hết rồi khớp
 * trong bộ nhớ theo thứ tự: UUID -> khớp ĐÚNG mã/tên -> bắt đầu bằng -> chứa; mơ hồ thì báo lỗi kèm ứng viên (xem {@link ResolveSupport}).
 */
@Component
@RequiredArgsConstructor
public class DanhMucComponent {

    private final DanhMucToolRepository repository;

    /** id loại hợp đồng; null nếu không truyền. */
    public UUID resolveLoaiHopDong(String value) {
        return resolve("loại hợp đồng", value, repository::tatCaLoaiHopDong);
    }

    /** id kiểu hợp đồng; null nếu không truyền. */
    public UUID resolveKieuHopDong(String value) {
        return resolve("kiểu hợp đồng", value, repository::tatCaKieuHopDong);
    }

    /**
     * Bước trạng thái hợp đồng (trạng thái của đối tượng trong hợp đồng) -> danh sách id ngăn cách bằng dấu phẩy; null nếu không truyền.
     * Danh mục này có nhiều dòng trùng tên (cùng bước ở các loại hợp đồng khác nhau), nên khớp cùng tên thì lấy tất cả các dòng đó.
     */
    public String resolveTrangThaiHopDongIds(String value) {
        String keyword = ResolveSupport.normalize(value);
        if (keyword == null) {
            return null;
        }
        List<DoiTuongInfoProjection> rows = repository.tatCaTrangThaiHopDong();
        UUID id = UuidHelp.tryParseUuid(keyword);
        if (id != null) {
            if (rows.stream().anyMatch(r -> id.equals(r.getId()))) {
                return id.toString();
            }
            throw notFound("trạng thái hợp đồng", value);
        }
        String k = ResolveSupport.lower(keyword);
        int best = rows.stream().mapToInt(r -> hang(r, k)).min().orElse(9);
        if (best >= 9) {
            throw notFound("trạng thái hợp đồng", value);
        }
        List<DoiTuongInfoProjection> top = rows.stream().filter(r -> hang(r, k) == best).toList();
        long soTen = top.stream().map(r -> r.getTen() == null ? "" : ResolveSupport.lower(r.getTen())).distinct().count();
        if (soTen > 1) {
            // khác tên thật sự: báo mơ hồ (pick ném lỗi kèm ứng viên)
            ResolveSupport.pick("trạng thái hợp đồng", keyword, top, r -> 0, DanhMucComponent::label);
        }
        return top.stream().map(r -> r.getId().toString()).collect(java.util.stream.Collectors.joining(","));
    }

    private UUID resolve(String entity, String value, Supplier<List<DoiTuongInfoProjection>> all) {
        String keyword = ResolveSupport.normalize(value);
        if (keyword == null) {
            return null;
        }
        List<DoiTuongInfoProjection> rows = all.get();
        UUID id = UuidHelp.tryParseUuid(keyword);
        if (id != null) {
            if (rows.stream().anyMatch(r -> id.equals(r.getId()))) {
                return id;
            }
            throw notFound(entity, value);
        }
        String k = ResolveSupport.lower(keyword);
        List<DoiTuongInfoProjection> hits = rows.stream().filter(r -> hang(r, k) < 9).toList();
        DoiTuongInfoProjection picked = ResolveSupport.pick(entity, keyword, hits, r -> hang(r, k), DanhMucComponent::label);
        if (picked == null) {
            throw notFound(entity, value);
        }
        return picked.getId();
    }

    /** 0 = khớp đúng mã/tên, 1 = bắt đầu bằng, 2 = chứa, 9 = không khớp. */
    private static int hang(DoiTuongInfoProjection r, String k) {
        String ma = r.getMa() == null ? "" : ResolveSupport.lower(r.getMa());
        String ten = r.getTen() == null ? "" : ResolveSupport.lower(r.getTen());
        if (ma.equals(k) || ten.equals(k)) {
            return 0;
        }
        if (ma.startsWith(k) || ten.startsWith(k)) {
            return 1;
        }
        return ma.contains(k) || ten.contains(k) ? 2 : 9;
    }

    private static String label(DoiTuongInfoProjection r) {
        return r.getMa() + (r.getTen() != null ? " - " + r.getTen() : "") + " (id " + r.getId() + ")";
    }

    private static AppException notFound(String entity, String value) {
        return new AppException(ToolErrorCode.FILTER_NOT_FOUND, "Không tìm thấy " + entity + " với id/mã/tên '" + value + "'");
    }
}
