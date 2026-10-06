package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.hopdong.HopDong;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong.dto.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongBuocLichSuItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongBuocPhanBoItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongCanhBaoItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongCanhBaoTienDoResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongSoLuongTheoKieuItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTheoBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTheoLoaiItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTheoLoaiResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTimKiemItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTimKiemResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTongQuanResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTongQuanToolResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongToolMapper;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongTrangThaiKhuVucResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongXepHangResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.LichSuBuocProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.PhanBoBuocProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.HopDongToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangChieu;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangContext;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangDong;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangRegistry;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.xephang.HopDongXepHangStrategy;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NumberUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;

public interface HopDongService {
    HopDongTongQuanToolResponse tongQuan(HopDongFilter filter, Double nguongChamTienDo, LocalDate fromDate, LocalDate toDate);

    HopDongTheoLoaiResponse thongKeTheoLoai(HopDongFilter filter, Double nguongChamTienDoIn);

    HopDongTimKiemResponse timKiem(HopDongFilter filter, String query, LocalDate fromDate, LocalDate toDate, String loaiNgay,
                                          Integer page, Integer pageSize);

    HopDongTrangThaiKhuVucResponse thongKeThuHep(HopDongFilter filter);

    HopDongCanhBaoTienDoResponse canhBaoTienDo(HopDongFilter filter, Double nguongXanh, Double nguongVang);

    HopDongXepHangResponse xepHang(HopDongFilter filter, String theo, Integer top);

    HopDongTheoBuocResponse theoBuoc(String hopDong, Integer page, Integer pageSize);

    HopDongQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                                      String khuVuc, String tinhThanh, String loaiHopDong, String kieuHopDong, String query, Integer page, Integer pageSize,
                                      Double nguongChamTienDo, Double nguongXanh, Double nguongVang,
                                      Integer top, LocalDate fromDate, LocalDate toDate, String loaiNgay);
}
