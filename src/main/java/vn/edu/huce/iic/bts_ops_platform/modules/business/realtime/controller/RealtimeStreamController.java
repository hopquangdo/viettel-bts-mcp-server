package vn.edu.huce.iic.bts_ops_platform.modules.business.realtime.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.ApiEntityCatalog;
import vn.edu.huce.iic.bts_ops_platform.common.openapi.QuyenHanMa;
import vn.edu.huce.iic.bts_ops_platform.common.security.RequiresPermission;
import vn.edu.huce.iic.bts_ops_platform.common.util.SecurityContextHelper;
import vn.edu.huce.iic.bts_ops_platform.config.OpenApiConfig;
import vn.edu.huce.iic.bts_ops_platform.modules.business.realtime.services.RealtimeEventService;

@RestController
@RequestMapping("/api/v1/realtime")
@RequiredArgsConstructor
@Tag(name = ApiEntityCatalog.Realtime.TAG, description = ApiEntityCatalog.Realtime.DESCRIPTION)
@RequiresPermission(anyOf = {
        QuyenHanMa.QUAN_LY_HOP_DONG,
        QuyenHanMa.QUAN_LY_SAN_LUONG,
        QuyenHanMa.XEM_NHIEM_VU_NHA_THAU
})
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class RealtimeStreamController {

    private final RealtimeEventService realtimeEventService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "SSE stream — thông báo mới và refresh dashboard")
    public SseEmitter stream() {
        return realtimeEventService.subscribe(SecurityContextHelper.requireCurrentUser().id());
    }
}
