package vn.edu.huce.iic.bts_ops_platform.common.event;

import java.util.UUID;

/**
 * business.vuongmac phát sau khi tự đếm lại số vướng mắc đang mở (repository của chính nó) —
 * core.hopdong tự nghe rồi ghi HopDongDoiTuong.coVuongMacMo bằng repository của chính nó. Tránh
 * business gọi thẳng vào service ghi của core, hoặc core đọc thẳng repository của business.
 */
public record HopDongDoiTuongCoVuongMacChangedEvent(UUID hopDongDoiTuongId, boolean coVuongMacMo) {
}
