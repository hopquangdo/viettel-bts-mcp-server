package vn.edu.huce.iic.bts_ops_platform.modules.business.luutru.entity;

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

/** Lịch sử archive / khôi phục hợp đồng — append-only. */
@Getter
@Setter
@Entity
@Table(name = "hop_dong_luu_tru_lich_su")
public class HopDongLuuTruLichSu {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "hop_dong_id", nullable = false)
    private UUID hopDongId;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "ngay_archive")
    private Instant ngayArchive;

    @Column(name = "nguoi_archive_id")
    private UUID nguoiArchiveId;

    @Column(name = "nguoi_archive_ten")
    private String nguoiArchiveTen;

    @Column(name = "ghi_chu", columnDefinition = "text")
    private String ghiChu;

    @Column(name = "ngay_tao", nullable = false, updatable = false)
    private Instant ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = Instant.now();
        }
    }
}
