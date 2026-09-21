package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.edu.huce.iic.bts_ops_platform.common.entity.CreatedEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hop_dong_nhom_uu_tien")
public class HopDongNhomUuTien extends CreatedEntity {

    @Column(name = "hop_dong_id", nullable = false)
    private UUID hopDongId;

    @Column(name = "ma", nullable = false, length = 50)
    private String ma;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "mau_sac", length = 20)
    private String mauSac;

    @Column(name = "ke_hoach", nullable = false)
    private Integer keHoach = 0;

    @Column(name = "hoan_thanh", nullable = false)
    private Integer hoanThanh = 0;

    @Column(name = "con_lai", nullable = false)
    private Integer conLai = 0;

    @Column(name = "phan_tram", nullable = false, precision = 5, scale = 2)
    private BigDecimal phanTram = BigDecimal.ZERO;

    @Column(name = "so_nhan", nullable = false)
    private Integer soNhan = 0;

    @Column(name = "thu_tu", nullable = false)
    private Short thuTu = 0;
}
