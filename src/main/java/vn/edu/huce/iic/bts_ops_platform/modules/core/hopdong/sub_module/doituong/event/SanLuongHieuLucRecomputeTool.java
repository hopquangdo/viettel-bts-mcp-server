package vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.huce.iic.bts_ops_platform.modules.core.hopdong.sub_module.doituong.repository.HopDongDoiTuongRepository;

/**
 * Tool một-lần, TẮT MẶC ĐỊNH — recompute HopDongDoiTuong.sanLuongHieuLuc cho TOÀN BỘ đối
 * tượng (không chỉ id NULL). Dùng khi rule tính "thành tiền" theo trạng thái sản lượng thay
 * đổi (vd: bỏ survey/design ra khỏi thành tiền, chỉ còn done) — số liệu cũ tính theo rule
 * trước cần vá lại.
 *
 * <p>Bật bằng: {@code APP_TOOLS_RECOMPUTE_SAN_LUONG_HIEU_LUC=true} rồi khởi động app 1 lần —
 * xong việc thì TẮT LẠI (bỏ biến môi trường / set false), không để bật thường trực.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.tools.recompute-san-luong-hieu-luc", havingValue = "true")
public class SanLuongHieuLucRecomputeTool implements CommandLineRunner {

    private final HopDongDoiTuongRepository hopDongDoiTuongRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("[SanLuongHieuLucRecomputeTool] recomputing san_luong_hieu_luc for all hop_dong_doi_tuong");
        int updated = hopDongDoiTuongRepository.recalculateSanLuongHieuLucAll();
        log.info("[SanLuongHieuLucRecomputeTool] done — {} rows updated", updated);
    }
}
