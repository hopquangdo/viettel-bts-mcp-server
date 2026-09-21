package vn.edu.huce.iic.bts_ops_platform.modules.business.phancong.entity;

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

/** Lịch sử phân công nhà thầu — append-only, lưu vĩnh viễn. */
@Getter
@Setter
@Entity
@Table(name = "phan_cong_lich_su")
public class PhanCongLichSu {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "hop_dong_id")
    private UUID hopDongId;

    @Column(name = "hop_dong_doi_tuong_id", nullable = false)
    private UUID hopDongDoiTuongId;

    @Column(name = "nha_thau_cu_id")
    private UUID nhaThauCuId;

    @Column(name = "nha_thau_moi_id")
    private UUID nhaThauMoiId;

    @Column(name = "nha_thau_cu_ten")
    private String nhaThauCuTen;

    @Column(name = "nha_thau_moi_ten")
    private String nhaThauMoiTen;

    @Column(name = "noi_dung", nullable = false, columnDefinition = "text")
    private String noiDung;

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
