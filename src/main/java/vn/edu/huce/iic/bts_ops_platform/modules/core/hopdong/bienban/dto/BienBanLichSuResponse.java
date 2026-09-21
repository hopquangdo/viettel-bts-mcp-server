package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.bienban.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BienBanLichSuResponse {
    private UUID id;
    private String maBienBan;
    private String loaiBienBan;
    private String loaiBienBanTen;
    private LocalDate ngayLap;
    private UUID nguoiLapId;
    private String nguoiLapTen;
    private String trangThai;
    private Integer soLuongDoiTuong;
    private String phamViXuat;
    private String phamViChiTiet;
    private String lyDoTuChoi;
    private UUID nguoiPheDuyetId;
    private String nguoiPheDuyetTen;
    private Instant ngayPheDuyet;
    private UUID bienBanGocId;
    /** true khi trangThai = tu_choi — FE dùng để hiện nút "Xuất lại". */
    private boolean coTheXuatLai;
    /** true khi trangThai = da_duyet và còn hiệu lực — FE dùng để hiện nút "Tạo bản thay thế". */
    private boolean coTheTaoBanThayThe;
    private Boolean hoatDong;
    private String lyDoHuyHieuLuc;
    private Instant ngayHuyHieuLuc;
    private String nguoiHuyTen;
    /** Danh sách hopDongDoiTuongId thuộc biên bản này — FE dùng để nối trạng thái biên bản vào
     * đúng dòng trạm trong "Danh sách trạm theo tỉnh". Rỗng nếu biên bản tạo trước khi có field này. */
    private List<UUID> hopDongDoiTuongIds;
    /** Tên trạng thái trạm tại thời điểm xuất — null với biên bản tạo trước migration. */
    private String trangThaiTramTen;
}
