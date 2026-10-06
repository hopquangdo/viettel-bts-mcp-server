package vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.KhuVucInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.DoiTuongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.KhuVucComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.NhaThauComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.BienBanToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.bienban.dto.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.nhathau.dto.NhaThauInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;

public interface BienBanService {
    BienBanTongQuanToolResponse tongQuan(BienBanFilter filter);

    BienBanThieuKhaoSatResponse thieuKhaoSat(BienBanFilter filter, Integer page, Integer pageSize);

    BienBanThieuHoSoResponse thieuHoSo(BienBanFilter filter, Integer page, Integer pageSize);

    BienBanTheoTrangThaiResponse theoTrangThai(BienBanFilter filter, String trangThai, Integer page, Integer pageSize);

    BienBanQueryResponse query(String khuVuc, String tinhThanh, String hopDong, String nhaThau, String trangThai,
                                                                          String doiTuong, LocalDate fromDate, LocalDate toDate,
                                                                          Integer page, Integer pageSize);
}
