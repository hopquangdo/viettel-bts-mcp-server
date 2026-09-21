package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.DoiTuongHopDongLienKetResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Helper xác định đối tượng quản lý tab danh sách (trừ Hạng mục thi công).
 */
public final class DoiTuongHopDongLienKetSupport {

  public static final UUID HANG_MUC_THI_CONG_DOI_TUONG_ID =
      UUID.fromString("d77c2f9d-b5ca-48f6-95fe-6574c9fda5b2");

  public static final String THIEU_CAU_HINH_MESSAGE =
      "Kiểu hợp đồng chưa gắn đối tượng quản lý. Vui lòng cấu hình tại Cấu hình → Kiểu hợp đồng → Gắn đối tượng.";

  private static final Pattern HANG_MUC_PATTERN =
      Pattern.compile("hạng\\s*mục|hang\\s*muc|boq|hm\\s", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private DoiTuongHopDongLienKetSupport() {
  }

  public static boolean isHangMucDoiTuong(DoiTuongHopDongLienKetResponse link) {
    if (link == null) {
      return false;
    }
    return isHangMucDoiTuong(link.getDoiTuongQuanLyId(), link.getDoiTuongTen(), link.getDoiTuongMa());
  }

  public static boolean isHangMucDoiTuong(UUID doiTuongQuanLyId, String ten, String ma) {
    if (HANG_MUC_THI_CONG_DOI_TUONG_ID.equals(doiTuongQuanLyId)) {
      return true;
    }
    String label = String.format(
        "%s %s",
        ten == null ? "" : ten,
        ma == null ? "" : ma);
    return HANG_MUC_PATTERN.matcher(label).find();
  }

  /** Đối tượng dùng cho tab danh sách trạm — không lấy HM thi công, không fallback mặc định. */
  public static Optional<DoiTuongHopDongLienKetResponse> resolveStationLink(
      List<DoiTuongHopDongLienKetResponse> links) {
    if (links == null || links.isEmpty()) {
      return Optional.empty();
    }
    return links.stream().filter(link -> !isHangMucDoiTuong(link)).findFirst();
  }

  public static boolean hasStationDoiTuong(List<DoiTuongHopDongLienKetResponse> links) {
    return resolveStationLink(links).isPresent();
  }

  public static boolean isAllowedStationDoiTuong(
      List<DoiTuongHopDongLienKetResponse> links, UUID doiTuongQuanLyId) {
    if (doiTuongQuanLyId == null || links == null) {
      return false;
    }
    return links.stream()
        .filter(link -> !isHangMucDoiTuong(link))
        .anyMatch(link -> doiTuongQuanLyId.equals(link.getDoiTuongQuanLyId()));
  }
}
