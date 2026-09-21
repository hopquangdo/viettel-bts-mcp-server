package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

/** Danh mục các loại biên bản/hồ sơ có thể yêu cầu trong quy trình 1 hợp đồng — dùng chung cho cả
 * 2 luồng (thi công lẫn tư vấn thiết kế), gán vào từng kiểu hợp đồng qua
 * {@link KieuHopDongDanhMucBienBan} để tạo checklist riêng cho từng luồng. Loại nào
 * {@code coMauWord = false} thì chưa có mẫu Word tự động sinh — trạm/hợp đồng chỉ có thể tải lên
 * file thủ công (qua HoSoTramTepDinhKem), giao diện phải ghi rõ "Chưa được cung cấp", không bịa
 * cấu trúc/nội dung mẫu. */
@Getter
@Setter
@Entity
@Table(name = "danh_muc_bien_ban")
public class DanhMucBienBan extends AuditableEntity {

    @Column(name = "ma", nullable = false, unique = true, length = 50)
    private String ma;

    @Column(name = "ten", nullable = false, length = 255)
    private String ten;

    @Column(name = "mo_ta", columnDefinition = "text")
    private String moTa;

    /** Đã có mẫu Word thật để tự sinh hồ sơ hay chưa — false thì giao diện hiện "Chưa được cung
     * cấp" và chỉ cho tải lên thủ công, không tạo file giả. */
    @Column(name = "co_mau_word", nullable = false)
    private Boolean coMauWord = false;

    /** truoc_thi_cong | thi_cong | nghiem_thu | quyet_toan — hub hồ sơ trạm nhóm các dòng checklist
     * theo giai đoạn và áp gate tuần tự đúng nhóm. Null coi như thi_cong. */
    @Column(name = "giai_doan", length = 30)
    private String giaiDoan;

    @Column(name = "thu_tu_mac_dinh")
    private Integer thuTuMacDinh;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
