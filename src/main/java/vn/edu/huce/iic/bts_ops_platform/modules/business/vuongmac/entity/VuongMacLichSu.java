package vn.edu.huce.iic.bts_ops_platform.modules.business.vuongmac.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** Lịch sử xử lý vướng mắc — append-only, không ghi đè ghi chú cũ. */
@Getter
@Setter
@Entity
@Table(name = "vuong_mac_lich_su")
public class VuongMacLichSu {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "vuong_mac_id", nullable = false)
    private UUID vuongMacId;

    @Column(name = "trang_thai_cu", length = 20)
    private String trangThaiCu;

    @Column(name = "trang_thai_moi", nullable = false, length = 20)
    private String trangThaiMoi;

    @Column(name = "ghi_chu", columnDefinition = "text")
    private String ghiChu;

    @Column(name = "ly_do_tu_choi", columnDefinition = "text")
    private String lyDoTuChoi;

    @Column(name = "nguoi_xu_ly_id")
    private UUID nguoiXuLyId;

    @Column(name = "nguoi_xu_ly_ten")
    private String nguoiXuLyTen;

    @Column(name = "nguoi_thuc_hien_id")
    private UUID nguoiThucHienId;

    @Column(name = "nguoi_thuc_hien_ten")
    private String nguoiThucHienTen;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private Instant ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = Instant.now();
        }
    }
}
