package vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.CanBoInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.GeoRefResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.security.DataScopeService;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.CanBoComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.DoiTuongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.KhuVucComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.phancong.PhanCong;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong.dto.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.ChuaPhanCongRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongChuaPhanCongResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongChuaPhanCongToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongDanhSachResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongTheoCanBoResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongTongQuanToolResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongToolThongKe;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongTyLeHuyItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongTyLeHuyResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongXepHangNhaThauResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.PhanCongXepHangNhaThauToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query.PhanCongDanhSachQuery;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.query.PhanCongDanhSachResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.TongQuanDoiTuongRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.XepHangNhaThauRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpFormat;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;

public interface PhanCongService {
    vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.response.PhanCongDanhSachResponse danhSach(
            vn.edu.huce.iic.bts_ops_platform.mcp.modules.phancong.dto.request.PhanCongFilter filter, Integer page, Integer pageSize);

    PhanCongTongQuanToolResponse tongQuan(PhanCongFilter filter);

    PhanCongTyLeHuyResponse tyLeHuyTram(String theo, Boolean chiVuotNguong);

    PhanCongDanhSachResponse danhSach(PhanCongFilter filter, String query, Boolean lichSu, Integer page, Integer pageSize);

    PhanCongTheoCanBoResponse theoCanBo(String canBo, LocalDate fromDate, LocalDate toDate, Integer page, Integer pageSize);

    PhanCongChuaPhanCongResponse chuaPhanCong(PhanCongFilter filter, Integer page, Integer pageSize);

    PhanCongXepHangNhaThauResponse xepHangNhaThau(PhanCongFilter filter, Integer top);

    PhanCongQueryResponse query(String vung, String nhaThau, String canBo,
                                       String query, String hopDong, String khuVuc, String tinhThanh, String doiTuong, String giaiDoan,
                                       Boolean lichSu, Integer topIn,
                                       Integer page, Integer pageSize,
                                       LocalDate fromDate, LocalDate toDate);
}
