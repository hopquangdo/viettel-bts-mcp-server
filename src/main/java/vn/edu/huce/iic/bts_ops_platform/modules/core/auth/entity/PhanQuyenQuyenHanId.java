package vn.edu.huce.iic.bts_ops_platform.modules.core.auth.entity;

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
public class PhanQuyenQuyenHanId implements Serializable {

    @Column(name = "quyen_han_id")
    private UUID quyenHanId;

    @Column(name = "quyen_id", length = 100)
    private String quyenId;
}
