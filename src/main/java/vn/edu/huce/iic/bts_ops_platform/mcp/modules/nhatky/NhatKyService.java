package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhatky;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.AppException;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.util.VietnamDateUtils;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.DoiTuongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.exception.ToolErrorCode;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhatky.dto.NhatKyItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhatky.dto.NhatKyQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhatky.dto.NhatKyRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;

public interface NhatKyService {
    NhatKyQueryResponse query(String doiTuong, String hopDong, String hanhDong, String nguoiThucHien,
                                     LocalDate fromDate, LocalDate toDate, String thuTu, Integer page, Integer pageSize);
}
