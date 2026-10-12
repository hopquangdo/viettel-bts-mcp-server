package vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.UUID;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong.dto.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.dto.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituongton.TramTonToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.NumberUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;

public interface DoiTuongTonService {
    TramTonTongQuanToolResponse tongQuan(TramTonFilter filter, Integer quaHanNgay, LocalDate fromDate, LocalDate toDate);

    TramTonDanhSachResponse danhSach(TramTonFilter filter, String tab, Integer quaHanNgay, LocalDate fromDate, LocalDate toDate,
                                            Integer page, Integer pageSize);

    TramTonThieuCapNhatResponse thieuCapNhat(TramTonFilter filter, Integer soNgayThieuCapNhat, LocalDate sinceDate);

    TramTonSanLuongBatThuongResponse sanLuongBatThuong(TramTonFilter filter);

    TramTonXepHangKhuVucResponse xepHangKhuVuc(TramTonFilter filter, Integer top);

    TramTonThieuDieuKienResponse thieuDieuKien(TramTonFilter filter);

    TramTonQueryResponse query(String doiTuong, String hopDong, String nhaThau,
                                      String khuVuc, String tinhThanh, String loaiHopDong, String tab, LocalDate sinceDate, Integer quaHanNgay, Integer topIn,
                                      Integer page, Integer pageSize, Integer soNgayThieuCapNhat,
                                      LocalDate fromDate, LocalDate toDate);
}
