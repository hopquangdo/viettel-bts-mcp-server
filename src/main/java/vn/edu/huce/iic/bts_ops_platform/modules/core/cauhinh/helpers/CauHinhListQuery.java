package vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.helpers;

/**
 * JPQL fragments dùng chung cho các query list trong module cauhinh.
 */
public final class CauHinhListQuery {

    private CauHinhListQuery() {
    }

    public static final String NOT_DELETED = "(:includeDeleted = TRUE OR e.ngayXoa IS NULL)";

    public static final String ACTIVE_ONLY = "(:activeOnly IS NULL OR :activeOnly = FALSE OR e.hoatDong = TRUE)";

    public static final String SELECTOR_ONLY =
            "(:selectorOnly IS NULL OR :selectorOnly = FALSE OR COALESCE(e.hienThiTrenGiaoDien, TRUE) = TRUE)";

    public static final String NOT_DELETED_H = "(:includeDeleted = TRUE OR h.ngayXoa IS NULL)";

    public static final String ACTIVE_ONLY_H = "(:activeOnly IS NULL OR :activeOnly = FALSE OR h.hoatDong = TRUE)";

    public static final String NOT_DELETED_LK = "(:includeDeleted = TRUE OR lk.ngayXoa IS NULL)";

    public static final String ACTIVE_ONLY_LK = "(:activeOnly IS NULL OR :activeOnly = FALSE OR lk.hoatDong = TRUE)";
}
