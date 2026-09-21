package vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import vn.edu.huce.iic.bts_ops_platform.common.dto.TyLeHuyRow;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.entity.CanhBaoNguongCauHinh;

import java.util.List;
import java.util.UUID;

public interface TyLeHuyTramStatsRepository extends Repository<CanhBaoNguongCauHinh, UUID> {

    @Query(value = """
            SELECT
                COALESCE(CAST(dt.nha_thau_id AS text), '__none__') AS groupKey,
                COALESCE(nd.ho_ten, 'Chưa gán cán bộ') AS groupTen,
                'CAN_BO' AS groupLoai,
                COUNT(*) AS tongTram,
                COUNT(*) FILTER (
                    WHERE dt.ly_do_huy IS NOT NULL
                       OR UPPER(COALESCE(tt.ma, '')) LIKE '%HUY%'
                       OR UPPER(COALESCE(tt.ten, '')) LIKE '%HỦY%'
                       OR UPPER(COALESCE(tt.ten, '')) LIKE '%HUY%'
                ) AS soTramHuy
            FROM hop_dong_doi_tuong dt
            LEFT JOIN trang_thai_hop_dong tt ON tt.id = dt.trang_thai_hop_dong_id
            LEFT JOIN nguoi_dung nd ON nd.id = dt.nha_thau_id AND nd.ngay_xoa IS NULL
            WHERE dt.ngay_xoa IS NULL AND dt.hoat_dong = TRUE
            GROUP BY dt.nha_thau_id, nd.ho_ten
            HAVING COUNT(*) > 0
            ORDER BY soTramHuy DESC
            """, nativeQuery = true)
    List<TyLeHuyRow> thongKeTheoCanBo();

    @Query(value = """
            SELECT
                COALESCE(CAST(dt.khu_vuc_id AS text), '__none__') AS groupKey,
                COALESCE(kv.ten, 'Chưa gán khu vực') AS groupTen,
                'KHU_VUC' AS groupLoai,
                COUNT(*) AS tongTram,
                COUNT(*) FILTER (
                    WHERE dt.ly_do_huy IS NOT NULL
                       OR UPPER(COALESCE(tt.ma, '')) LIKE '%HUY%'
                       OR UPPER(COALESCE(tt.ten, '')) LIKE '%HỦY%'
                       OR UPPER(COALESCE(tt.ten, '')) LIKE '%HUY%'
                ) AS soTramHuy
            FROM hop_dong_doi_tuong dt
            LEFT JOIN trang_thai_hop_dong tt ON tt.id = dt.trang_thai_hop_dong_id
            LEFT JOIN khu_vuc kv ON kv.id = dt.khu_vuc_id AND kv.ngay_xoa IS NULL
            WHERE dt.ngay_xoa IS NULL AND dt.hoat_dong = TRUE
            GROUP BY dt.khu_vuc_id, kv.ten
            HAVING COUNT(*) > 0
            ORDER BY soTramHuy DESC
            """, nativeQuery = true)
    List<TyLeHuyRow> thongKeTheoKhuVuc();
}
