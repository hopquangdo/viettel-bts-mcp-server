package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class HopDongDanhSachTramRowResponse {
    private UUID id;
    private String maTram;
    private String diaDiem;
    private String linhVuc;
    private String cao;
    private String nhaThau;
    private String trangThaiMa;
    /** Trạng thái luồng HĐ (Khởi công, Xong móng, Khảo sát…) — dùng ở danh sách trạm/tuyến. */
    private String trangThaiTen;
    private String trangThaiMauSac;
    /** Giai đoạn suy từ biên bản thi công — chỉ dùng ở trang biên bản & hồ sơ. */
    private String trangThaiBienBanTen;
    private String nhomUuTienTen;
    private String nhomUuTienMauSac;
    private LocalDate ngayBatDau;
    private LocalDate ngayHt;
    /** % hoàn thành theo sản lượng thi công thực tế / kế hoạch của đối tượng — null nếu chưa có kế hoạch để so sánh. */
    private Double tyLeHoanThanh;
    private List<HopDongDanhSachTramThuocTinhItemResponse> thuocTinhGiaTri = new ArrayList<>();
    private Instant ngayTao;
    private Instant ngayCapNhat;
}
