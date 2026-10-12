package vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.vuongmac.VuongMac;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong.dto.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.RankedItemDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.request.VuongMacFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacQuaHanResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacTimKiemResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.response.VuongMacXepHangResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.VuongMacItemDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.VuongMacTongQuanDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.dto.VuongMacToolMapper;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.VuongMacToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacXepHangContext;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacXepHangRegistry;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.vuongmac.xephang.VuongMacXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;

public interface VuongMacService {
    VuongMacTongQuanResponse tongQuan(VuongMacFilter filter, LocalDate sinceDate);

    VuongMacTimKiemResponse timKiem(VuongMacFilter filter, String query, Integer page, Integer pageSize);

    VuongMacXepHangResponse xepHang(VuongMacFilter filter, String theo, Integer top);

    VuongMacQuaHanResponse quaHan(Integer quaHanNgay, LocalDate sinceDate, Integer page, Integer pageSize);

    VuongMacQueryResponse query(String doiTuong, String hopDong, String nhaThau, String khuVuc, String tinhThanh, String loaiHopDong,
                                       String trangThai, String kieuVuongMac, String giaiDoan, Boolean dangMoOnly,
                                       String query, Integer top, LocalDate sinceDate,
                                       Integer page, Integer pageSize, Integer quaHanNgay,
                                       String canBo, LocalDate fromDate, LocalDate toDate);
}
