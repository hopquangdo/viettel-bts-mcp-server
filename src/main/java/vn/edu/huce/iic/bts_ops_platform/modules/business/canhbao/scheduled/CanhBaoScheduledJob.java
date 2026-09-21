package vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.edu.huce.iic.bts_ops_platform.modules.business.canhbao.services.CanhBaoService;

@Component
@RequiredArgsConstructor
@Slf4j
public class CanhBaoScheduledJob {

    private final CanhBaoService canhBaoService;

    /** Quét KPI deadline + tỷ lệ hủy vượt ngưỡng, phát thông báo và refresh dashboard. */
    @Scheduled(fixedDelayString = "${app.realtime.scan-interval-ms:300000}")
    public void quetCanhBao() {
        try {
            canhBaoService.quetCanhBaoTuDong();
        } catch (Exception ex) {
            log.warn("CanhBaoScheduledJob failed: {}", ex.getMessage());
        }
    }
}
