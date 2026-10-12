package vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.DanhMucComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.HopDongComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.KhuVucComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.TinhComponent;
import vn.edu.huce.iic.bts_ops_platform.mcp.entity.hopdong.HopDong;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoChiTietHopDongToolDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoDanhSachHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoFilter;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoHangMucBreakdownToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoHopDongToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoKhuVucCardToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoKhuVucDetailToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoKhuVucToolDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoNhomBreakdownToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoTinhCanhBaoToolItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoTongQuanToolDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoTongQuanToolResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoTramVuotNguongItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.NganHoTramVuotNguongResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.volume.dto.TongQuanAggregateRow;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpFormat;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.ResolveSupport;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;

public interface VolumeService {
    NganHoTongQuanToolResponse tongQuan(NganHoFilter filter);

    NganHoChiTietHopDongToolDto chiTietHopDong(String hopDong, Double nguongCanhBao);

    NganHoKhuVucToolDto theoKhuVuc(String hopDong, Double nguongCanhBao);

    NganHoDanhSachHopDongResponse danhSachHopDong(NganHoFilter filter, String statusFilter, Integer page, Integer pageSize);

    NganHoTramVuotNguongResponse tramVuotNguong(NganHoFilter filter, Integer page, Integer pageSize);

    NganHoQueryResponse query(String hopDong, String query, Double heSo, Double nguongCanhBao,
                                     String loaiHopDong, String statusFilter, String khuVuc, String tinhThanh,
                                     Integer page, Integer pageSize);
}
