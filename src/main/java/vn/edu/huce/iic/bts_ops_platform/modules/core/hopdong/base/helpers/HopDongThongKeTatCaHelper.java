package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.helpers;

import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.dto.response.LuongTrangThaiBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.KieuHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.cauhinh.entity.LoaiHopDong;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongDanhSachTienDoBuocResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThongKeTheoKieuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThongKeHopDongResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThongKeKieuResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThongKeLoaiResponse;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.base.dto.response.HopDongThongKeTatCaResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gom thống kê hợp đồng theo từng loại hợp đồng NGAY TRONG JAVA từ các bảng đã group sẵn,
 * thay cho việc chạy lại bộ countGroupBy*() một lần cho mỗi loaiHopDongId.
 * Thuần tính toán, không chạm DB — xem HopDongThongKeTatCaHelperTest.
 */
public final class HopDongThongKeTatCaHelper {

    /** Hợp đồng có tỷ lệ hoàn thành dưới ngưỡng này được xem là "chậm tiến độ". */
    public static final double CHAM_TIEN_DO_NGUONG_PHAN_TRAM = 70.0;

    private static final String TRANG_THAI_THI_CONG_MAC_DINH = "CHUA_TC";

    private HopDongThongKeTatCaHelper() {
    }

    /**
     * @param hopDongRows   [id, loaiHopDongId, kieuHopDongId, trangThaiThiCong, maHopDong, ten]
     * @param soDoiTuongRows [hopDongId, count] — số đối tượng đang hoạt động của mỗi hợp đồng
     * @param vuongMacRows  [hopDongId, count] — số đối tượng đang có vướng mắc mở
     * @param trangThaiDoiTuongRows [hopDongId, maTrangThai, count]
     * @param kieuHopDongs  TOÀN BỘ danh mục kiểu (không chỉ kiểu đang có hợp đồng) — dùng để
     *                      dựng đủ tab `theoKieu`, kể cả kiểu chưa có hợp đồng nào
     * @param flowBuocByKieuId luồng trạng thái theo kieuHopDongId; kiểu chưa gán luồng thì vắng mặt
     */
    public static HopDongThongKeTatCaResponse build(
            String capNhatLuc,
            List<Object[]> hopDongRows,
            List<Object[]> soDoiTuongRows,
            List<Object[]> vuongMacRows,
            List<Object[]> trangThaiDoiTuongRows,
            List<LoaiHopDong> loaiHopDongs,
            Collection<KieuHopDong> kieuHopDongs,
            Map<UUID, List<LuongTrangThaiBuocResponse>> flowBuocByKieuId) {

        Map<UUID, Long> soDoiTuongTheoHopDong = toCountById(soDoiTuongRows);
        Map<UUID, Long> vuongMacTheoHopDong = toCountById(vuongMacRows);
        Map<UUID, Map<String, Long>> trangThaiTheoHopDong = toStatusCountsById(trangThaiDoiTuongRows);
        Map<UUID, KieuHopDong> kieuById = indexById(kieuHopDongs, KieuHopDong::getId);
        Map<UUID, LoaiHopDong> loaiById = indexById(loaiHopDongs, LoaiHopDong::getId);

        // Danh mục kiểu theo từng loại — nguồn để gieo tab kiểu (kể cả kiểu chưa có hợp đồng).
        Map<UUID, List<KieuHopDong>> kieuByLoai = new LinkedHashMap<>();
        for (KieuHopDong kieu : kieuHopDongs) {
            if (kieu.getId() != null && kieu.getLoaiHopDongId() != null) {
                kieuByLoai.computeIfAbsent(kieu.getLoaiHopDongId(), ignored -> new ArrayList<>()).add(kieu);
            }
        }

        // Gieo sẵn theo thứ tự danh mục để loại/kiểu chưa có hợp đồng nào vẫn xuất hiện (FE dựng
        // đủ tab); loại đã xóa mà vẫn còn hợp đồng sẽ được computeIfAbsent thêm vào cuối.
        Map<UUID, BoDem> theoLoai = new LinkedHashMap<>();
        for (LoaiHopDong loai : loaiHopDongs) {
            if (loai.getId() != null) {
                theoLoai.put(loai.getId(), boDemChoLoai(loai.getId(), kieuByLoai));
            }
        }

        BoDem tongHop = new BoDem();
        for (Object[] row : hopDongRows) {
            UUID hopDongId = (UUID) row[0];
            UUID loaiHopDongId = (UUID) row[1];
            UUID kieuHopDongId = (UUID) row[2];
            String trangThaiThiCong = row[3] != null ? row[3].toString() : TRANG_THAI_THI_CONG_MAC_DINH;
            Map<String, Long> trangThaiDoiTuong = trangThaiTheoHopDong.getOrDefault(hopDongId, Map.of());

            HopDongThongKeHopDongResponse chiTiet = new HopDongThongKeHopDongResponse(
                    hopDongId,
                    asText(row[4]),
                    asText(row[5]),
                    soDoiTuongTheoHopDong.getOrDefault(hopDongId, 0L),
                    trangThaiDoiTuong,
                    HopDongDanhSachTienDoHelper.computeTyLeHoanThanh(trangThaiDoiTuong),
                    vuongMacTheoHopDong.getOrDefault(hopDongId, 0L));

            tongHop.them(chiTiet, kieuHopDongId, trangThaiThiCong);
            if (loaiHopDongId != null) {
                BoDem boDemLoai = theoLoai.computeIfAbsent(
                        loaiHopDongId, id -> boDemChoLoai(id, kieuByLoai));
                boDemLoai.them(chiTiet, kieuHopDongId, trangThaiThiCong);
                if (kieuHopDongId != null) {
                    boDemLoai.theoKieu
                            .computeIfAbsent(kieuHopDongId, ignored -> new BoDem())
                            .them(chiTiet, kieuHopDongId, trangThaiThiCong);
                }
            }
        }

        List<HopDongThongKeLoaiResponse> items = new ArrayList<>(theoLoai.size());
        for (Map.Entry<UUID, BoDem> entry : theoLoai.entrySet()) {
            items.add(entry.getValue()
                    .toResponse(entry.getKey(), loaiById.get(entry.getKey()), kieuById, flowBuocByKieuId));
        }
        return new HopDongThongKeTatCaResponse(
                capNhatLuc, tongHop.toResponse(null, null, kieuById, flowBuocByKieuId), items);
    }

    /** Bộ đếm của 1 loại, đã gieo sẵn tab cho mọi kiểu thuộc loại đó theo thứ tự danh mục. */
    private static BoDem boDemChoLoai(UUID loaiHopDongId, Map<UUID, List<KieuHopDong>> kieuByLoai) {
        BoDem boDem = new BoDem();
        for (KieuHopDong kieu : kieuByLoai.getOrDefault(loaiHopDongId, List.of())) {
            boDem.theoKieu.put(kieu.getId(), new BoDem());
        }
        return boDem;
    }

    /** Bộ đếm dồn của 1 loại hợp đồng (hoặc của dòng tổng khi không lọc loại nào). */
    private static final class BoDem {

        private long tongHopDong;
        private long tongDoiTuong;
        private long soHopDongChamTienDo;
        /** Tổng số vướng mắc đang mở (pending/in_progress), không phải số HĐ. */
        private long tongVuongMacMo;
        private final Map<String, Long> tienDoTheoTrangThai = new LinkedHashMap<>();
        private final Map<UUID, Long> soLuongTheoKieu = new LinkedHashMap<>();
        private final List<HopDongThongKeHopDongResponse> hopDongChiTiet = new ArrayList<>();

        /** Trạng thái ĐỐI TƯỢNG cộng dồn — nguồn dựng tienDoTheoLuongThucTe ở cấp kiểu. */
        private final Map<String, Long> trangThaiDoiTuongGop = new LinkedHashMap<>();

        /** Chỉ dùng ở cấp loại: bộ đếm con của từng kiểu thuộc loại đó. */
        private final Map<UUID, BoDem> theoKieu = new LinkedHashMap<>();

        void them(HopDongThongKeHopDongResponse chiTiet, UUID kieuHopDongId, String trangThaiThiCong) {
            tongHopDong++;
            tongDoiTuong += chiTiet.soDoiTuong();
            tienDoTheoTrangThai.merge(trangThaiThiCong, 1L, Long::sum);
            chiTiet.tienDoTheoTrangThai().forEach((ma, soLuong) -> trangThaiDoiTuongGop.merge(ma, soLuong, Long::sum));
            if (kieuHopDongId != null) {
                soLuongTheoKieu.merge(kieuHopDongId, 1L, Long::sum);
            }
            // Giữ đúng hành vi thongKe(): bảng chi tiết vốn sinh từ GROUP BY trên hop_dong_doi_tuong
            // nên hợp đồng chưa có đối tượng nào không xuất hiện, và do đó cũng không bị tính là
            // chậm tiến độ (tỷ lệ 0%) hay có vướng mắc.
            if (chiTiet.soDoiTuong() <= 0) {
                return;
            }
            hopDongChiTiet.add(chiTiet);
            if (chiTiet.tyLeHoanThanh() < CHAM_TIEN_DO_NGUONG_PHAN_TRAM) {
                soHopDongChamTienDo++;
            }
            tongVuongMacMo += chiTiet.soVuongMac();
        }

        private List<HopDongThongKeTheoKieuResponse> toTheoKieuList(
                Map<UUID, KieuHopDong> kieuById,
                Map<UUID, List<LuongTrangThaiBuocResponse>> flowBuocByKieuId) {
            List<HopDongThongKeTheoKieuResponse> items = new ArrayList<>(theoKieu.size());
            for (Map.Entry<UUID, BoDem> entry : theoKieu.entrySet()) {
                items.add(entry.getValue().toKieuResponse(
                        entry.getKey(), kieuById.get(entry.getKey()), flowBuocByKieuId));
            }
            return items;
        }

        HopDongThongKeTheoKieuResponse toKieuResponse(
                UUID kieuHopDongId,
                KieuHopDong kieu,
                Map<UUID, List<LuongTrangThaiBuocResponse>> flowBuocByKieuId) {
            List<LuongTrangThaiBuocResponse> flowBuoc =
                    flowBuocByKieuId.getOrDefault(kieuHopDongId, List.of());
            List<HopDongDanhSachTienDoBuocResponse> tienDoTheoLuongThucTe = flowBuoc.isEmpty()
                    ? List.of()
                    : HopDongDanhSachTienDoHelper.buildTienDoBuocListForDisplay(flowBuoc, trangThaiDoiTuongGop);
            return new HopDongThongKeTheoKieuResponse(
                    kieuHopDongId,
                    kieu != null ? kieu.getMa() : null,
                    kieu != null ? kieu.getTen() : null,
                    kieu != null ? kieu.getNhom() : null,
                    tongHopDong,
                    tongHopDong,
                    tongDoiTuong,
                    tienDoTheoTrangThai,
                    HopDongDanhSachTienDoHelper.computeAggregateTyLeHoanThanh(
                            trangThaiDoiTuongGop, tongDoiTuong, flowBuoc),
                    tienDoTheoLuongThucTe,
                    hopDongChiTiet,
                    soHopDongChamTienDo,
                    tongVuongMacMo);
        }

        HopDongThongKeLoaiResponse toResponse(
                UUID loaiHopDongId,
                LoaiHopDong loai,
                Map<UUID, KieuHopDong> kieuById,
                Map<UUID, List<LuongTrangThaiBuocResponse>> flowBuocByKieuId) {
            List<HopDongThongKeKieuResponse> kieuItems = new ArrayList<>(soLuongTheoKieu.size());
            for (Map.Entry<UUID, Long> entry : soLuongTheoKieu.entrySet()) {
                KieuHopDong kieu = kieuById.get(entry.getKey());
                kieuItems.add(new HopDongThongKeKieuResponse(
                        entry.getKey(),
                        kieu != null ? kieu.getMa() : null,
                        kieu != null ? kieu.getTen() : null,
                        kieu != null ? kieu.getNhom() : null,
                        entry.getValue()));
            }
            return new HopDongThongKeLoaiResponse(
                    loaiHopDongId,
                    loai != null ? loai.getMa() : null,
                    loai != null ? loai.getTen() : null,
                    tongHopDong,
                    tongHopDong,
                    tongDoiTuong,
                    tienDoTheoTrangThai,
                    HopDongDanhSachTienDoHelper.computeAggregateTyLeHoanThanh(
                            trangThaiDoiTuongGop, tongDoiTuong, List.of()),
                    kieuItems,
                    toTheoKieuList(kieuById, flowBuocByKieuId),
                    hopDongChiTiet,
                    soHopDongChamTienDo,
                    tongVuongMacMo);
        }
    }

    private static Map<UUID, Long> toCountById(List<Object[]> rows) {
        Map<UUID, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            if (row[0] == null) {
                continue;
            }
            result.merge((UUID) row[0], asLong(row[1]), Long::sum);
        }
        return result;
    }

    private static Map<UUID, Map<String, Long>> toStatusCountsById(List<Object[]> rows) {
        Map<UUID, Map<String, Long>> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            if (row[0] == null) {
                continue;
            }
            String ma = row[1] != null ? row[1].toString() : "UNKNOWN";
            result.computeIfAbsent((UUID) row[0], ignored -> new LinkedHashMap<>())
                    .merge(ma, asLong(row[2]), Long::sum);
        }
        return result;
    }

    private static <T> Map<UUID, T> indexById(
            Collection<T> items, java.util.function.Function<T, UUID> idGetter) {
        Map<UUID, T> result = new LinkedHashMap<>();
        for (T item : items) {
            UUID id = idGetter.apply(item);
            if (id != null) {
                result.putIfAbsent(id, item);
            }
        }
        return result;
    }

    private static long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private static String asText(Object value) {
        return value != null ? value.toString() : null;
    }
}
