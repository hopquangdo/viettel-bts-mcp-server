package vn.edu.huce.iic.bts_ops_platform.modules.core.thuvien.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.util.UUID;

/** Mô tả địa lý/khí hậu/kinh tế theo từng tỉnh — nội dung tĩnh do người dùng tự soạn, dùng để
 * điền tự động các token @ViTriDiaLy/@DacDiemDiaHinh/@KhiHau/@DieuKienKinhTe trong "Thuyết minh
 * khảo sát", khớp đúng cơ chế tra cứu theo tỉnh của hệ thống gốc (bảng TraCuu.ViTriDiaLy). Khóa
 * theo tinhThanhId (nhóm ổn định của 1 tỉnh, không đổi khi tỉnh đổi tên/sáp nhập) thay vì id của
 * từng bản ghi TinhThanh, để không phải lặp lại mô tả cho cả tên cũ lẫn tên mới. */
@Getter
@Setter
@Entity
@Table(name = "mo_ta_tinh")
public class MoTaTinh extends AuditableEntity {

    @Column(name = "tinh_thanh_id", nullable = false, unique = true)
    private UUID tinhThanhId;

    @Column(name = "vi_tri_dia_ly", columnDefinition = "text")
    private String viTriDiaLy;

    @Column(name = "dac_diem_dia_hinh", columnDefinition = "text")
    private String dacDiemDiaHinh;

    @Column(name = "khi_hau", columnDefinition = "text")
    private String khiHau;

    @Column(name = "dieu_kien_kinh_te", columnDefinition = "text")
    private String dieuKienKinhTe;
}
