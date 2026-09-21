package vn.edu.huce.iic.bts_ops_platform.modules.core.file.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.AuditableEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tep_dinh_kem")
public class TepDinhKem extends AuditableEntity {

    @Column(name = "ma", length = 100)
    private String ma;

    @Column(name = "ten_tep", nullable = false)
    private String tenTep;

    @Column(name = "ten_tep_goc")
    private String tenTepGoc;

    @Column(name = "duong_dan", nullable = false, length = 1000)
    private String duongDan;

    @Column(name = "url", length = 2000)
    private String url;

    @Column(name = "loai_tep", length = 50)
    private String loaiTep;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "kich_thuoc")
    private Long kichThuoc;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "nguoi_tao_id")
    private UUID nguoiTaoId;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;
}
