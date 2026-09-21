package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class LuongTrangThaiBuocId implements Serializable {

    @Column(name = "luong_trang_thai_id")
    private UUID luongTrangThaiId;

    @Column(name = "trang_thai_hop_dong_id")
    private UUID trangThaiHopDongId;
}
