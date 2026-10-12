package vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankedItemResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.hopdong.HopDong;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.nguonluc.NguonViecBang;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecDanhSachResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecManualRowDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecRowToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecSapHetGiaTriResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecSummaryToolDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecTabCountsToolDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecToolMapper;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.dto.NguonViecXepHangResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.NguonViecOverlayToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.NguonViecToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecXepHangRegistry;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nguonviec.xephang.NguonViecXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NguonViecCalc;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;

public interface NguonViecService {
    NguonViecTongQuanResponse tongQuan(NguonViecFilter filter);

    NguonViecDanhSachResponse danhSach(NguonViecFilter filter, String tab, Integer page, Integer pageSize);

    NguonViecXepHangResponse xepHang(String theo, LocalDate fromDate, LocalDate toDate, Integer top);

    NguonViecSapHetGiaTriResponse sapHetGiaTri(LocalDate fromDate, LocalDate toDate, Double nguongSapHet);

    NguonViecQueryResponse query(String khuVuc, String nhaThau, String trangThai, String loaiCv, String phapLy,
                                        String linhVuc, String tab,
                                        LocalDate fromDate, LocalDate toDate, String query, Integer topIn,
                                        Integer page, Integer pageSize, Double nguongSapHet);
}
