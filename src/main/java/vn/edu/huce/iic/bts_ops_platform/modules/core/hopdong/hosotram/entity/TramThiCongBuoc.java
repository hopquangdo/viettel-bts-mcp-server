package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.hosotram.entity;

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
@Table(name = "tram_thi_cong_buoc")
public class TramThiCongBuoc extends AuditableEntity {

    @Column(name = "hop_dong_doi_tuong_id", nullable = false)
    private UUID hopDongDoiTuongId;

    @Column(name = "ma_buoc", nullable = false, length = 50)
    private String maBuoc;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "chua_co";

    @Column(name = "ly_do_tu_choi", columnDefinition = "text")
    private String lyDoTuChoi;

    @Column(name = "nguoi_duyet_id")
    private UUID nguoiDuyetId;

    @Column(name = "ngay_duyet")
    private Instant ngayDuyet;
}
