package vn.edu.huce.iic.bts_ops_platform.mcp.entity.nguonluc;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.entity.AuditableEntity;
import vn.edu.huce.iic.bts_ops_platform.mcp.dto.nguonviec.NguonViecBangDuLieu;

@Getter
@Setter
@Entity
@Table(name = "nguon_viec_bang")
public class NguonViecBang extends AuditableEntity {

    @Column(name = "trung_tam", nullable = false, length = 50)
    private String trungTam;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "du_lieu", nullable = false, columnDefinition = "jsonb")
    private NguonViecBangDuLieu duLieu = new NguonViecBangDuLieu();
}
