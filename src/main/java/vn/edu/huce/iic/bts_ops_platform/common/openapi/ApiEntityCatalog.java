package vn.edu.huce.iic.bts_ops_platform.common.openapi;

import io.swagger.v3.oas.models.tags.Tag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Danh mục API theo entity — nguồn tham chiếu cho {@code @Tag} trên controller và nhóm Swagger UI.
 */
public final class ApiEntityCatalog {

    private ApiEntityCatalog() {
    }

    public static final class XacThuc {
        public static final String TAG = "Xác thực — Đăng nhập";
        public static final String DESCRIPTION = """
                **Bước 1:** Gọi POST /dang-nhap → copy data.accessToken. \
                **Bước 2:** Bấm Authorize (góc phải Swagger) → dán token. \
                Public — không cần Bearer.""";
        public static final String PATH = "/api/v1/xac-thuc";
    }

    public static final class NguoiDung {
        public static final String TAG = "Người dùng";
        public static final String DESCRIPTION = "CRUD tài khoản. Bảng: nguoi_dung. Quyền: " + QuyenHanMa.QUAN_LY_NGUOI_DUNG;
        public static final String PATH = "/api/v1/nguoi-dung";
    }

    public static final class Quyen {
        public static final String TAG = "Quyền";
        public static final String DESCRIPTION = "CRUD vai trò/quyền. Bảng: quyen. Quyền: " + QuyenHanMa.QUAN_LY_QUYEN;
        public static final String PATH = "/api/v1/quyen";
    }

    public static final class QuyenHan {
        public static final String TAG = "Quyền hạn";
        public static final String DESCRIPTION = "CRUD quyền hạn (permission). Bảng: quyen_han. Quyền: " + QuyenHanMa.QUAN_LY_QUYEN_HAN;
        public static final String PATH = "/api/v1/quyen-han";
    }

    public static final class PhanQuyenQuyenHan {
        public static final String TAG = "Gán quyền hạn";
        public static final String DESCRIPTION = "Gán quyền hạn cho quyền. Bảng: phan_quyen_quyen_han. Quyền: " + QuyenHanMa.QUAN_LY_PHAN_QUYEN;
        public static final String PATH = "/api/v1/phan-quyen-quyen-han";
    }

    public static final class KhuVuc {
        public static final String TAG = "Khu vực";
        public static final String DESCRIPTION = "CRUD khu vực. Bảng: khu_vuc. Quyền: " + QuyenHanMa.QUAN_LY_KHU_VUC;
        public static final String PATH = "/api/v1/khu-vuc";
    }

    public static final class TinhThanh {
        public static final String TAG = "Tỉnh/Thành";
        public static final String DESCRIPTION = "CRUD tỉnh thành. Bảng: tinh_thanh. Quyền: " + QuyenHanMa.QUAN_LY_TINH_THANH;
        public static final String PATH = "/api/v1/tinh-thanh";
    }

    public static final class LoaiHopDong {
        public static final String TAG = "Loại hợp đồng";
        public static final String DESCRIPTION = "CRUD loại hợp đồng. Bảng: loai_hop_dong. Quyền: " + QuyenHanMa.QUAN_LY_LOAI_HOP_DONG;
        public static final String PATH = "/api/v1/loai-hop-dong";
    }

    public static final class KieuHopDong {
        public static final String TAG = "Kiểu hợp đồng";
        public static final String DESCRIPTION = "CRUD kiểu hợp đồng. Bảng: kieu_hop_dong. Quyền: " + QuyenHanMa.QUAN_LY_KIEU_HOP_DONG;
        public static final String PATH = "/api/v1/kieu-hop-dong";
    }

    public static final class DoiTuongQuanLy {
        public static final String TAG = "Đối tượng quản lý";
        public static final String DESCRIPTION = "CRUD đối tượng quản lý. Bảng: doi_tuong_quan_ly. "
                + "Field hienThiTrenGiaoDien: false = ẩn selector import HĐ. "
                + "Query selectorOnly=true để lọc. Quyền: " + QuyenHanMa.QUAN_LY_DOI_TUONG_QUAN_LY;
        public static final String PATH = "/api/v1/doi-tuong-quan-ly";
    }

    public static final class DoiTuongHopDongLienKet {
        public static final String TAG = "Đối tượng — Liên kết HĐ";
        public static final String DESCRIPTION = "Gắn đối tượng quản lý với loại/kiểu HĐ. Bảng: doi_tuong_hop_dong_lien_ket. Quyền: " + QuyenHanMa.QUAN_LY_DOI_TUONG_QUAN_LY;
        public static final String PATH = "/api/v1/doi-tuong-hop-dong-lien-ket";
    }

    public static final class LienKetHopDong {
        public static final String TAG = "Liên kết kiểu HĐ";
        public static final String DESCRIPTION = "Cấu hình liên kết group/follow giữa các kiểu HĐ. Bảng: lien_ket_hop_dong. Quyền: " + QuyenHanMa.QUAN_LY_KIEU_HOP_DONG;
        public static final String PATH = "/api/v1/lien-ket-hop-dong";
    }

    public static final class TrangThaiHopDong {
        public static final String TAG = "Trạng thái hợp đồng";
        public static final String DESCRIPTION = "CRUD trạng thái hợp đồng. Bảng: trang_thai_hop_dong. Quyền: " + QuyenHanMa.QUAN_LY_TRANG_THAI_HOP_DONG;
        public static final String PATH = "/api/v1/trang-thai-hop-dong";
    }

    public static final class LuongTrangThai {
        public static final String TAG = "Luồng trạng thái";
        public static final String DESCRIPTION = "CRUD luồng trạng thái (danh sách trạng thái có thứ tự). Bảng: luong_trang_thai. Quyền: " + QuyenHanMa.QUAN_LY_TRANG_THAI_HOP_DONG;
        public static final String PATH = "/api/v1/luong-trang-thai";
    }

    public static final class KieuHopDongLuongTrangThai {
        public static final String TAG = "Kiểu HĐ — Luồng TT";
        public static final String DESCRIPTION = "Gắn luồng trạng thái cho kiểu hợp đồng. Bảng: kieu_hop_dong_luong_trang_thai. Quyền: " + QuyenHanMa.QUAN_LY_HOP_DONG_TRANG_THAI;
        public static final String PATH = "/api/v1/kieu-hop-dong-luong-trang-thai";
    }

    public static final class KieuDuLieu {
        public static final String TAG = "Kiểu dữ liệu";
        public static final String DESCRIPTION = "CRUD kiểu dữ liệu thuộc tính. Bảng: kieu_du_lieu. Quyền: " + QuyenHanMa.QUAN_LY_KIEU_DU_LIEU;
        public static final String PATH = "/api/v1/kieu-du-lieu";
    }

    public static final class ExcelMapping {
        public static final String TAG = "Excel mapping";
        public static final String DESCRIPTION = "CRUD ánh xạ Excel. Bảng: excel_mapping, excel_mapping_cot. Quyền: " + QuyenHanMa.QUAN_LY_EXCEL_MAPPING;
        public static final String PATH = "/api/v1/excel-mapping";
    }

    public static final class ThuocTinh {
        public static final String TAG = "Thuộc tính";
        public static final String DESCRIPTION = "CRUD thuộc tính động. Bảng: thuoc_tinh. Quyền: " + QuyenHanMa.QUAN_LY_THUOC_TINH;
        public static final String PATH = "/api/v1/thuoc-tinh";
    }

    public static final class ThuocTinhHopDong {
        public static final String TAG = "Thuộc tính HĐ";
        public static final String DESCRIPTION = "Thư viện thuộc tính thông tin hợp đồng. Bảng: thuoc_tinh_hop_dong. Quyền: " + QuyenHanMa.QUAN_LY_THUOC_TINH;
        public static final String PATH = "/api/v1/thuoc-tinh-hop-dong";
    }

    public static final class ThuocTinhKieuHopDong {
        public static final String TAG = "Thuộc tính — Kiểu HĐ";
        public static final String DESCRIPTION = "Gắn thuộc tính HĐ theo kiểu hợp đồng. Bảng: thuoc_tinh_kieu_hop_dong. Quyền: " + QuyenHanMa.QUAN_LY_THUOC_TINH;
        public static final String PATH = "/api/v1/thuoc-tinh-kieu-hop-dong";
    }

    public static final class DanhMucBienBan {
        public static final String TAG = "Danh mục biên bản";
        public static final String DESCRIPTION = "Thư viện các loại biên bản/hồ sơ dùng chung cho cả 2 luồng thi công và tư vấn thiết kế. Bảng: danh_muc_bien_ban. Quyền: " + QuyenHanMa.QUAN_LY_KIEU_HOP_DONG;
        public static final String PATH = "/api/v1/danh-muc-bien-ban";
    }

    public static final class KieuHopDongDanhMucBienBan {
        public static final String TAG = "Kiểu HĐ — Danh mục biên bản";
        public static final String DESCRIPTION = "Gắn danh mục biên bản theo kiểu hợp đồng (checklist hồ sơ riêng cho từng kiểu HĐ). Bảng: kieu_hop_dong_danh_muc_bien_ban. Quyền: " + QuyenHanMa.QUAN_LY_KIEU_HOP_DONG;
        public static final String PATH = "/api/v1/kieu-hop-dong-danh-muc-bien-ban";
    }

    public static final class HopDongTrangThai {
        public static final String TAG = "HĐ — Trạng thái";
        public static final String DESCRIPTION = "Cấu hình trạng thái theo loại/kiểu HĐ. Bảng: hop_dong_trang_thai. Quyền: " + QuyenHanMa.QUAN_LY_HOP_DONG_TRANG_THAI;
        public static final String PATH = "/api/v1/hop-dong-trang-thai";
    }

    public static final class HopDong {
        public static final String TAG = "Hợp đồng";
        public static final String DESCRIPTION = "CRUD hợp đồng. Bảng: hop_dong. Quyền: " + QuyenHanMa.QUAN_LY_HOP_DONG;
        public static final String PATH = "/api/v1/hop-dong";
    }

    public static final class HopDongLienKet {
        public static final String TAG = "HĐ — Liên kết instance";
        public static final String DESCRIPTION = "Liên kết hợp đồng cụ thể theo cấu hình kiểu HĐ. Bảng: hop_dong_lien_ket. Quyền: " + QuyenHanMa.QUAN_LY_HOP_DONG;
        public static final String PATH = "/api/v1/hop-dong-lien-ket";
    }

    public static final class ChuDauTu {
        public static final String TAG = "Chủ đầu tư";
        public static final String DESCRIPTION = "CRUD chủ đầu tư. Bảng: chu_dau_tu. Quyền: " + QuyenHanMa.QUAN_LY_CHU_DAU_TU;
        public static final String PATH = "/api/v1/chu-dau-tu";
    }

    public static final class HopDongThuocTinh {
        public static final String TAG = "HĐ — Thuộc tính";
        public static final String DESCRIPTION = "Giá trị thuộc tính hợp đồng. Bảng: hop_dong_thuoc_tinh. Quyền: " + QuyenHanMa.QUAN_LY_HOP_DONG_THUOC_TINH;
        public static final String PATH = "/api/v1/hop-dong-thuoc-tinh";
    }

    public static final class HopDongTepDinhKem {
        public static final String TAG = "HĐ — Tệp đính kèm";
        public static final String DESCRIPTION = "Liên kết tệp với hợp đồng. Bảng: hop_dong_tep_dinh_kem. Quyền: " + QuyenHanMa.QUAN_LY_HOP_DONG_TEP_DINH_KEM;
        public static final String PATH = "/api/v1/hop-dong-tep-dinh-kem";
    }

    public static final class HopDongNhomUuTien {
        public static final String TAG = "HĐ — Nhóm ưu tiên";
        public static final String DESCRIPTION = "Nhóm ưu tiên hợp đồng. Bảng: hop_dong_nhom_uu_tien. Quyền: " + QuyenHanMa.QUAN_LY_HOP_DONG_NHOM_UU_TIEN;
        public static final String PATH = "/api/v1/hop-dong-nhom-uu-tien";
    }

    public static final class HopDongDoiTuong {
        public static final String TAG = "HĐ — Đối tượng";
        public static final String DESCRIPTION = "Đối tượng thuộc hợp đồng. Bảng: hop_dong_doi_tuong. Quyền: " + QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG;
        public static final String PATH = "/api/v1/hop-dong-doi-tuong";
    }

    public static final class HopDongDoiTuongGiaTri {
        public static final String TAG = "HĐ — Giá trị đối tượng";
        public static final String DESCRIPTION = "Giá trị thuộc tính đối tượng HĐ. Bảng: hop_dong_doi_tuong_gia_tri. Quyền: " + QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG_GIA_TRI;
        public static final String PATH = "/api/v1/hop-dong-doi-tuong-gia-tri";
    }

    public static final class HopDongDoiTuongTrangThai {
        public static final String TAG = "HĐ — TT đối tượng";
        public static final String DESCRIPTION = "Trạng thái đối tượng hợp đồng. Bảng: hop_dong_doi_tuong_trang_thai. Quyền: " + QuyenHanMa.QUAN_LY_HOP_DONG_DOI_TUONG_TRANG_THAI;
        public static final String PATH = "/api/v1/hop-dong-doi-tuong-trang-thai";
    }

    public static final class HangMucNhom {
        public static final String TAG = "Hạng mục — Nhóm";
        public static final String DESCRIPTION = "Nhóm hạng mục công việc. Bảng: hang_muc_nhom. Quyền: " + QuyenHanMa.QUAN_LY_HANG_MUC_NHOM;
        public static final String PATH = "/api/v1/hang-muc-nhom";
    }

    public static final class HangMucChiTiet {
        public static final String TAG = "Hạng mục — Chi tiết";
        public static final String DESCRIPTION = "Chi tiết hạng mục. Bảng: hang_muc_chi_tiet. Quyền: " + QuyenHanMa.QUAN_LY_HANG_MUC_CHI_TIET;
        public static final String PATH = "/api/v1/hang-muc-chi-tiet";
    }

    public static final class HangMucCongViec {
        public static final String TAG = "Hạng mục — Công việc";
        public static final String DESCRIPTION = "Công việc hạng mục. Bảng: hang_muc_cong_viec. Quyền: " + QuyenHanMa.QUAN_LY_HANG_MUC_CONG_VIEC;
        public static final String PATH = "/api/v1/hang-muc-cong-viec";
    }

    public static final class SanLuong {
        public static final String TAG = "Sản lượng";
        public static final String DESCRIPTION = "Sản lượng thi công. Bảng: san_luong. Quyền: " + QuyenHanMa.QUAN_LY_SAN_LUONG;
        public static final String PATH = "/api/v1/san-luong";
    }

    public static final class PhanCong {
        public static final String TAG = "Phân công";
        public static final String DESCRIPTION = "CRUD phân công. Bảng: phan_cong. Quyền: " + QuyenHanMa.QUAN_LY_PHAN_CONG;
        public static final String PATH = "/api/v1/phan-cong";
    }

    public static final class VuongMac {
        public static final String TAG = "Vướng mắc";
        public static final String DESCRIPTION = "Quản lý vướng mắc. Bảng: vuong_mac. Quyền: " + QuyenHanMa.QUAN_LY_VUONG_MAC;
        public static final String PATH = "/api/v1/vuong-mac";
    }

    public static final class LuuTruHopDong {
        public static final String TAG = "Lưu trữ hợp đồng";
        public static final String DESCRIPTION = "Quản lý lưu trữ hợp đồng. Bảng: hop_dong_luu_tru. Quyền: " + QuyenHanMa.QUAN_LY_LUU_TRU;
        public static final String PATH = "/api/v1/luu-tru/hop-dong";
    }

    public static final class Volume {
        public static final String TAG = "Volume hợp đồng";
        public static final String DESCRIPTION = "Kiểm soát volume HĐ: tổng quan/cảnh báo/khu vực. Bảng nguồn: hop_dong, san_luong, hop_dong_doi_tuong. Quyền: " + QuyenHanMa.QUAN_LY_LUONG_HOP_DONG;
        public static final String PATH = "/api/v1/volume";
    }

    public static final class NguonLuc {
        public static final String TAG = "Nguồn lực";
        public static final String DESCRIPTION = "Quản lý nguồn việc: danh sách HĐ, overlay KV, lưu bảng. Quyền: " + QuyenHanMa.QUAN_LY_NGUON_LUC;
        public static final String PATH = "/api/v1/nguon-luc";
    }

    public static final class Dashboard {
        public static final String TAG = "Dashboard tổng quan";
        public static final String DESCRIPTION = "Chỉ số tổng quan dashboard (đối tượng, HĐ, vướng mắc, khu vực, sản lượng, theo loại HĐ). "
                + "Quyền: " + QuyenHanMa.QUAN_LY_HOP_DONG + " hoặc " + QuyenHanMa.QUAN_LY_SAN_LUONG;
        public static final String PATH = "/api/v1/dashboard";
    }

    public static final class ThongBao {
        public static final String TAG = "Thông báo";
        public static final String DESCRIPTION = "Hộp thư thông báo in-app: phân công, deadline KPI, workflow, cảnh báo.";
        public static final String PATH = "/api/v1/thong-bao";
    }

    public static final class CanhBao {
        public static final String TAG = "Cảnh báo";
        public static final String DESCRIPTION = "Cảnh báo nghiệp vụ: tỷ lệ hủy trạm, ngưỡng, đánh dấu kiểm tra SL bất thường.";
        public static final String PATH = "/api/v1/canh-bao";
    }

    public static final class Realtime {
        public static final String TAG = "Realtime";
        public static final String DESCRIPTION = "SSE stream cập nhật dashboard và thông báo tức thời.";
        public static final String PATH = "/api/v1/realtime";
    }

    public static final class TramTon {
        public static final String TAG = "Trạm tồn";
        public static final String DESCRIPTION = "Quản lý trạm tồn theo điều kiện doanh thu (HT / vướng mắc / pháp lý / QT). "
                + "Quyền: " + QuyenHanMa.QUAN_LY_HOP_DONG;
        public static final String PATH = "/api/v1/tram-ton";
    }

    public static final class TienDo {
        public static final String TAG = "Tiến độ HĐ thi công";
        public static final String DESCRIPTION = "Quản lý tiến độ HĐ thi công: mốc thời gian, checklist, kế hoạch, vật tư, KPI cảnh báo. "
                + "Quyền: " + QuyenHanMa.QUAN_LY_HOP_DONG;
        public static final String PATH = "/api/v1/tien-do";
    }

    public static final class TroLyAI {
        public static final String TAG = "Trợ lý AI";
        public static final String DESCRIPTION = "Hỏi đáp hỗ trợ nội bộ. Quyền: " + QuyenHanMa.QUAN_LY_TRO_LY_AI;
        public static final String PATH = "/api/v1/tro-ly-ai";
    }

    public static final class AuditLog {
        public static final String TAG = "Audit log";
        public static final String DESCRIPTION = "Nhật ký thao tác. Bảng: audit_log. Quyền: " + QuyenHanMa.QUAN_LY_AUDIT_LOG;
        public static final String PATH = "/api/v1/audit-log";
    }

    public static final class TepDinhKem {
        public static final String TAG = "Tệp đính kèm";
        public static final String DESCRIPTION = "Upload & quản lý tệp. Bảng: tep_dinh_kem. Quyền: " + QuyenHanMa.QUAN_LY_TEP_DINH_KEM;
        public static final String PATH = "/api/v1/tep-dinh-kem";
    }

    /** Nhóm tag trên Swagger UI (extension {@code x-tagGroups}). */
    public static List<Map<String, Object>> tagGroups() {
        return List.of(
                tagGroup("Xác thực & Người dùng", XacThuc.TAG, NguoiDung.TAG),
                tagGroup("Phân quyền", Quyen.TAG, QuyenHan.TAG, PhanQuyenQuyenHan.TAG),
                tagGroup("Thư viện", KhuVuc.TAG, TinhThanh.TAG),
                tagGroup("Cấu hình hợp đồng",
                        LoaiHopDong.TAG, KieuHopDong.TAG, DoiTuongQuanLy.TAG, DoiTuongHopDongLienKet.TAG, LienKetHopDong.TAG, TrangThaiHopDong.TAG,
                        LuongTrangThai.TAG, KieuHopDongLuongTrangThai.TAG,
                        KieuDuLieu.TAG, ExcelMapping.TAG, ThuocTinh.TAG, ThuocTinhHopDong.TAG, ThuocTinhKieuHopDong.TAG, HopDongTrangThai.TAG,
                        DanhMucBienBan.TAG, KieuHopDongDanhMucBienBan.TAG),
                tagGroup("Hợp đồng",
                        HopDong.TAG, HopDongLienKet.TAG, ChuDauTu.TAG, HopDongThuocTinh.TAG, HopDongTepDinhKem.TAG,
                        HopDongNhomUuTien.TAG, HopDongDoiTuong.TAG, HopDongDoiTuongGiaTri.TAG,
                        HopDongDoiTuongTrangThai.TAG),
                tagGroup("Hạng mục & Sản lượng",
                        HangMucNhom.TAG, HangMucChiTiet.TAG, HangMucCongViec.TAG, SanLuong.TAG),
                tagGroup("Vận hành & giám sát",
                        PhanCong.TAG, LuuTruHopDong.TAG, Volume.TAG, NguonLuc.TAG, Dashboard.TAG,
                        ThongBao.TAG, CanhBao.TAG, Realtime.TAG,
                        TramTon.TAG, TienDo.TAG, TroLyAI.TAG, AuditLog.TAG, VuongMac.TAG, TepDinhKem.TAG)
        );
    }

    /** Thứ tự tag đăng ký trên OpenAPI (giữ thứ tự nhóm entity). */
    public static List<Tag> orderedTags() {
        LinkedHashMap<String, String> tagDescriptions = new LinkedHashMap<>();
        Stream.of(
                XacThuc.TAG, NguoiDung.TAG,
                Quyen.TAG, QuyenHan.TAG, PhanQuyenQuyenHan.TAG,
                KhuVuc.TAG, TinhThanh.TAG,
                LoaiHopDong.TAG, KieuHopDong.TAG, DoiTuongQuanLy.TAG, DoiTuongHopDongLienKet.TAG, LienKetHopDong.TAG, TrangThaiHopDong.TAG,
                LuongTrangThai.TAG, KieuHopDongLuongTrangThai.TAG,
                KieuDuLieu.TAG, ThuocTinh.TAG, ThuocTinhHopDong.TAG, ThuocTinhKieuHopDong.TAG, HopDongTrangThai.TAG,
                DanhMucBienBan.TAG, KieuHopDongDanhMucBienBan.TAG,
                ExcelMapping.TAG,
                HopDong.TAG, HopDongLienKet.TAG, ChuDauTu.TAG, HopDongThuocTinh.TAG, HopDongTepDinhKem.TAG,
                HopDongNhomUuTien.TAG, HopDongDoiTuong.TAG, HopDongDoiTuongGiaTri.TAG, HopDongDoiTuongTrangThai.TAG,
                HangMucNhom.TAG, HangMucChiTiet.TAG, HangMucCongViec.TAG, SanLuong.TAG,
                PhanCong.TAG, LuuTruHopDong.TAG, Volume.TAG,
                NguonLuc.TAG, Dashboard.TAG, ThongBao.TAG, CanhBao.TAG, Realtime.TAG,
                TramTon.TAG, TienDo.TAG, TroLyAI.TAG, AuditLog.TAG,
                VuongMac.TAG, TepDinhKem.TAG
        ).forEach(tag -> tagDescriptions.putIfAbsent(tag, descriptionFor(tag)));

        return tagDescriptions.entrySet().stream()
                .map(e -> new Tag().name(e.getKey()).description(e.getValue()))
                .toList();
    }

    private static Map<String, Object> tagGroup(String name, String... tags) {
        return Map.of("name", name, "tags", List.of(tags));
    }

    private static String descriptionFor(String tag) {
        return switch (tag) {
            case XacThuc.TAG -> XacThuc.DESCRIPTION;
            case NguoiDung.TAG -> NguoiDung.DESCRIPTION;
            case Quyen.TAG -> Quyen.DESCRIPTION;
            case QuyenHan.TAG -> QuyenHan.DESCRIPTION;
            case PhanQuyenQuyenHan.TAG -> PhanQuyenQuyenHan.DESCRIPTION;
            case KhuVuc.TAG -> KhuVuc.DESCRIPTION;
            case TinhThanh.TAG -> TinhThanh.DESCRIPTION;
            case LoaiHopDong.TAG -> LoaiHopDong.DESCRIPTION;
            case KieuHopDong.TAG -> KieuHopDong.DESCRIPTION;
            case DoiTuongQuanLy.TAG -> DoiTuongQuanLy.DESCRIPTION;
            case DoiTuongHopDongLienKet.TAG -> DoiTuongHopDongLienKet.DESCRIPTION;
            case LienKetHopDong.TAG -> LienKetHopDong.DESCRIPTION;
            case TrangThaiHopDong.TAG -> TrangThaiHopDong.DESCRIPTION;
            case LuongTrangThai.TAG -> LuongTrangThai.DESCRIPTION;
            case KieuHopDongLuongTrangThai.TAG -> KieuHopDongLuongTrangThai.DESCRIPTION;
            case KieuDuLieu.TAG -> KieuDuLieu.DESCRIPTION;
            case ExcelMapping.TAG -> ExcelMapping.DESCRIPTION;
            case ThuocTinh.TAG -> ThuocTinh.DESCRIPTION;
            case ThuocTinhHopDong.TAG -> ThuocTinhHopDong.DESCRIPTION;
            case ThuocTinhKieuHopDong.TAG -> ThuocTinhKieuHopDong.DESCRIPTION;
            case DanhMucBienBan.TAG -> DanhMucBienBan.DESCRIPTION;
            case KieuHopDongDanhMucBienBan.TAG -> KieuHopDongDanhMucBienBan.DESCRIPTION;
            case HopDongTrangThai.TAG -> HopDongTrangThai.DESCRIPTION;
            case HopDong.TAG -> HopDong.DESCRIPTION;
            case HopDongLienKet.TAG -> HopDongLienKet.DESCRIPTION;
            case ChuDauTu.TAG -> ChuDauTu.DESCRIPTION;
            case HopDongThuocTinh.TAG -> HopDongThuocTinh.DESCRIPTION;
            case HopDongTepDinhKem.TAG -> HopDongTepDinhKem.DESCRIPTION;
            case HopDongNhomUuTien.TAG -> HopDongNhomUuTien.DESCRIPTION;
            case HopDongDoiTuong.TAG -> HopDongDoiTuong.DESCRIPTION;
            case HopDongDoiTuongGiaTri.TAG -> HopDongDoiTuongGiaTri.DESCRIPTION;
            case HopDongDoiTuongTrangThai.TAG -> HopDongDoiTuongTrangThai.DESCRIPTION;
            case HangMucNhom.TAG -> HangMucNhom.DESCRIPTION;
            case HangMucChiTiet.TAG -> HangMucChiTiet.DESCRIPTION;
            case HangMucCongViec.TAG -> HangMucCongViec.DESCRIPTION;
            case SanLuong.TAG -> SanLuong.DESCRIPTION;
            case PhanCong.TAG -> PhanCong.DESCRIPTION;
            case LuuTruHopDong.TAG -> LuuTruHopDong.DESCRIPTION;
            case Volume.TAG -> Volume.DESCRIPTION;
            case NguonLuc.TAG -> NguonLuc.DESCRIPTION;
            case Dashboard.TAG -> Dashboard.DESCRIPTION;
            case ThongBao.TAG -> ThongBao.DESCRIPTION;
            case CanhBao.TAG -> CanhBao.DESCRIPTION;
            case Realtime.TAG -> Realtime.DESCRIPTION;
            case TramTon.TAG -> TramTon.DESCRIPTION;
            case TienDo.TAG -> TienDo.DESCRIPTION;
            case TroLyAI.TAG -> TroLyAI.DESCRIPTION;
            case AuditLog.TAG -> AuditLog.DESCRIPTION;
            case VuongMac.TAG -> VuongMac.DESCRIPTION;
            case TepDinhKem.TAG -> TepDinhKem.DESCRIPTION;
            default -> tag;
        };
    }
}
