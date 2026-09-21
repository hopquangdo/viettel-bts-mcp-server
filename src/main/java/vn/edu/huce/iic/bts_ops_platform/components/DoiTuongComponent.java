package vn.edu.huce.iic.bts_ops_platform.components;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.common.cache.CacheService;
import vn.edu.huce.iic.bts_ops_platform.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.dto.doituong.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.dto.doituong.DoiTuongInfoProjection;
import vn.edu.huce.iic.bts_ops_platform.exception.ToolErrorCode;
import vn.edu.huce.iic.bts_ops_platform.helps.UuidHelp;
import vn.edu.huce.iic.bts_ops_platform.repository.DoiTuongRepository;
import vn.edu.huce.iic.bts_ops_platform.support.ResolveSupport;

import java.time.Duration;
import java.util.UUID;

/**
 * Resolve đối tượng từ id/mã/tên. Thứ tự (rẻ trước): UUID (khoá chính) -> khớp ĐÚNG mã (index sẵn có) -> tìm gần đúng theo tên.
 * Tìm gần đúng mơ hồ thì báo lỗi kèm ứng viên, không chọn bừa (xem {@link ResolveSupport}).
 */
@Component
@RequiredArgsConstructor
public class DoiTuongComponent {

    private static final String CACHE_NAME = "mcp-doi-tuong-resolve";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final DoiTuongInfo EMPTY = new DoiTuongInfo(null, null, null);

    private final DoiTuongRepository repository;
    private final CacheService cacheService;

    /**
     * {@code info} là 1 đối tượng CỤ THỂ (trạm, tuyến… — bản ghi hop_dong_doi_tuong) chứ không phải LOẠI đối tượng (Trạm, Tuyến…).
     * Dùng để quyết định bỏ các bảng xếp hạng: chỉ đối tượng cụ thể mới bỏ; lọc theo loại vẫn giữ. Tốn 1 câu SQL nhỏ, chỉ khi có truyền doiTuong.
     */
    public boolean laCuThe(DoiTuongInfo info) {
        return info != null && info.id() != null && repository.countCuTheById(info.id()) > 0;
    }

    public DoiTuongInfo resolve(String value) {
        String keyword = ResolveSupport.normalize(value);
        if (keyword == null) {
            return EMPTY;
        }
        String cacheKey = keyword.toLowerCase();
        DoiTuongInfo info = cacheService.get(CACHE_NAME, cacheKey, DoiTuongInfo.class).orElseGet(() -> {
            DoiTuongInfo resolved = lookup(keyword);
            if (resolved != null) {
                cacheService.put(CACHE_NAME, cacheKey, resolved, CACHE_TTL);
            }
            return resolved;
        });
        if (info == null) {
            throw new AppException(ToolErrorCode.FILTER_NOT_FOUND,
                    "Không tìm thấy đối tượng/trạm với id/mã/tên '" + value + "'");
        }
        return info;
    }

    /**
     * Thứ tự: UUID (loại đối tượng, rồi đối tượng cụ thể) -> khớp ĐÚNG mã loại -> khớp ĐÚNG giá trị khoá chính của 1 đối tượng cụ thể
     * (Mã trạm, Mã tuyến, Tên tòa…) -> tìm gần đúng loại -> tìm gần đúng đối tượng cụ thể. Kết quả {@code id} là id LOẠI đối tượng
     * hoặc id ĐỐI TƯỢNG CỤ THỂ; truy vấn lọc theo cả hai (xem điều kiện {@code doi_tuong_quan_ly_id = x OR d.id = x}).
     */
    private DoiTuongInfo lookup(String keyword) {
        UUID id = UuidHelp.tryParseUuid(keyword);
        if (id != null) {
            var loai = repository.findInfoById(id);
            if (loai.isPresent()) {
                return toInfo(loai.get());
            }
            return repository.countCuTheById(id) > 0 ? new DoiTuongInfo(id, id.toString(), null) : null;
        }
        DoiTuongInfoProjection exact = ResolveSupport.pick("loại đối tượng", keyword,
                repository.findByMaExact(keyword), p -> 0, DoiTuongComponent::label);
        if (exact != null) {
            return toInfo(exact);
        }
        String k = ResolveSupport.lower(keyword);
        // đối tượng cụ thể khớp đúng khoá chính (Mã trạm…): duy nhất thì dùng, trùng nhiều thì báo mơ hồ kèm hợp đồng
        var cuTheExact = ResolveSupport.pick("đối tượng", keyword, repository.findCuTheExact(k), p -> 0, DoiTuongComponent::labelCuThe);
        if (cuTheExact != null) {
            return toInfo(cuTheExact);
        }
        DoiTuongRepository.UngVien loaiGanDung = ResolveSupport.pick("loại đối tượng", keyword,
                repository.searchGanDung(k, ResolveSupport.prefixPattern(k), ResolveSupport.containsPattern(k)),
                DoiTuongRepository.UngVien::getHang, DoiTuongComponent::label);
        if (loaiGanDung != null) {
            return toInfo(loaiGanDung);
        }
        DoiTuongRepository.UngVien cuTheGanDung = ResolveSupport.pick("đối tượng", keyword,
                repository.searchCuThe(ResolveSupport.prefixPattern(k), ResolveSupport.containsPattern(k)),
                DoiTuongRepository.UngVien::getHang, DoiTuongComponent::labelCuThe);
        return cuTheGanDung == null ? null : toInfo(cuTheGanDung);
    }

    /** Nhãn ứng viên đối tượng cụ thể: giá trị khoá kèm mã hợp đồng (vì cùng mã có thể có ở nhiều hợp đồng). */
    private static String labelCuThe(DoiTuongInfoProjection p) {
        return p.getMa() + " (hợp đồng " + p.getTen() + ", id " + p.getId() + ")";
    }

    private static DoiTuongInfo toInfo(DoiTuongInfoProjection p) {
        return new DoiTuongInfo(p.getId(), p.getMa(), p.getTen());
    }

    private static String label(DoiTuongInfoProjection p) {
        return p.getMa() + (p.getTen() != null ? " - " + p.getTen() : "");
    }
}
