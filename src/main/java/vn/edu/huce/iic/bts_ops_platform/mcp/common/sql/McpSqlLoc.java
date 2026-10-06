package vn.edu.huce.iic.bts_ops_platform.mcp.common.sql;

public final class McpSqlLoc {

    private static final String PHAM_VI_D_DIMENSIONS = """
              AND (CAST(:#{#pv.nhaThauId} AS uuid) IS NULL OR d.nha_thau_id = CAST(:#{#pv.nhaThauId} AS uuid))
              AND (CAST(:#{#pv.doiTuongId} AS uuid) IS NULL OR (d.doi_tuong_quan_ly_id = CAST(:#{#pv.doiTuongId} AS uuid) OR d.id = CAST(:#{#pv.doiTuongId} AS uuid)))
              AND (CAST(:#{#pv.khuVucId} AS uuid) IS NULL OR d.khu_vuc_id = CAST(:#{#pv.khuVucId} AS uuid))
              AND (CAST(:#{#pv.tinhThanhId} AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:#{#pv.tinhThanhId} AS uuid))
            """;

    public static final String PHAM_VI_D = """
              AND (CAST(:#{#pv.hopDongId} AS uuid) IS NULL OR d.hop_dong_id = CAST(:#{#pv.hopDongId} AS uuid))
            """ + PHAM_VI_D_DIMENSIONS + """
              AND (CAST(:#{#pv.loaiHopDongId} AS uuid) IS NULL OR d.hop_dong_id IN (
                    SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:#{#pv.loaiHopDongId} AS uuid)))
            """;

    public static final String PHAM_VI_H = """
              AND (CAST(:#{#pv.hopDongId} AS uuid) IS NULL OR h.id = CAST(:#{#pv.hopDongId} AS uuid))
              AND (CAST(:#{#pv.loaiHopDongId} AS uuid) IS NULL OR h.loai_hop_dong_id = CAST(:#{#pv.loaiHopDongId} AS uuid))
              AND (
                (CAST(:#{#pv.nhaThauId} AS uuid) IS NULL AND CAST(:#{#pv.doiTuongId} AS uuid) IS NULL
                 AND CAST(:#{#pv.khuVucId} AS uuid) IS NULL AND CAST(:#{#pv.tinhThanhId} AS uuid) IS NULL)
                OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.hop_dong_id = h.id AND d.ngay_xoa IS NULL
            """ + PHAM_VI_D_DIMENSIONS + """
                ))
            """;

    public static final String PHAM_VI_S = """
              AND EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.id = s.hop_dong_doi_tuong_id AND d.ngay_xoa IS NULL
                      AND (CAST(:#{#pv.hopDongId} AS uuid) IS NULL OR d.hop_dong_id = CAST(:#{#pv.hopDongId} AS uuid))
                      AND (CAST(:#{#pv.nhaThauId} AS uuid) IS NULL OR d.nha_thau_id = CAST(:#{#pv.nhaThauId} AS uuid))
                      AND (CAST(:#{#pv.doiTuongIds} AS text) IS NULL OR d.doi_tuong_quan_ly_id = ANY(CAST(string_to_array(CAST(:#{#pv.doiTuongIds} AS text), ',') AS uuid[])) OR d.id = ANY(CAST(string_to_array(CAST(:#{#pv.doiTuongIds} AS text), ',') AS uuid[])))
                      AND (CAST(:#{#pv.khuVucId} AS uuid) IS NULL OR d.khu_vuc_id = CAST(:#{#pv.khuVucId} AS uuid))
                      AND (CAST(:#{#pv.tinhThanhId} AS uuid) IS NULL OR d.tinh_thanh_id = CAST(:#{#pv.tinhThanhId} AS uuid))
                      AND (CAST(:#{#pv.loaiHopDongId} AS uuid) IS NULL OR d.hop_dong_id IN (
                            SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:#{#pv.loaiHopDongId} AS uuid)))
                )
            """;

    public static final String PHAM_VI_V = """
              AND (CAST(:#{#pv.hopDongId} AS uuid) IS NULL OR v.hop_dong_id = CAST(:#{#pv.hopDongId} AS uuid))
              AND (CAST(:#{#pv.loaiHopDongId} AS uuid) IS NULL OR v.hop_dong_id IN (
                    SELECT lh.id FROM hop_dong lh WHERE lh.loai_hop_dong_id = CAST(:#{#pv.loaiHopDongId} AS uuid)))
              AND (
                (CAST(:#{#pv.nhaThauId} AS uuid) IS NULL AND CAST(:#{#pv.doiTuongId} AS uuid) IS NULL
                 AND CAST(:#{#pv.khuVucId} AS uuid) IS NULL AND CAST(:#{#pv.tinhThanhId} AS uuid) IS NULL)
                OR EXISTS (
                    SELECT 1 FROM hop_dong_doi_tuong d
                    WHERE d.id = v.du_lieu_doi_tuong_id AND d.ngay_xoa IS NULL
            """ + PHAM_VI_D_DIMENSIONS + """
                ))
            """;

    private McpSqlLoc() {
    }
}