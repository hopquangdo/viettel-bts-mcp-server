        package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response;

        import lombok.Data;
        import java.util.UUID;
import java.time.Instant;

        @Data
        public class HopDongDoiTuongTrangThaiResponse {
            private UUID id;
    private UUID kieuHopDongId;
    private UUID loaiHopDongId;
    private UUID luongTrangThaiId;
    private UUID doiTuongId;
    private UUID trangThaiId;
    private String trangThaiMa;
    private String trangThaiTen;
    private String nhanHienThi;
    private String mauSac;
    private String mauNen;
    private String giaTriLoc;
    private Short thuTu;
    private Boolean ghiDe;
    private Boolean hoatDong;
    private Instant ngayTao;
    private Instant ngayCapNhat;
        }
