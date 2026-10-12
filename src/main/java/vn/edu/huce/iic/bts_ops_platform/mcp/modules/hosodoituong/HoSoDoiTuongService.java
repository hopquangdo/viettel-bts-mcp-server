package vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import vn.edu.huce.iic.bts_ops_platform.mcp.common.dto.PagedResult;
import vn.edu.huce.iic.bts_ops_platform.mcp.components.*;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.doituong.dto.DoiTuongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hopdong.dto.HopDongInfo;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong.dto.ChuaKhaoSatProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong.dto.HoSoDoiTuongChuaKhaoSatItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong.dto.HoSoDoiTuongKhaoSatXongChuaCoSanLuongItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong.dto.HoSoDoiTuongKhoiCongItem;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong.dto.HoSoDoiTuongQueryResponse;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong.dto.HoSoDoiTuongThuocTinhDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong.dto.HoSoDoiTuongTongQuanDto;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong.dto.KhaoSatXongChuaCoSanLuongProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong.dto.KhoiCongTrongKyProjection;
import vn.edu.huce.iic.bts_ops_platform.mcp.modules.hosodoituong.HoSoDoiTuongToolRepository;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.McpParallel;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.PagingUtil;
import vn.edu.huce.iic.bts_ops_platform.mcp.support.StatusLabels;

public interface HoSoDoiTuongService {
    HoSoDoiTuongQueryResponse query(String doiTuong, String hopDong, String nhaThau, String khuVuc, String tinhThanh,
                                           String trangThaiHopDong, Boolean coNhomUuTien,
                                           LocalDate fromDate, LocalDate toDate, Integer page, Integer pageSize);
}
