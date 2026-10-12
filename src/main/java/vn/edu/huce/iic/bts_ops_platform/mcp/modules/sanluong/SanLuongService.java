package vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.OutputSummaryDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PeriodInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PeriodTrendDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.ProgressSummaryDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankedEntityDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.RankingOverviewDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.TrendInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.util.VietnamDateUtils;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong.dto.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.DoiTuongListProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.DoiTuongProgressProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.HangMucDoiTuongProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.NhaThauChuaBaoProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.NhaThauDaBaoProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.ProgressGroupProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.query.SanLuongProgressGroupQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.response.SanLuongProgressSummaryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongDoiTuongChuaCoItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongDoiTuongItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongHangMucDoiTuongDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongHangMucItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongNghiemThuDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongNhaThauItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongPeriodTrendDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.SanLuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.SanLuongToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangContext;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangRegistry;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.xephang.SanLuongXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NumberUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PeriodComparisonHelper;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.RankingBuilder;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.TrendCalculator;

public interface SanLuongService {
    SanLuongProgressSummaryResponse tienDo(
            vn.edu.huce.iic.bts_ops_platform.mcp.modules.sanluong.dto.request.SanLuongFilter filter,
            Double nguongHoanThanhThap);

    SanLuongTongQuanToolResponse tongQuan(SanLuongFilter filter, Double nguongHoanThanhThap);

    SanLuongXepHangResponse xepHang(SanLuongFilter filter, String theo, String xepHangTheo, Boolean tangDan);

    SanLuongXuHuongResponse xuHuong(SanLuongFilter filter);

    SanLuongNhaThauBaoCaoResponse nhaThauBaoCao(SanLuongFilter filter, String trangThai, Integer page, Integer pageSize);

    SanLuongNghiemThuResponse nghiemThu(SanLuongFilter filter, Integer page, Integer pageSize);

    SanLuongDoiTuongChuaCoResponse doiTuongChuaCo(SanLuongFilter filter, Integer page, Integer pageSize);

    SanLuongDanhSachDoiTuongResponse danhSachDoiTuong(SanLuongFilter filter, Boolean includeWithoutOutput, String sapXep,
                                                            Integer page, Integer pageSize);

    SanLuongHangMucDoiTuongResponse hangMucDoiTuong(String doiTuong);

    SanLuongQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                                       String khuVuc, String tinhThanh, LocalDate fromDate, LocalDate toDate,
                                       Integer page, Integer pageSize, Double nguongHoanThanhThap, Boolean includeWithoutOutput,
                                       String sapXep, String loaiHopDong, String xepHangTheo, Boolean tangDan);
}
