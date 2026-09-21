package vn.edu.huce.iic.bts_ops_platform.modules.business.tiendo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "ke_hoach_trien_khai")
public class KeHoachTrienKhai extends AuditableEntity {

    @Column(name = "hop_dong_id", nullable = false)
    private UUID hopDongId;

    @Column(name = "ten", nullable = false, length = 255)
    private String ten;

    /** NHAP | CHO_DUYET | DA_DUYET | TU_CHOI */
    @Column(name = "trang_thai", nullable = false, length = 30)
    private String trangThai = "NHAP";

    @Column(name = "nguoi_lap_id")
    private UUID nguoiLapId;

    @Column(name = "nguoi_duyet_id")
    private UUID nguoiDuyetId;

    @Column(name = "ngay_gui")
    private Instant ngayGui;

    @Column(name = "ngay_duyet")
    private Instant ngayDuyet;

    @Column(name = "ly_do_tu_choi", columnDefinition = "text")
    private String lyDoTuChoi;
}
